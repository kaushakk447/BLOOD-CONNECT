package com.bloodconnect.repository;

import com.bloodconnect.domain.entity.DonorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {
    Optional<DonorProfile> findByUserId(Long userId);

    List<DonorProfile> findByBloodGroupInAndVerificationStatusAndAvailabilityStatus(
        List<DonorProfile.BloodGroup> bloodGroups,
        DonorProfile.VerificationStatus verificationStatus,
        DonorProfile.AvailabilityStatus availabilityStatus
    );

    List<DonorProfile> findByVerificationStatus(DonorProfile.VerificationStatus status);
}
