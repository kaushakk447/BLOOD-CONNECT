package com.bloodconnect.service;

import com.bloodconnect.api.dto.*;
import com.bloodconnect.domain.entity.*;
import com.bloodconnect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private BloodRequestRepository bloodRequestRepository;
    @Mock private DonorProfileRepository donorProfileRepository;
    @Mock private DonorRequestRepository donorRequestRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    private BloodRequestService bloodRequestService;
    private DonorService donorService;
    private MatchingService matchingService;

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

        donorService = new DonorService();
        ReflectionTestUtils.setField(donorService, "userRepository", userRepository);
        ReflectionTestUtils.setField(donorService, "bloodRequestRepository", bloodRequestRepository);
        ReflectionTestUtils.setField(donorService, "donorRequestRepository", donorRequestRepository);
        ReflectionTestUtils.setField(donorService, "notificationService", notificationService);
        ReflectionTestUtils.setField(donorService, "auditService", auditService);
    }

    @Test
    void patientRequestIsMatchedVisibleToDonorAndAcceptanceUpdatesPatientStatus() {
        User patient = User.builder().id(1L).name("Patient").email("patient@example.test").phone("9999999999").build();
        User donor = User.builder().id(2L).name("Donor").email("donor@example.test").phone("8888888888").build();
        DonorProfile eligibleDonor = DonorProfile.builder()
            .id(10L).userId(2L).bloodGroup(DonorProfile.BloodGroup.O_POSITIVE)
            .verificationStatus(DonorProfile.VerificationStatus.VERIFIED)
            .availabilityStatus(DonorProfile.AvailabilityStatus.AVAILABLE)
            .latitude(new BigDecimal("13.0827")).longitude(new BigDecimal("80.2707")).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(donor));
        when(donorProfileRepository.findByBloodGroupInAndVerificationStatusAndAvailabilityStatus(
            anyList(), eq(DonorProfile.VerificationStatus.VERIFIED), eq(DonorProfile.AvailabilityStatus.AVAILABLE)))
            .thenReturn(List.of(eligibleDonor));
        when(bloodRequestRepository.save(any(BloodRequest.class))).thenAnswer(invocation -> {
            BloodRequest request = invocation.getArgument(0);
            if (request.getId() == null) request.setId(100L);
            return request;
        });
        when(donorRequestRepository.findByBloodRequestIdAndDonorId(100L, 2L)).thenReturn(Optional.empty());
        when(donorRequestRepository.save(any(DonorRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(donorRequestRepository.countByBloodRequestId(100L)).thenReturn(1L);
        when(donorRequestRepository.countByBloodRequestIdAndStatusIn(eq(100L), anyList())).thenReturn(0L);

        BloodRequestResponse created = bloodRequestService.createBloodRequest(requestInput(), 1L);
        assertEquals("DONORS_NOTIFIED", created.getStatus());
        ArgumentCaptor<DonorRequest> donorRequestCaptor = ArgumentCaptor.forClass(DonorRequest.class);
        verify(donorRequestRepository).save(donorRequestCaptor.capture());
        DonorRequest donorRequest = donorRequestCaptor.getValue();
        donorRequest.setId(200L);

        when(donorRequestRepository.findByDonorIdAndStatus(2L, DonorRequest.Status.NOTIFIED)).thenReturn(List.of(donorRequest));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(toEntity(created)));
        // Use one persistent object for the donor response and patient tracking assertions.
        BloodRequest persisted = toEntity(created);
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(persisted));
        assertEquals(1, donorService.getPendingDonorRequests(2L).size());

        when(donorRequestRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(donorRequest));
        when(donorRequestRepository.countByBloodRequestIdAndStatusIn(eq(100L), anyList())).thenReturn(1L);
        donorService.acceptDonorRequest(200L, 2L);

        assertEquals(DonorRequest.Status.ACCEPTED, donorRequest.getStatus());
        assertEquals(BloodRequest.Status.DONOR_RESPONDED, persisted.getStatus());
        verify(notificationService).notifyDonorResponse(1L, 200L, donor, true);
        assertEquals("DONOR_RESPONDED", bloodRequestService.getBloodRequestById(100L, 1L).getStatus());
    }

    private CreateBloodRequestDTO requestInput() {
        return CreateBloodRequestDTO.builder().bloodGroup("O_POSITIVE").unitsRequired(1).urgency("HIGH")
            .location("Chennai").latitude(new BigDecimal("13.0827")).longitude(new BigDecimal("80.2707"))
            .contactNumber("9999999999").requiredDatetime(LocalDateTime.now().plusHours(2)).build();
    }

    private BloodRequest toEntity(BloodRequestResponse response) {
        return BloodRequest.builder().id(response.getId()).requesterId(1L)
            .bloodGroup(BloodRequest.BloodGroup.O_POSITIVE).unitsRequired(1).unitsFulfilled(0)
            .urgency(BloodRequest.Urgency.HIGH).location("Chennai")
            .latitude(new BigDecimal("13.0827")).longitude(new BigDecimal("80.2707"))
            .contactNumber("9999999999").requiredDatetime(LocalDateTime.now().plusHours(2))
            .searchRadiusKm(50).status(BloodRequest.Status.DONORS_NOTIFIED)
            .donorCountNotified(1).donorCountResponded(0).build();
    }
}
