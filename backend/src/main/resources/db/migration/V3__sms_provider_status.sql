ALTER TABLE sms_deliveries
    ADD COLUMN IF NOT EXISTS provider_status VARCHAR(50);
