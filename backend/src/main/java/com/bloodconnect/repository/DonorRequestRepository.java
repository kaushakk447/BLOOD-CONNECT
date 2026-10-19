package com.bloodconnect.repository;

import com.bloodconnect.domain.entity.DonorRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRequestRepository extends JpaRepository<DonorRequest, Long> {
    List<DonorRequest> findByDonorId(Long donorId);
    List<DonorRequest> findByBloodRequestId(Long requestId);
    List<DonorRequest> findByDonorIdAndStatus(Long donorId, DonorRequest.Status status);
    Optional<DonorRequest> findByBloodRequestIdAndDonorId(Long requestId, Long donorId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select donorRequest from DonorRequest donorRequest where donorRequest.id = :id")
    Optional<DonorRequest> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    List<DonorRequest> findByBloodRequestIdAndStatus(Long requestId, DonorRequest.Status status);
    long countByBloodRequestId(Long requestId);
    long countByBloodRequestIdAndStatusIn(Long requestId, List<DonorRequest.Status> statuses);
}
