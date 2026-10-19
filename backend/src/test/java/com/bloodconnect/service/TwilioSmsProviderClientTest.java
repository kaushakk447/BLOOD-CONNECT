package com.bloodconnect.service;

import com.bloodconnect.config.SmsProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TwilioSmsProviderClientTest {

    @Test
    void missingConfigurationFailsBeforeAnyNetworkRequest() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> new TwilioSmsProviderClient(new SmsProperties()).send("+919999999999", "test"));

        assertEquals("Twilio credentials and sender number must be configured", error.getMessage());
    }

    @Test
    void invalidRecipientFailsBeforeAnyNetworkRequest() {
        SmsProperties properties = configuredProperties();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> new TwilioSmsProviderClient(properties).send("9999999999", "test"));

        assertEquals("Recipient phone number must be in E.164 format for Twilio delivery", error.getMessage());
    }

    private SmsProperties configuredProperties() {
        SmsProperties properties = new SmsProperties();
        properties.setAccountSid("ACtest");
        properties.setAuthToken("token");
        properties.setFrom("+12025550123");
        return properties;
    }
}
