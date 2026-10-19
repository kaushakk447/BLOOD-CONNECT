package com.bloodconnect.service;

import com.bloodconnect.api.dto.DonorRequestResponse;
import com.bloodconnect.domain.entity.BloodRequest;
import com.bloodconnect.domain.entity.DonorRequest;
import com.bloodconnect.domain.entity.User;
import com.bloodconnect.exception.ForbiddenException;
import com.bloodconnect.repository.BloodRequestRepository;
import com.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.repository.DonorRequestRepository;
import com.bloodconnect.repository.NotificationRepository;
import com.bloodconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestAccessAndDonorInboxTest {

    @Mock private UserRepository userRepository;
    @Mock private BloodRequestRepository bloodRequestRepository;
    @Mock private DonorProfileRepository donorProfileRepository;
    @Mock private DonorRequestRepository donorRequestRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    private BloodRequestService bloodRequestService;
    private DonorService donorService;

    @BeforeEach
    void setUp() {
        bloodRequestService = new BloodRequestService();
        ReflectionTestUtils.setField(bloodRequestService, "userRepository", userRepository);
        ReflectionTestUtils.setField(bloodRequestService, "bloodRequestRepository", bloodRequestRepository);
        ReflectionTestUtils.setField(bloodRequestService, "donorProfileRepository", donorProfileRepository);
        ReflectionTestUtils.setField(bloodRequestService, "donorRequestRepository", donorRequestRepository);
        ReflectionTestUtils.setField(bloodRequestService, "notificationRepository", notificationRepository);
        ReflectionTestUtils.setField(bloodRequestService, "notificationService", notificationService);
        ReflectionTestUtils.setField(bloodRequestService, "auditService", auditService);

        donorService = new DonorService();
        ReflectionTestUtils.setField(donorService, "userRepository", userRepository);
        ReflectionTestUtils.setField(donorService, "bloodRequestRepository", bloodRequestRepository);
        ReflectionTestUtils.setField(donorService, "donorRequestRepository", donorRequestRepository);
        ReflectionTestUtils.setField(donorService, "notificationService", notificationService);
        ReflectionTestUtils.setField(donorService, "auditService", auditService);
    }

    @Test
    void patientCannotReadAnotherPatientsPrivateRequestById() {
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest()));

        assertThrows(ForbiddenException.class, () -> bloodRequestService.getBloodRequestById(100L, 2L));

        verify(donorRequestRepository, never()).findByBloodRequestIdAndDonorId(any(), any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void donorInvitationDoesNotGrantPrivateRequestDetailAccess() {
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest()));
        lenient().when(donorRequestRepository.findByBloodRequestIdAndDonorId(100L, 2L))
            .thenReturn(Optional.of(donorRequest(2L, DonorRequest.Status.NOTIFIED)));

        assertThrows(ForbiddenException.class, () -> bloodRequestService.getBloodRequestById(100L, 2L));
        verify(donorRequestRepository, never()).findByBloodRequestIdAndDonorId(100L, 2L);
    }

    @Test
    void donorInboxReturnsOnlyThatDonorsInvitationAndRedactsPrivateDataUntilAcceptance() {
        DonorRequest invitation = donorRequest(2L, DonorRequest.Status.NOTIFIED);
        when(donorRequestRepository.findByDonorIdAndStatus(2L, DonorRequest.Status.NOTIFIED))
            .thenReturn(List.of(invitation));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester()));

        List<DonorRequestResponse> responses = donorService.getPendingDonorRequests(2L);

        assertEquals(1, responses.size());
        assertEquals(2L, responses.get(0).getDonorId());
        assertNull(responses.get(0).getBloodRequest().getContactNumber());
        assertNull(responses.get(0).getBloodRequest().getLatitude());
        assertNull(responses.get(0).getBloodRequest().getLongitude());
        assertNull(responses.get(0).getBloodRequest().getRequester());
        verify(donorRequestRepository).findByDonorIdAndStatus(2L, DonorRequest.Status.NOTIFIED);
        verify(donorRequestRepository, never()).findByDonorIdAndStatus(3L, DonorRequest.Status.NOTIFIED);
    }

    @Test
    void acceptedInvitationRevealsOnlyTheAuthorizedContactDetails() {
        DonorRequest acceptedInvitation = donorRequest(2L, DonorRequest.Status.ACCEPTED);
        when(donorRequestRepository.findByDonorId(2L)).thenReturn(List.of(acceptedInvitation));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester()));

        DonorRequestResponse response = donorService.getDonorRequests(2L).get(0);

        assertEquals("9999999999", response.getBloodRequest().getContactNumber());
        assertEquals("Patient A", response.getBloodRequest().getRequester().getName());
        assertEquals("9999999999", response.getBloodRequest().getRequester().getPhone());
        assertNull(response.getBloodRequest().getRequester().getEmail());
        assertNull(response.getBloodRequest().getLatitude());
        assertNull(response.getBloodRequest().getLongitude());
    }

    private BloodRequest bloodRequest() {
        return BloodRequest.builder()
            .id(100L).requesterId(1L).bloodGroup(BloodRequest.BloodGroup.O_POSITIVE)
            .unitsRequired(1).unitsFulfilled(0).urgency(BloodRequest.Urgency.HIGH)
            .location("Chennai").latitude(new BigDecimal("13.0827")).longitude(new BigDecimal("80.2707"))
            .contactNumber("9999999999").requiredDatetime(LocalDateTime.now().plusHours(2))
            .searchRadiusKm(50).status(BloodRequest.Status.SEARCHING)
            .donorCountNotified(1).donorCountResponded(0).build();
    }

    private DonorRequest donorRequest(Long donorId, DonorRequest.Status status) {
        return DonorRequest.builder().id(200L).bloodRequestId(100L).donorId(donorId)
            .distanceKm(new BigDecimal("4.2")).status(status).build();
    }

    private User requester() {
        return User.builder().id(1L).name("Patient A").email("patient@example.test")
            .phone("9999999999").role(User.UserRole.PATIENT).build();
    }
}
