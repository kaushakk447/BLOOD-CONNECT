package com.bloodconnect.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsConfigurationTest {

    @Test
    void applicationConfigurationUsesCanonicalTwilioEnvironmentVariables() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            String yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(yaml.contains("${TWILIO_ACCOUNT_SID:}"));
            assertTrue(yaml.contains("${TWILIO_AUTH_TOKEN:}"));
            assertTrue(yaml.contains("${TWILIO_FROM_NUMBER:}"));
            assertTrue(yaml.contains("${SMS_ENABLED:false}"));
            assertTrue(yaml.contains("${SMS_MOCK_MODE:true}"));
            assertFalse(yaml.contains("SMS_TWILIO_"));
        }
    }
}
