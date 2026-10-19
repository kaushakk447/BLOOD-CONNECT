package com.bloodconnect.service;

import com.bloodconnect.api.dto.DonorProfileResponse;
import com.bloodconnect.api.dto.UserResponse;
import com.bloodconnect.domain.entity.BloodRequest;
import com.bloodconnect.domain.entity.DonorProfile;
import com.bloodconnect.domain.entity.User;
import com.bloodconnect.exception.ResourceNotFoundException;
import com.bloodconnect.repository.BloodRequestRepository;
import com.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private BloodRequestService bloodRequestService;

    @Autowired
    private AuditService auditService;

    @Transactional(readOnly = true)
    public List<DonorProfileResponse> getPendingDonors() {
        List<DonorProfile> pending = donorProfileRepository.findByVerificationStatus(
            DonorProfile.VerificationStatus.PENDING
        );
        return pending.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(u -> UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole().toString())
                .smsNotificationsConsent(u.getSmsNotificationsConsent())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public DonorProfileResponse verifyDonor(Long donorProfileId, boolean verified) {
        DonorProfile donorProfile = donorProfileRepository.findById(donorProfileId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        boolean becameVerified = verified
            && donorProfile.getVerificationStatus() != DonorProfile.VerificationStatus.VERIFIED;
        donorProfile.setVerificationStatus(
            verified ? DonorProfile.VerificationStatus.VERIFIED : DonorProfile.VerificationStatus.REJECTED
        );

        donorProfile = donorProfileRepository.save(donorProfile);

        auditService.logAction("VERIFY_DONOR", "DonorProfile", donorProfile.getId(), null,
            "verified=" + verified, "SUCCESS");

        if (becameVerified && donorProfile.getAvailabilityStatus() == DonorProfile.AvailabilityStatus.AVAILABLE) {
            List<BloodRequest> searchingRequests = bloodRequestRepository.findByStatus(BloodRequest.Status.SEARCHING);
            log.info("Rematching {} searching requests after verifying available donor {}",
                searchingRequests.size(), donorProfile.getId());
            searchingRequests.forEach(bloodRequestService::matchAndNotifyDonors);
        }

        return mapToResponse(donorProfile);
    }

    private DonorProfileResponse mapToResponse(DonorProfile donorProfile) {
        User user = userRepository.findById(donorProfile.getUserId()).orElse(null);
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
                .role(user.getRole().toString())
                .smsNotificationsConsent(user.getSmsNotificationsConsent())
                .build() : null)
            .build();
    }
}
