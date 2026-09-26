package com.bloodconnect.repository;

import com.bloodconnect.domain.entity.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRequesterId(Long requesterId);
    List<BloodRequest> findByStatus(BloodRequest.Status status);
    List<BloodRequest> findByStatusAndCreatedAtAfter(BloodRequest.Status status, LocalDateTime createdAfter);
    Optional<BloodRequest> findByIdAndRequesterId(Long id, Long requesterId);
}
