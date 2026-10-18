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
            .status(BloodRequest.Status.OPEN)
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
        
        // Update status to SEARCHING
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
            bloodRequest.setStatus(BloodRequest.Status.EXPIRED);
            bloodRequestRepository.save(bloodRequest);
            return;
        }
        
        // Create donor requests and send notifications
        for (DonorProfile donorProfile : nearbyDonors) {
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
        bloodRequest.setDonorCountNotified(nearbyDonors.size());
        bloodRequest.setStatus(BloodRequest.Status.DONORS_NOTIFIED);
        bloodRequestRepository.save(bloodRequest);
    }
    
    @Transactional(readOnly = true)
    public BloodRequestResponse getBloodRequestById(Long requestId, Long userId) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
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
        List<BloodRequest> requests = bloodRequestRepository.findByStatus(BloodRequest.Status.OPEN);
        
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
            bloodRequest.setStatus(BloodRequest.Status.valueOf(request.getStatus().toUpperCase()));
        }
        
        if (request.getUnitsFulfilled() != null) {
            bloodRequest.setUnitsFulfilled(request.getUnitsFulfilled());
        }
        
        if (bloodRequest.getUnitsFulfilled() >= bloodRequest.getUnitsRequired()) {
            bloodRequest.setStatus(BloodRequest.Status.FULFILLED);
        }
        
        bloodRequest = bloodRequestRepository.save(bloodRequest);
        
        User requester = userRepository.findById(userId).orElse(null);
        return mapBloodRequestToResponse(bloodRequest, requester);
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
                .build() : null)
            .build();
    }
}
