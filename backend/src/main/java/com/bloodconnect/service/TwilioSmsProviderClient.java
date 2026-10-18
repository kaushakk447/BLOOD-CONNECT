package com.bloodconnect.service;

import com.bloodconnect.config.SmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/** Minimal Twilio adapter, isolated so tests never need provider credentials or network access. */
@Component
@RequiredArgsConstructor
public class TwilioSmsProviderClient {
    private final SmsProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public String send(String recipient, String body) {
        if (!"twilio".equalsIgnoreCase(properties.getProvider())) {
            throw new IllegalStateException("Unsupported SMS provider: " + properties.getProvider());
        }
        if (isBlank(properties.getAccountSid()) || isBlank(properties.getAuthToken()) || isBlank(properties.getFrom())) {
            throw new IllegalStateException("Twilio credentials and sender number must be configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(properties.getAccountSid(), properties.getAuthToken());
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", recipient);
        form.add("From", properties.getFrom());
        form.add("Body", body);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://api.twilio.com/2010-04-01/Accounts/" + properties.getAccountSid() + "/Messages.json",
            new HttpEntity<>(form, headers), Map.class
        );
        Object sid = response.getBody() == null ? null : response.getBody().get("sid");
        if (!response.getStatusCode().is2xxSuccessful() || sid == null) {
            throw new IllegalStateException("Twilio did not return a message id");
        }
        return sid.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
