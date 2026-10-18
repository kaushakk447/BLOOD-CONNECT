package com.bloodconnect.service;

import com.bloodconnect.config.SmsProperties;
import com.bloodconnect.domain.entity.SmsDelivery;
import com.bloodconnect.domain.entity.User;
import com.bloodconnect.repository.SmsDeliveryRepository;
import com.bloodconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsNotificationServiceTest {
    @Mock private SmsDeliveryRepository deliveries;
    @Mock private UserRepository users;
    @Mock private TwilioSmsProviderClient twilio;
    private SmsNotificationService service;

    @BeforeEach
    void setUp() {
        SmsProperties properties = new SmsProperties();
        properties.setEnabled(true);
        properties.setMockMode(true);
        service = new SmsNotificationService(deliveries, users, properties, twilio);
        when(deliveries.save(any(SmsDelivery.class))).thenAnswer(invocation -> {
            SmsDelivery delivery = invocation.getArgument(0);
            if (delivery.getId() == null) delivery.setId(1L);
            return delivery;
        });
    }

    @Test
    void mockModePersistsDeliveryWithoutCallingProvider() {
        when(deliveries.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(users.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).phone("9999999999")
            .smsNotificationsConsent(true).build()));

        service.notifyDonorMatch(2L, 10L);

        ArgumentCaptor<SmsDelivery> delivery = ArgumentCaptor.forClass(SmsDelivery.class);
        verify(deliveries, times(2)).save(delivery.capture());
        assertEquals(SmsDelivery.Status.MOCKED, delivery.getAllValues().get(1).getStatus());
        verifyNoInteractions(twilio);
    }

    @Test
    void consentIsRequiredAndDuplicateKeysAreSuppressed() {
        when(deliveries.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(users.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).phone("9999999999")
            .smsNotificationsConsent(false).build()));
        service.notifyDonorMatch(2L, 10L);
        ArgumentCaptor<SmsDelivery> delivery = ArgumentCaptor.forClass(SmsDelivery.class);
        verify(deliveries, times(2)).save(delivery.capture());
        assertEquals(SmsDelivery.Status.SKIPPED, delivery.getAllValues().get(1).getStatus());

        reset(deliveries, users, twilio);
        when(deliveries.existsByIdempotencyKey(anyString())).thenReturn(true);
        service.notifyDonorMatch(2L, 10L);
        verify(deliveries, never()).save(any());
        verifyNoInteractions(users, twilio);
    }
}
