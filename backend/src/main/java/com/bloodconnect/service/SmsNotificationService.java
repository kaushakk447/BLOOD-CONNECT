package com.bloodconnect.service;

import com.bloodconnect.config.SmsProperties;
import com.bloodconnect.domain.entity.SmsDelivery;
import com.bloodconnect.domain.entity.User;
import com.bloodconnect.repository.SmsDeliveryRepository;
import com.bloodconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationService {
    private final SmsDeliveryRepository smsDeliveryRepository;
    private final UserRepository userRepository;
    private final SmsProperties properties;
    private final TwilioSmsProviderClient twilioSmsProviderClient;

    @Transactional
    public void notifyDonorMatch(Long recipientUserId, Long bloodRequestId) {
        deliver(recipientUserId, SmsDelivery.EventType.DONOR_MATCH, "BloodRequest", bloodRequestId,
            "BloodConnect: a compatible blood request is available. Sign in to review and respond.");
    }

    @Transactional
    public void notifyDonorResponse(Long recipientUserId, Long donorRequestId, boolean accepted) {
        String response = accepted ? "accepted" : "declined";
        deliver(recipientUserId, SmsDelivery.EventType.DONOR_RESPONSE, "DonorRequest", donorRequestId,
            "BloodConnect: a donor has " + response + " your blood request. Sign in for details.");
    }

    private void deliver(Long recipientUserId, SmsDelivery.EventType eventType, String entityType, Long entityId, String body) {
        String key = eventType + ":" + entityType + ":" + entityId + ":" + recipientUserId;
        if (smsDeliveryRepository.existsByIdempotencyKey(key)) {
            log.info("Suppressing duplicate SMS delivery {}", key);
            return;
        }

        User recipient = userRepository.findById(recipientUserId).orElse(null);
        SmsDelivery delivery = SmsDelivery.builder()
            .recipientUserId(recipientUserId)
            .eventType(eventType)
            .relatedEntityType(entityType)
            .relatedEntityId(entityId)
            .idempotencyKey(key)
            .status(SmsDelivery.Status.PENDING)
            .provider(properties.getProvider())
            .build();
        delivery = smsDeliveryRepository.save(delivery);

        if (recipient == null || !Boolean.TRUE.equals(recipient.getSmsNotificationsConsent())) {
            delivery.setStatus(SmsDelivery.Status.SKIPPED);
            delivery.setErrorMessage("Recipient has not consented to SMS notifications");
            smsDeliveryRepository.save(delivery);
            return;
        }
        if (!properties.isEnabled()) {
            delivery.setStatus(SmsDelivery.Status.SKIPPED);
            delivery.setErrorMessage("SMS delivery is disabled");
            smsDeliveryRepository.save(delivery);
            return;
        }
        if (properties.isMockMode()) {
            delivery.setStatus(SmsDelivery.Status.MOCKED);
            delivery.setSentAt(LocalDateTime.now());
            delivery.setAttemptCount(1);
            smsDeliveryRepository.save(delivery);
            return;
        }

        try {
            delivery.setAttemptCount(1);
            TwilioSmsProviderClient.ProviderResponse providerResponse = twilioSmsProviderClient.send(recipient.getPhone(), body);
            delivery.setProviderMessageId(providerResponse.messageSid());
            delivery.setProviderStatus(providerResponse.status());
            delivery.setStatus(SmsDelivery.Status.SENT);
            delivery.setSentAt(LocalDateTime.now());
        } catch (RuntimeException exception) {
            log.warn("SMS delivery {} failed: {}", delivery.getId(), exception.getMessage());
            delivery.setStatus(SmsDelivery.Status.FAILED);
            delivery.setErrorMessage(truncate(exception.getMessage()));
        }
        smsDeliveryRepository.save(delivery);
    }

    private String truncate(String message) {
        if (message == null) return "Unknown provider error";
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
