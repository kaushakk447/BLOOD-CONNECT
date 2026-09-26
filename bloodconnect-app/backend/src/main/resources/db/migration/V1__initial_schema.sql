-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    account_status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_account_status ON users(account_status);

-- Create donor_profiles table
CREATE TABLE donor_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blood_group VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    weight_kg DECIMAL(5, 2) NOT NULL,
    location VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    availability_status VARCHAR(50) DEFAULT 'UNAVAILABLE' NOT NULL,
    last_donation_date DATE NULL,
    donation_count INT DEFAULT 0,
    verification_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    allow_location_sharing BOOLEAN DEFAULT FALSE,
    push_notifications_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_donor_profiles_blood_group ON donor_profiles(blood_group);
CREATE INDEX idx_donor_profiles_verification_status ON donor_profiles(verification_status);
CREATE INDEX idx_donor_profiles_availability_status ON donor_profiles(availability_status);
CREATE INDEX idx_donor_profiles_location ON donor_profiles(latitude, longitude);

-- Create hospitals table
CREATE TABLE hospitals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hospital_name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    location VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone_number VARCHAR(20) NOT NULL,
    license_number VARCHAR(100) UNIQUE,
    verification_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    verified_by BIGINT NULL REFERENCES users(id),
    verified_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_hospitals_verification_status ON hospitals(verification_status);
CREATE INDEX idx_hospitals_location ON hospitals(latitude, longitude);

-- Create blood_requests table
CREATE TABLE blood_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hospital_id BIGINT NULL REFERENCES hospitals(id),
    blood_group VARCHAR(50) NOT NULL,
    units_required INT NOT NULL,
    urgency VARCHAR(50) DEFAULT 'HIGH' NOT NULL,
    location VARCHAR(500) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN' NOT NULL,
    description TEXT,
    contact_number VARCHAR(20) NOT NULL,
    required_datetime TIMESTAMP NOT NULL,
    search_radius_km INT DEFAULT 50,
    units_fulfilled INT DEFAULT 0,
    donor_count_notified INT DEFAULT 0,
    donor_count_responded INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL
);

CREATE INDEX idx_blood_requests_status ON blood_requests(status);
CREATE INDEX idx_blood_requests_blood_group ON blood_requests(blood_group);
CREATE INDEX idx_blood_requests_location ON blood_requests(latitude, longitude);
CREATE INDEX idx_blood_requests_urgency ON blood_requests(urgency);
CREATE INDEX idx_blood_requests_created_at ON blood_requests(created_at);

-- Create donor_requests table (matches donors to blood requests)
CREATE TABLE donor_requests (
    id BIGSERIAL PRIMARY KEY,
    blood_request_id BIGINT NOT NULL REFERENCES blood_requests(id) ON DELETE CASCADE,
    donor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    distance_km DECIMAL(8, 3),
    status VARCHAR(50) DEFAULT 'NOTIFIED' NOT NULL,
    notified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    responded_at TIMESTAMP NULL,
    connection_details TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE(blood_request_id, donor_id)
);

CREATE INDEX idx_donor_requests_status ON donor_requests(status);
CREATE INDEX idx_donor_requests_notified_at ON donor_requests(notified_at);
CREATE INDEX idx_donor_requests_donor_id ON donor_requests(donor_id);

-- Create notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Create audit_logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(50) DEFAULT 'SUCCESS' NOT NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
