package com.bloodconnect.service;

import com.bloodconnect.config.SmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.Map;

/** Minimal Twilio adapter, isolated so tests never need provider credentials or network access. */
@Component
@RequiredArgsConstructor
public class TwilioSmsProviderClient {
    private final SmsProperties properties;
    private final RestTemplate restTemplate = createRestTemplate();

    public ProviderResponse send(String recipient, String body) {
        if (!"twilio".equalsIgnoreCase(properties.getProvider())) {
            throw new IllegalStateException("Unsupported SMS provider: " + properties.getProvider());
        }
        if (isBlank(properties.getAccountSid()) || isBlank(properties.getAuthToken()) || isBlank(properties.getFrom())) {
            throw new IllegalStateException("Twilio credentials and sender number must be configured");
        }
        if (isBlank(recipient) || !recipient.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new IllegalArgumentException("Recipient phone number must be in E.164 format for Twilio delivery");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(properties.getAccountSid(), properties.getAuthToken());
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", recipient);
        form.add("From", properties.getFrom());
        form.add("Body", body);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.twilio.com/2010-04-01/Accounts/" + properties.getAccountSid() + "/Messages.json",
                new HttpEntity<>(form, headers), Map.class
            );
            Object sid = response.getBody() == null ? null : response.getBody().get("sid");
            if (!response.getStatusCode().is2xxSuccessful() || sid == null) {
                throw new IllegalStateException("Twilio accepted the request without returning a message SID");
            }
            Object providerStatus = response.getBody().get("status");
            return new ProviderResponse(sid.toString(), providerStatus == null ? "accepted" : providerStatus.toString());
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException("Twilio rejected the request (HTTP " + exception.getRawStatusCode()
                + providerErrorCode(exception.getResponseBodyAsString()) + ")", exception);
        } catch (ResourceAccessException exception) {
            throw new IllegalStateException("Twilio could not be reached before the request timed out", exception);
        }
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(15_000);
        return new RestTemplate(factory);
    }

    private String providerErrorCode(String body) {
        if (body == null) return "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\\"code\\\"\\s*:\\s*(\\d+)").matcher(body);
        return matcher.find() ? ", Twilio error " + matcher.group(1) : "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Twilio accepts an SMS asynchronously; this is not a delivery confirmation. */
    public record ProviderResponse(String messageSid, String status) { }
}
