package com.bloodconnect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.sms")
public class SmsProperties {
    /** Master switch. Disabled deployments persist a SKIPPED delivery, never send. */
    private boolean enabled = false;
    /** Safe default for local development and automated tests. */
    private boolean mockMode = true;
    private String provider = "twilio";
    private String accountSid;
    private String authToken;
    private String from;
}
