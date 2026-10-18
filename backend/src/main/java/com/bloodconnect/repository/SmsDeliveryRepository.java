package com.bloodconnect.repository;

import com.bloodconnect.domain.entity.SmsDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsDeliveryRepository extends JpaRepository<SmsDelivery, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
