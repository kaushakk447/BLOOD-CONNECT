ALTER TABLE users
    ADD COLUMN IF NOT EXISTS sms_notifications_consent BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE sms_deliveries (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    related_entity_type VARCHAR(50) NOT NULL,
    related_entity_id BIGINT NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_message_id VARCHAR(255),
    error_message VARCHAR(1000),
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sms_deliveries_recipient ON sms_deliveries(recipient_user_id);
CREATE INDEX idx_sms_deliveries_status ON sms_deliveries(status);
