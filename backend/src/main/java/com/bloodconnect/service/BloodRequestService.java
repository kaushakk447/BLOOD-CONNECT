package com.bloodconnect.service;

import com.bloodconnect.api.dto.*;
import com.bloodconnect.domain.entity.*;
import com.bloodconnect.exception.BadRequestException;
import com.bloodconnect.exception.ResourceNotFoundException;
import com.bloodconnect.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BloodRequestService {
    
    @Autowired
    private BloodRequestRepository bloodRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DonorProfileRepository donorProfileRepository;
    
    @Autowired
    private DonorRequestRepository donorRequestRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private MatchingService matchingService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuditService auditService;
    
    @Transactional
    public BloodRequestResponse createBloodRequest(CreateBloodRequestDTO request, Long requesterId) {
        log.info("Creating blood request for requester: {}", requesterId);
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        validateCreateRequest(request);
        BloodRequest bloodRequest = BloodRequest.builder()
            .requesterId(requesterId)
            .bloodGroup(BloodRequest.BloodGroup.valueOf(request.getBloodGroup().toUpperCase()))
            .unitsRequired(request.getUnitsRequired())
            .urgency(BloodRequest.Urgency.valueOf(request.getUrgency().toUpperCase()))
            .location(request.getLocation())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .contactNumber(request.getContactNumber())
            .requiredDatetime(request.getRequiredDatetime())
            .hospitalId(request.getHospitalId())
            .description(request.getDescription())
            .searchRadiusKm(request.getSearchRadiusKm() != null ? request.getSearchRadiusKm() : 50)
            .status(BloodRequest.Status.SEARCHING)
            .build();
        
        bloodRequest = bloodRequestRepository.save(bloodRequest);
        
        // Trigger matching process
        matchAndNotifyDonors(bloodRequest);
        
        auditService.logAction("CREATE_BLOOD_REQUEST", "BloodRequest", bloodRequest.getId(), null, request.toString(), "SUCCESS");
        
        return mapBloodRequestToResponse(bloodRequest, requester);
    }
    
    @Transactional
    public void matchAndNotifyDonors(BloodRequest bloodRequest) {
        log.info("Starting donor matching for blood request: {}", bloodRequest.getId());
        
        if (isTerminal(bloodRequest.getStatus())) {
            log.info("Skipping matching for terminal blood request {}", bloodRequest.getId());
            return;
        }
        // SEARCHING remains non-terminal when there are no eligible donors.
        bloodRequest.setStatus(BloodRequest.Status.SEARCHING);
        bloodRequestRepository.save(bloodRequest);
        
        // Convert blood group
        DonorProfile.BloodGroup bloodGroup = DonorProfile.BloodGroup.valueOf(
            bloodRequest.getBloodGroup().toString()
        );
        
        // Find nearby compatible donors
        List<DonorProfile> nearbyDonors = matchingService.findNearbyCompatibleDonors(
            bloodGroup,
            bloodRequest.getLatitude(),
            bloodRequest.getLongitude(),
            bloodRequest.getSearchRadiusKm()
        );
        
        log.info("Found {} nearby compatible donors", nearbyDonors.size());
        
       if (nearbyDonors.isEmpty()) {
              log.info(
              "No compatible donors found for blood request {}. Keeping request open.",
               bloodRequest.getId()
              );

              bloodRequestRepository.save(bloodRequest);
               return;
             }
        
        // Create donor requests and send notifications
        for (DonorProfile donorProfile : nearbyDonors) {
            if (donorRequestRepository.findByBloodRequestIdAndDonorId(bloodRequest.getId(), donorProfile.getUserId()).isPresent()) {
                log.debug("Donor {} was already notified for request {}", donorProfile.getUserId(), bloodRequest.getId());
                continue;
            }
            // Calculate distance
            double distance = matchingService.calculateDistance(
                bloodRequest.getLatitude(),
                bloodRequest.getLongitude(),
                donorProfile.getLatitude(),
                donorProfile.getLongitude()
            );
            
            // Create donor request
            DonorRequest donorRequest = DonorRequest.builder()
                .bloodRequestId(bloodRequest.getId())
                .donorId(donorProfile.getUserId())
                .distanceKm(BigDecimal.valueOf(distance))
                .status(DonorRequest.Status.NOTIFIED)
                .build();
            
            donorRequestRepository.save(donorRequest);
            
            // Send notification to donor
            notificationService.notifyDonor(
                donorProfile.getUserId(),
                bloodRequest,
                distance
            );
        }
        
        // Update blood request with donor notification count
        syncDonorCounters(bloodRequest);
        if (bloodRequest.getDonorCountNotified() > 0) {
            bloodRequest.setStatus(BloodRequest.Status.DONORS_NOTIFIED);
        }
        bloodRequestRepository.save(bloodRequest);
    }
    
    @Transactional(readOnly = true)
    public BloodRequestResponse getBloodRequestById(Long requestId, Long userId) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
        if (!bloodRequest.getRequesterId().equals(userId)
            && donorRequestRepository.findByBloodRequestIdAndDonorId(requestId, userId).isEmpty()) {
            throw new com.bloodconnect.exception.ForbiddenException("Not authorized to view this blood request");
        }
        User requester = userRepository.findById(bloodRequest.getRequesterId())
            .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        
        return mapBloodRequestToResponse(bloodRequest, requester);
    }
    
    @Transactional(readOnly = true)
    public List<BloodRequestResponse> getBloodRequestsByRequester(Long requesterId) {
        List<BloodRequest> requests = bloodRequestRepository.findByRequesterId(requesterId);
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        
        return requests.stream()
            .map(br -> mapBloodRequestToResponse(br, requester))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BloodRequestResponse> getAllBloodRequests() {
        List<BloodRequest> requests = bloodRequestRepository.findByStatusIn(Arrays.asList(
            BloodRequest.Status.OPEN, BloodRequest.Status.SEARCHING,
            BloodRequest.Status.DONORS_NOTIFIED, BloodRequest.Status.DONOR_RESPONDED,
            BloodRequest.Status.CONNECTED
        ));
        
        return requests.stream()
            .map(br -> {
                User requester = userRepository.findById(br.getRequesterId()).orElse(null);
                return mapBloodRequestToResponse(br, requester);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public BloodRequestResponse updateBloodRequest(Long requestId, UpdateBloodRequestDTO request, Long userId) {
        BloodRequest bloodRequest = bloodRequestRepository.findByIdAndRequesterId(requestId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
        if (request.getStatus() != null) {
            if (!"CANCELLED".equalsIgnoreCase(request.getStatus())) {
                throw new BadRequestException("Only cancellation can be requested directly");
            }
            if (isTerminal(bloodRequest.getStatus())) {
                throw new BadRequestException("A terminal request cannot be changed");
            }
            bloodRequest.setStatus(BloodRequest.Status.CANCELLED);
        }
        
        if (request.getUnitsFulfilled() != null) {
            bloodRequest.setUnitsFulfilled(request.getUnitsFulfilled());
        }
        
        if (request.getUnitsFulfilled() != null && request.getUnitsFulfilled() < 0) {
            throw new BadRequestException("Units fulfilled cannot be negative");
        }
        if (bloodRequest.getUnitsFulfilled() >= bloodRequest.getUnitsRequired()) {
            bloodRequest.setStatus(BloodRequest.Status.FULFILLED);
        }
        
        bloodRequest = bloodRequestRepository.save(bloodRequest);
        
        User requester = userRepository.findById(userId).orElse(null);
        return mapBloodRequestToResponse(bloodRequest, requester);
    }

    @Transactional
    public BloodRequestResponse retryMatching(Long requestId, Long userId) {
        BloodRequest bloodRequest = bloodRequestRepository.findByIdAndRequesterId(requestId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        if (isTerminal(bloodRequest.getStatus())) {
            throw new BadRequestException("A terminal request cannot be matched again");
        }
        if (bloodRequest.getStatus() == BloodRequest.Status.DONOR_RESPONDED
            || bloodRequest.getStatus() == BloodRequest.Status.CONNECTED) {
            throw new BadRequestException("A request with donor responses cannot be matched again");
        }
        matchAndNotifyDonors(bloodRequest);
        User requester = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapBloodRequestToResponse(bloodRequest, requester);
    }

    private void validateCreateRequest(CreateBloodRequestDTO request) {
        if (request.getRequiredDatetime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Required date and time must be in the future");
        }
        if (request.getSearchRadiusKm() != null && request.getSearchRadiusKm() <= 0) {
            throw new BadRequestException("Search radius must be greater than zero");
        }
        if (request.getLatitude().abs().compareTo(new BigDecimal("90")) > 0
            || request.getLongitude().abs().compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException("Location coordinates are invalid");
        }
        try {
            BloodRequest.BloodGroup.valueOf(request.getBloodGroup().trim().toUpperCase());
            BloodRequest.Urgency.valueOf(request.getUrgency().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid blood group or urgency");
        }
    }

    private void syncDonorCounters(BloodRequest bloodRequest) {
        bloodRequest.setDonorCountNotified((int) donorRequestRepository.countByBloodRequestId(bloodRequest.getId()));
        bloodRequest.setDonorCountResponded((int) donorRequestRepository.countByBloodRequestIdAndStatusIn(
            bloodRequest.getId(), Arrays.asList(DonorRequest.Status.ACCEPTED, DonorRequest.Status.DECLINED)
        ));
    }

    private boolean isTerminal(BloodRequest.Status status) {
        return status == BloodRequest.Status.FULFILLED || status == BloodRequest.Status.CANCELLED || status == BloodRequest.Status.EXPIRED;
    }
    
    private BloodRequestResponse mapBloodRequestToResponse(BloodRequest bloodRequest, User requester) {
        return BloodRequestResponse.builder()
            .id(bloodRequest.getId())
            .bloodGroup(bloodRequest.getBloodGroup().toString())
            .unitsRequired(bloodRequest.getUnitsRequired())
            .unitsFulfilled(bloodRequest.getUnitsFulfilled())
            .urgency(bloodRequest.getUrgency().toString())
            .status(bloodRequest.getStatus().toString())
            .location(bloodRequest.getLocation())
            .latitude(bloodRequest.getLatitude())
            .longitude(bloodRequest.getLongitude())
            .description(bloodRequest.getDescription())
            .contactNumber(bloodRequest.getContactNumber())
            .requiredDatetime(bloodRequest.getRequiredDatetime())
            .donorCountNotified(bloodRequest.getDonorCountNotified())
            .donorCountResponded(bloodRequest.getDonorCountResponded())
            .createdAt(bloodRequest.getCreatedAt())
            .updatedAt(bloodRequest.getUpdatedAt())
            .requester(requester != null ? UserResponse.builder()
                .id(requester.getId())
                .name(requester.getName())
                .email(requester.getEmail())
                .phone(requester.getPhone())
                .smsNotificationsConsent(requester.getSmsNotificationsConsent())
                .build() : null)
            .build();
    }
}
