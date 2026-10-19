package com.bloodconnect.service;

import com.bloodconnect.domain.entity.BloodRequest;
import com.bloodconnect.domain.entity.DonorProfile;
import com.bloodconnect.domain.entity.DonorRequest;
import com.bloodconnect.domain.entity.User;
import com.bloodconnect.repository.BloodRequestRepository;
import com.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.repository.DonorRequestRepository;
import com.bloodconnect.repository.NotificationRepository;
import com.bloodconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminVerificationMatchingTest {

    @Mock private UserRepository userRepository;
    @Mock private BloodRequestRepository bloodRequestRepository;
    @Mock private DonorProfileRepository donorProfileRepository;
    @Mock private DonorRequestRepository donorRequestRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    private MatchingService matchingService;
    private BloodRequestService bloodRequestService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService();
        ReflectionTestUtils.setField(matchingService, "donorProfileRepository", donorProfileRepository);

        bloodRequestService = new BloodRequestService();
        ReflectionTestUtils.setField(bloodRequestService, "userRepository", userRepository);
        ReflectionTestUtils.setField(bloodRequestService, "bloodRequestRepository", bloodRequestRepository);
        ReflectionTestUtils.setField(bloodRequestService, "donorProfileRepository", donorProfileRepository);
        ReflectionTestUtils.setField(bloodRequestService, "donorRequestRepository", donorRequestRepository);
        ReflectionTestUtils.setField(bloodRequestService, "notificationRepository", notificationRepository);
        ReflectionTestUtils.setField(bloodRequestService, "matchingService", matchingService);
        ReflectionTestUtils.setField(bloodRequestService, "notificationService", notificationService);
        ReflectionTestUtils.setField(bloodRequestService, "auditService", auditService);

        adminService = new AdminService();
        ReflectionTestUtils.setField(adminService, "donorProfileRepository", donorProfileRepository);
        ReflectionTestUtils.setField(adminService, "userRepository", userRepository);
        ReflectionTestUtils.setField(adminService, "bloodRequestRepository", bloodRequestRepository);
        ReflectionTestUtils.setField(adminService, "bloodRequestService", bloodRequestService);
        ReflectionTestUtils.setField(adminService, "auditService", auditService);
    }

    @Test
    void pendingDonorsAreExcludedByTheVerifiedAvailableMatchingQuery() {
        when(donorProfileRepository.findByBloodGroupInAndVerificationStatusAndAvailabilityStatus(
            anyList(), eq(DonorProfile.VerificationStatus.VERIFIED), eq(DonorProfile.AvailabilityStatus.AVAILABLE)))
            .thenReturn(List.of());

        assertTrue(matchingService.findNearbyCompatibleDonors(
            DonorProfile.BloodGroup.O_POSITIVE, new BigDecimal("13.0827"), new BigDecimal("80.2707"), 50
        ).isEmpty());

        verify(donorProfileRepository).findByBloodGroupInAndVerificationStatusAndAvailabilityStatus(
            anyList(), eq(DonorProfile.VerificationStatus.VERIFIED), eq(DonorProfile.AvailabilityStatus.AVAILABLE));
    }

    @Test
    void verifyingAvailableDonorMatchesSearchingRequestOnceAndRetriesStayIdempotent() {
        DonorProfile donor = donor(10L, DonorProfile.VerificationStatus.PENDING,
            DonorProfile.AvailabilityStatus.AVAILABLE, new BigDecimal("13.0827"), new BigDecimal("80.2707"));
        BloodRequest request = searchingRequest();
        AtomicBoolean invitationExists = new AtomicBoolean(false);

        when(donorProfileRepository.findById(10L)).thenReturn(Optional.of(donor));
        when(donorProfileRepository.save(any(DonorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(2L)).thenReturn(Optional.of(donorUser()));
        when(bloodRequestRepository.findByStatus(BloodRequest.Status.SEARCHING)).thenReturn(List.of(request));
        when(donorProfileRepository.findByBloodGroupInAndVerificationStatusAndAvailabilityStatus(
            anyList(), eq(DonorProfile.VerificationStatus.VERIFIED), eq(DonorProfile.AvailabilityStatus.AVAILABLE)))
            .thenReturn(List.of(donor));
        when(bloodRequestRepository.save(any(BloodRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(donorRequestRepository.findByBloodRequestIdAndDonorId(100L, 2L)).thenAnswer(invocation ->
            invitationExists.get() ? Optional.of(DonorRequest.builder().id(200L).build()) : Optional.empty());
        when(donorRequestRepository.save(any(DonorRequest.class))).thenAnswer(invocation -> {
            invitationExists.set(true);
            return invocation.getArgument(0);
        });
        when(donorRequestRepository.countByBloodRequestId(100L)).thenReturn(1L);
        when(donorRequestRepository.countByBloodRequestIdAndStatusIn(eq(100L), anyList())).thenReturn(0L);

        adminService.verifyDonor(10L, true);
        adminService.verifyDonor(10L, true);
        bloodRequestService.matchAndNotifyDonors(request);

        assertEquals(DonorProfile.VerificationStatus.VERIFIED, donor.getVerificationStatus());
        assertEquals(BloodRequest.Status.DONORS_NOTIFIED, request.getStatus());
        ArgumentCaptor<DonorRequest> invitationCaptor = ArgumentCaptor.forClass(DonorRequest.class);
        verify(donorRequestRepository, times(1)).save(invitationCaptor.capture());
        assertEquals(100L, invitationCaptor.getValue().getBloodRequestId());
        assertEquals(2L, invitationCaptor.getValue().getDonorId());
        verify(notificationService, times(1)).notifyDonor(eq(2L), eq(request), any(Double.class));
        verify(bloodRequestRepository, times(1)).findByStatus(BloodRequest.Status.SEARCHING);
    }

    @Test
    void verifyingUnavailableDonorDoesNotRematchOrInvite() {
        DonorProfile donor = donor(11L, DonorProfile.VerificationStatus.PENDING,
            DonorProfile.AvailabilityStatus.UNAVAILABLE, new BigDecimal("13.0827"), new BigDecimal("80.2707"));
        when(donorProfileRepository.findById(11L)).thenReturn(Optional.of(donor));
        when(donorProfileRepository.save(donor)).thenReturn(donor);
        when(userRepository.findById(2L)).thenReturn(Optional.of(donorUser()));

        adminService.verifyDonor(11L, true);

        assertEquals(DonorProfile.VerificationStatus.VERIFIED, donor.getVerificationStatus());
        verify(bloodRequestRepository, never()).findByStatus(any());
        verify(donorRequestRepository, never()).save(any(DonorRequest.class));
        verify(notificationService, never()).notifyDonor(any(), any(), any(Double.class));
    }

    private DonorProfile donor(Long id, DonorProfile.VerificationStatus verificationStatus,
                               DonorProfile.AvailabilityStatus availabilityStatus,
                               BigDecimal latitude, BigDecimal longitude) {
        return DonorProfile.builder()
            .id(id).userId(2L).bloodGroup(DonorProfile.BloodGroup.O_POSITIVE)
            .dateOfBirth(LocalDate.of(1990, 1, 1)).weightKg(new BigDecimal("70"))
            .location("Chennai").latitude(latitude).longitude(longitude)
            .availabilityStatus(availabilityStatus).verificationStatus(verificationStatus)
            .donationCount(0).allowLocationSharing(false).pushNotificationsEnabled(true)
            .build();
    }

    private BloodRequest searchingRequest() {
        return BloodRequest.builder()
            .id(100L).requesterId(1L).bloodGroup(BloodRequest.BloodGroup.O_POSITIVE)
            .unitsRequired(1).unitsFulfilled(0).urgency(BloodRequest.Urgency.HIGH)
            .location("Chennai").latitude(new BigDecimal("13.0827")).longitude(new BigDecimal("80.2707"))
            .contactNumber("9999999999").requiredDatetime(LocalDateTime.now().plusHours(2))
            .searchRadiusKm(50).status(BloodRequest.Status.SEARCHING)
            .donorCountNotified(0).donorCountResponded(0).build();
    }

    private User donorUser() {
        return User.builder().id(2L).name("Donor").email("donor@example.test").phone("8888888888")
            .role(User.UserRole.DONOR).build();
    }
}
