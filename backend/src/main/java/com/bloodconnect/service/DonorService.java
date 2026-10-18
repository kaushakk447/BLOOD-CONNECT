package com.bloodconnect.service;

import com.bloodconnect.api.dto.*;
import com.bloodconnect.domain.entity.*;
import com.bloodconnect.exception.BadRequestException;
import com.bloodconnect.exception.ForbiddenException;
import com.bloodconnect.exception.ResourceNotFoundException;
import com.bloodconnect.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DonorService {
    
    @Autowired
    private DonorProfileRepository donorProfileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DonorRequestRepository donorRequestRepository;
    
    @Autowired
    private BloodRequestRepository bloodRequestRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuditService auditService;
    
    @Transactional
    public DonorProfileResponse createDonorProfile(CreateDonorProfileDTO request, Long userId) {
        log.info("Creating donor profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (donorProfileRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("Donor profile already exists for this user");
        }
        
        DonorProfile donorProfile = DonorProfile.builder()
            .userId(userId)
            .bloodGroup(DonorProfile.BloodGroup.valueOf(request.getBloodGroup().toUpperCase()))
            .dateOfBirth(request.getDateOfBirth())
            .weightKg(request.getWeightKg())
            .location(request.getLocation())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .lastDonationDate(request.getLastDonationDate())
            .allowLocationSharing(request.getAllowLocationSharing() != null ? request.getAllowLocationSharing() : false)
            .pushNotificationsEnabled(request.getPushNotificationsEnabled() != null ? request.getPushNotificationsEnabled() : true)
            .verificationStatus(DonorProfile.VerificationStatus.PENDING)
            .availabilityStatus(DonorProfile.AvailabilityStatus.UNAVAILABLE)
            .build();
        
        donorProfile = donorProfileRepository.save(donorProfile);
        
        auditService.logAction("CREATE_DONOR_PROFILE", "DonorProfile", donorProfile.getId(), null, request.toString(), "SUCCESS");
        
        return mapDonorProfileToResponse(donorProfile, user);
    }
    
    @Transactional(readOnly = true)
    public DonorProfileResponse getDonorProfile(Long userId) {
        DonorProfile donorProfile = donorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return mapDonorProfileToResponse(donorProfile, user);
    }
    
    @Transactional
    public DonorProfileResponse updateDonorAvailability(Long userId, UpdateDonorAvailabilityDTO request) {
        log.info("Updating donor availability for user: {}", userId);
        
        DonorProfile donorProfile = donorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));
        
        donorProfile.setAvailabilityStatus(
            DonorProfile.AvailabilityStatus.valueOf(request.getAvailabilityStatus().toUpperCase())
        );
        
        donorProfile = donorProfileRepository.save(donorProfile);
        
        User user = userRepository.findById(userId).orElse(null);
        return mapDonorProfileToResponse(donorProfile, user);
    }
    
    @Transactional(readOnly = true)
    public List<DonorRequestResponse> getDonorRequests(Long donorUserId) {
        List<DonorRequest> donorRequests = donorRequestRepository.findByDonorId(donorUserId);
        
        return donorRequests.stream()
            .map(dr -> {
                BloodRequest bloodRequest = bloodRequestRepository.findById(dr.getBloodRequestId())
                    .orElse(null);
                User requester = bloodRequest != null ? 
                    userRepository.findById(bloodRequest.getRequesterId()).orElse(null) : null;
                return mapDonorRequestToResponse(dr, bloodRequest, requester);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DonorRequestResponse> getPendingDonorRequests(Long donorUserId) {
        List<DonorRequest> donorRequests = donorRequestRepository.findByDonorIdAndStatus(
            donorUserId,
            DonorRequest.Status.NOTIFIED
        );
        
        return donorRequests.stream()
            .map(dr -> {
                BloodRequest bloodRequest = bloodRequestRepository.findById(dr.getBloodRequestId())
                    .orElse(null);
                User requester = bloodRequest != null ? 
                    userRepository.findById(bloodRequest.getRequesterId()).orElse(null) : null;
                return mapDonorRequestToResponse(dr, bloodRequest, requester);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public DonorRequestResponse acceptDonorRequest(Long donorRequestId, Long donorUserId) {
        log.info("Donor {} accepting request: {}", donorUserId, donorRequestId);
        
        DonorRequest donorRequest = donorRequestRepository.findById(donorRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor request not found"));
        
        if (!donorRequest.getDonorId().equals(donorUserId)) {
            throw new ForbiddenException("Not authorized to accept this request");
        }
        
        donorRequest.setStatus(DonorRequest.Status.ACCEPTED);
        donorRequest.setRespondedAt(java.time.LocalDateTime.now());
        donorRequest = donorRequestRepository.save(donorRequest);
        
        // Update blood request status
        BloodRequest bloodRequest = bloodRequestRepository.findById(donorRequest.getBloodRequestId())
            .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
        bloodRequest.setDonorCountResponded(bloodRequest.getDonorCountResponded() + 1);
        bloodRequest.setStatus(BloodRequest.Status.DONOR_RESPONDED);
        bloodRequestRepository.save(bloodRequest);
        
        // Notify requester
        User donor = userRepository.findById(donorUserId).orElse(null);
        if (donor != null) {
            notificationService.notifyDonorResponse(bloodRequest.getRequesterId(), donorUserId, donor.getName(), true);
        }
        
        BloodRequest savedRequest = bloodRequestRepository.findById(donorRequest.getBloodRequestId()).orElse(null);
        User requester = savedRequest != null ? 
            userRepository.findById(savedRequest.getRequesterId()).orElse(null) : null;
        return mapDonorRequestToResponse(donorRequest, savedRequest, requester);
    }
    
    @Transactional
    public DonorRequestResponse declineDonorRequest(Long donorRequestId, Long donorUserId) {
        log.info("Donor {} declining request: {}", donorUserId, donorRequestId);
        
        DonorRequest donorRequest = donorRequestRepository.findById(donorRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor request not found"));
        
        if (!donorRequest.getDonorId().equals(donorUserId)) {
            throw new ForbiddenException("Not authorized to decline this request");
        }
        
        donorRequest.setStatus(DonorRequest.Status.DECLINED);
        donorRequest.setRespondedAt(java.time.LocalDateTime.now());
        donorRequest = donorRequestRepository.save(donorRequest);
        
        // Notify requester
        User donor = userRepository.findById(donorUserId).orElse(null);
        BloodRequest bloodRequest = bloodRequestRepository.findById(donorRequest.getBloodRequestId()).orElse(null);
        if (donor != null && bloodRequest != null) {
            notificationService.notifyDonorResponse(bloodRequest.getRequesterId(), donorUserId, donor.getName(), false);
        }
        
        BloodRequest savedRequest = bloodRequestRepository.findById(donorRequest.getBloodRequestId()).orElse(null);
        User requester = savedRequest != null ? 
            userRepository.findById(savedRequest.getRequesterId()).orElse(null) : null;
        return mapDonorRequestToResponse(donorRequest, savedRequest, requester);
    }
    
    private DonorProfileResponse mapDonorProfileToResponse(DonorProfile donorProfile, User user) {
        return DonorProfileResponse.builder()
            .id(donorProfile.getId())
            .userId(donorProfile.getUserId())
            .bloodGroup(donorProfile.getBloodGroup().toString())
            .dateOfBirth(donorProfile.getDateOfBirth())
            .weightKg(donorProfile.getWeightKg())
            .location(donorProfile.getLocation())
            .latitude(donorProfile.getLatitude())
            .longitude(donorProfile.getLongitude())
            .availabilityStatus(donorProfile.getAvailabilityStatus().toString())
            .lastDonationDate(donorProfile.getLastDonationDate())
            .donationCount(donorProfile.getDonationCount())
            .verificationStatus(donorProfile.getVerificationStatus().toString())
            .allowLocationSharing(donorProfile.getAllowLocationSharing())
            .pushNotificationsEnabled(donorProfile.getPushNotificationsEnabled())
            .createdAt(donorProfile.getCreatedAt())
            .user(user != null ? UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build() : null)
            .build();
    }
    
    private DonorRequestResponse mapDonorRequestToResponse(DonorRequest donorRequest, BloodRequest bloodRequest, User requester) {
        return DonorRequestResponse.builder()
            .id(donorRequest.getId())
            .bloodRequestId(donorRequest.getBloodRequestId())
            .donorId(donorRequest.getDonorId())
            .distanceKm(donorRequest.getDistanceKm())
            .status(donorRequest.getStatus().toString())
            .notifiedAt(donorRequest.getNotifiedAt())
            .respondedAt(donorRequest.getRespondedAt())
            .bloodRequest(bloodRequest != null ? BloodRequestResponse.builder()
                .id(bloodRequest.getId())
                .bloodGroup(bloodRequest.getBloodGroup().toString())
                .unitsRequired(bloodRequest.getUnitsRequired())
                .urgency(bloodRequest.getUrgency().toString())
                .status(bloodRequest.getStatus().toString())
                .location(bloodRequest.getLocation())
                .latitude(bloodRequest.getLatitude())
                .longitude(bloodRequest.getLongitude())
                .contactNumber(bloodRequest.getContactNumber())
                .requiredDatetime(bloodRequest.getRequiredDatetime())
                .createdAt(bloodRequest.getCreatedAt())
                .requester(requester != null ? UserResponse.builder()
                    .id(requester.getId())
                    .name(requester.getName())
                    .email(requester.getEmail())
                    .phone(requester.getPhone())
                    .build() : null)
                .build() : null)
            .build();
    }
}
