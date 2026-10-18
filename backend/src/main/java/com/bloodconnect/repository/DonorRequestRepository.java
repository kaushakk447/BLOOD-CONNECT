package com.bloodconnect.repository;

import com.bloodconnect.domain.entity.DonorRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRequestRepository extends JpaRepository<DonorRequest, Long> {
    List<DonorRequest> findByDonorId(Long donorId);
    List<DonorRequest> findByBloodRequestId(Long requestId);
    List<DonorRequest> findByDonorIdAndStatus(Long donorId, DonorRequest.Status status);
    Optional<DonorRequest> findByBloodRequestIdAndDonorId(Long requestId, Long donorId);
    List<DonorRequest> findByBloodRequestIdAndStatus(Long requestId, DonorRequest.Status status);
}
