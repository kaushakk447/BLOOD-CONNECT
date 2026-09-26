package com.bloodconnect.service;

import com.bloodconnect.domain.entity.DonorProfile;
import com.bloodconnect.repository.DonorProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MatchingService {
    
    @Autowired
    private DonorProfileRepository donorProfileRepository;
    
    private static final Map<DonorProfile.BloodGroup, List<DonorProfile.BloodGroup>> COMPATIBILITY_MAP = Map.ofEntries(
        Map.entry(DonorProfile.BloodGroup.O_NEGATIVE, Arrays.asList(DonorProfile.BloodGroup.O_NEGATIVE)),
        Map.entry(DonorProfile.BloodGroup.O_POSITIVE, Arrays.asList(
            DonorProfile.BloodGroup.O_POSITIVE,
            DonorProfile.BloodGroup.O_NEGATIVE
        )),
        Map.entry(DonorProfile.BloodGroup.A_NEGATIVE, Arrays.asList(
            DonorProfile.BloodGroup.A_NEGATIVE,
            DonorProfile.BloodGroup.O_NEGATIVE
        )),
        Map.entry(DonorProfile.BloodGroup.A_POSITIVE, Arrays.asList(
            DonorProfile.BloodGroup.A_POSITIVE,
            DonorProfile.BloodGroup.A_NEGATIVE,
            DonorProfile.BloodGroup.O_POSITIVE,
            DonorProfile.BloodGroup.O_NEGATIVE
        )),
        Map.entry(DonorProfile.BloodGroup.B_NEGATIVE, Arrays.asList(
            DonorProfile.BloodGroup.B_NEGATIVE,
            DonorProfile.BloodGroup.O_NEGATIVE
        )),
        Map.entry(DonorProfile.BloodGroup.B_POSITIVE, Arrays.asList(
            DonorProfile.BloodGroup.B_POSITIVE,
            DonorProfile.BloodGroup.B_NEGATIVE,
            DonorProfile.BloodGroup.O_POSITIVE,
            DonorProfile.BloodGroup.O_NEGATIVE
        )),
        Map.entry(DonorProfile.BloodGroup.AB_NEGATIVE, Arrays.asList(
            DonorProfile.BloodGroup.AB_NEGATIVE,
            DonorProfile.BloodGroup.A_NEGATIVE,
            DonorProfile.BloodGroup.B_NEGATIVE,
            DonorProfile.BloodGroup.O_NEGATIVE
        )),
        Map.entry(DonorProfile.BloodGroup.AB_POSITIVE, Arrays.asList(
            DonorProfile.BloodGroup.AB_POSITIVE,
            DonorProfile.BloodGroup.AB_NEGATIVE,
            DonorProfile.BloodGroup.A_POSITIVE,
            DonorProfile.BloodGroup.A_NEGATIVE,
            DonorProfile.BloodGroup.B_POSITIVE,
            DonorProfile.BloodGroup.B_NEGATIVE,
            DonorProfile.BloodGroup.O_POSITIVE,
            DonorProfile.BloodGroup.O_NEGATIVE
        ))
    );
    
    public List<DonorProfile> findNearbyCompatibleDonors(
        DonorProfile.BloodGroup bloodGroup,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusKm
    ) {
        log.info("Finding nearby compatible donors for blood group: {}", bloodGroup);
        
        // Get compatible blood groups
        List<DonorProfile.BloodGroup> compatibleGroups = COMPATIBILITY_MAP.get(bloodGroup);
        if (compatibleGroups == null) {
            compatibleGroups = new ArrayList<>();
        }
        
        // Find all verified and available donors with compatible blood groups
        List<DonorProfile> donors = donorProfileRepository.findByBloodGroupInAndVerificationStatusAndAvailabilityStatus(
            compatibleGroups,
            DonorProfile.VerificationStatus.VERIFIED,
            DonorProfile.AvailabilityStatus.AVAILABLE
        );
        
        // Filter by distance and sort by proximity
        return donors.stream()
            .filter(donor -> donor.getLatitude() != null && donor.getLongitude() != null)
            .filter(donor -> calculateDistance(latitude, longitude, donor.getLatitude(), donor.getLongitude()) <= radiusKm)
            .sorted((d1, d2) -> {
                double dist1 = calculateDistance(latitude, longitude, d1.getLatitude(), d1.getLongitude());
                double dist2 = calculateDistance(latitude, longitude, d2.getLatitude(), d2.getLongitude());
                return Double.compare(dist1, dist2);
            })
            .collect(Collectors.toList());
    }
    
    public List<DonorProfile.BloodGroup> getCompatibleBloodGroups(DonorProfile.BloodGroup bloodGroup) {
        return COMPATIBILITY_MAP.getOrDefault(bloodGroup, new ArrayList<>());
    }
    
    public double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
