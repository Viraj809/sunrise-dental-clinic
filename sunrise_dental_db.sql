-- ============================================================
-- Sunrise Dental Clinic — Database Schema
-- Database: sunrise_dental_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS sunrise_dental_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sunrise_dental_db;

-- ============================================================
-- 4.1 Tables
-- ============================================================

CREATE TABLE IF NOT EXISTS staff (
    staff_id      INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    contact       VARCHAR(15)  NOT NULL,
    address       VARCHAR(255),
    NIC           VARCHAR(20)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN', 'RECEPTIONIST', 'DENTIST') NOT NULL,
    shift_hours   VARCHAR(50),
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dentists (
    dentist_id     INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    contact        VARCHAR(15)  NOT NULL,
    email          VARCHAR(100),
    available_days VARCHAR(100),
    is_active      BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS patients (
    patient_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender        ENUM('Male', 'Female', 'Other'),
    address       VARCHAR(255),
    contact       VARCHAR(15)  NOT NULL,
    email         VARCHAR(100),
    NIC           VARCHAR(20)  NOT NULL UNIQUE,
    blood_group   VARCHAR(5),
    allergies     VARCHAR(255),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS appointments (
    appointment_id    INT AUTO_INCREMENT PRIMARY KEY,
    appointment_no    VARCHAR(20) NOT NULL UNIQUE,
    patient_id        INT NOT NULL,
    dentist_id        INT NOT NULL,
    treatment_type    VARCHAR(100) NOT NULL,
    appointment_date  DATE NOT NULL,
    appointment_time  TIME NOT NULL,
    status            ENUM('Scheduled', 'Confirmed', 'Completed', 'Cancelled') DEFAULT 'Scheduled',
    notes             TEXT,
    created_by        INT NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT fk_appt_dentist FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id),
    CONSTRAINT fk_appt_created_by FOREIGN KEY (created_by) REFERENCES staff(staff_id)
);

CREATE TABLE IF NOT EXISTS treatments (
    treatment_id       INT AUTO_INCREMENT PRIMARY KEY,
    treatment_code     VARCHAR(20) NOT NULL UNIQUE,
    treatment_name     VARCHAR(100) NOT NULL,
    base_price         DECIMAL(10,2) NOT NULL,
    consultation_fee   DECIMAL(10,2) NOT NULL,
    category           VARCHAR(50),
    duration_minutes   INT,
    description        TEXT
);

CREATE TABLE IF NOT EXISTS bills (
    bill_id         INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id  INT NOT NULL UNIQUE,
    consultation_fee DECIMAL(10,2) NOT NULL,
    treatment_fee   DECIMAL(10,2) NOT NULL,
    discount        DECIMAL(10,2) DEFAULT 0.00,
    tax             DECIMAL(10,2) DEFAULT 0.00,
    total_amount    DECIMAL(10,2) NOT NULL,
    payment_method  ENUM('CASH', 'CARD', 'INSURANCE') DEFAULT 'CASH',
    payment_status  ENUM('PENDING', 'PAID', 'CANCELLED') DEFAULT 'PENDING',
    issued_by       INT NOT NULL,
    issued_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
    CONSTRAINT fk_bill_issued_by FOREIGN KEY (issued_by) REFERENCES staff(staff_id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    log_id         INT AUTO_INCREMENT PRIMARY KEY,
    action_type    ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    table_name     VARCHAR(50) NOT NULL,
    record_id      INT NOT NULL,
    performed_by   INT,
    performed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_value      TEXT,
    new_value      TEXT
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id  INT,
    channel         ENUM('EMAIL', 'SMS', 'IN_APP') NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    status          ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    sent_at         TIMESTAMP NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
);

-- ============================================================
-- 4.2 Advanced Database Features
-- ============================================================

-- Stored Procedure: sp_generate_bill
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS sp_generate_bill(
    IN p_appointment_id INT
)
BEGIN
    DECLARE v_treatment_code VARCHAR(20);
    DECLARE v_patient_id INT;
    DECLARE v_base_price DECIMAL(10,2);
    DECLARE v_consultation_fee DECIMAL(10,2);
    DECLARE v_discount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_tax DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_treatment_fee DECIMAL(10,2);

    SELECT a.treatment_type, a.patient_id
      INTO v_treatment_code, v_patient_id
      FROM appointments a
     WHERE a.appointment_id = p_appointment_id;

    SELECT base_price, consultation_fee
      INTO v_base_price, v_consultation_fee
      FROM treatments
     WHERE treatment_code = v_treatment_code;

    SET v_treatment_fee = fn_get_treatment_cost(v_treatment_code, v_patient_id);
    SET v_tax = ROUND((v_treatment_fee + v_consultation_fee) * 0.05, 2);

    IF (SELECT YEAR(CURDATE()) - YEAR(date_of_birth)
          FROM patients WHERE patient_id = v_patient_id) >= 60 THEN
        SET v_discount = ROUND(v_treatment_fee * 0.10, 2);
    END IF;

    SET v_total = v_consultation_fee + v_treatment_fee - v_discount + v_tax;

    INSERT INTO bills (appointment_id, consultation_fee, treatment_fee, discount, tax, total_amount, issued_by, issued_at)
    VALUES (p_appointment_id, v_consultation_fee, v_treatment_fee, v_discount, v_tax, v_total, 1, NOW())
    ON DUPLICATE KEY UPDATE
        consultation_fee = VALUES(consultation_fee),
        treatment_fee    = VALUES(treatment_fee),
        discount         = VALUES(discount),
        tax              = VALUES(tax),
        total_amount     = VALUES(total_amount),
        issued_at        = VALUES(issued_at);

    SELECT v_total AS total_amount;
END$$

DELIMITER ;

-- Function: fn_get_treatment_cost
DELIMITER $$

CREATE FUNCTION IF NOT EXISTS fn_get_treatment_cost(
    p_treatment_code VARCHAR(20),
    p_patient_id     INT
)
RETURNS DECIMAL(10,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_base_price DECIMAL(10,2);
    DECLARE v_age INT;
    DECLARE v_result DECIMAL(10,2);

    SELECT base_price INTO v_base_price
      FROM treatments
     WHERE treatment_code = p_treatment_code;

    SELECT YEAR(CURDATE()) - YEAR(date_of_birth) INTO v_age
      FROM patients
     WHERE patient_id = p_patient_id;

    IF v_age >= 60 THEN
        SET v_result = ROUND(v_base_price * 0.90, 2);
    ELSE
        SET v_result = v_base_price;
    END IF;

    RETURN IFNULL(v_result, 0.00);
END$$

DELIMITER ;

-- Trigger: trg_appointment_audit (AFTER INSERT/UPDATE/DELETE)
DELIMITER $$

CREATE TRIGGER IF NOT EXISTS trg_appointment_audit_insert
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action_type, table_name, record_id, performed_by, old_value, new_value)
    VALUES ('INSERT', 'appointments', NEW.appointment_id, NEW.created_by, NULL,
            CONCAT('appointment_no=', NEW.appointment_no, ', patient_id=', NEW.patient_id,
                   ', dentist_id=', NEW.dentist_id, ', status=', NEW.status));
END$$

CREATE TRIGGER IF NOT EXISTS trg_appointment_audit_update
AFTER UPDATE ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action_type, table_name, record_id, performed_by, old_value, new_value)
    VALUES ('UPDATE', 'appointments', NEW.appointment_id, NEW.created_by,
            CONCAT('status=', OLD.status, ', date=', DATE_FORMAT(OLD.appointment_date, '%Y-%m-%d'), ', time=', TIME_FORMAT(OLD.appointment_time, '%H:%i')),
            CONCAT('status=', NEW.status, ', date=', DATE_FORMAT(NEW.appointment_date, '%Y-%m-%d'), ', time=', TIME_FORMAT(NEW.appointment_time, '%H:%i')));
END$$

CREATE TRIGGER IF NOT EXISTS trg_appointment_audit_delete
AFTER DELETE ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action_type, table_name, record_id, performed_by, old_value, new_value)
    VALUES ('DELETE', 'appointments', OLD.appointment_id, OLD.created_by,
            CONCAT('appointment_no=', OLD.appointment_no, ', status=', OLD.status), NULL);
END$$

DELIMITER ;

-- Trigger: trg_prevent_double_booking (BEFORE INSERT/UPDATE)
DELIMITER $$

CREATE TRIGGER IF NOT EXISTS trg_prevent_double_booking_insert
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM appointments
         WHERE dentist_id = NEW.dentist_id
           AND appointment_date = NEW.appointment_date
           AND appointment_time = NEW.appointment_time
           AND status NOT IN ('Cancelled')
           AND appointment_id <> NEW.appointment_id
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Double booking detected: dentist already has an appointment at this date and time.';
    END IF;
END$$

CREATE TRIGGER IF NOT EXISTS trg_prevent_double_booking_update
BEFORE UPDATE ON appointments
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM appointments
         WHERE dentist_id = NEW.dentist_id
           AND appointment_date = NEW.appointment_date
           AND appointment_time = NEW.appointment_time
           AND status NOT IN ('Cancelled')
           AND appointment_id <> NEW.appointment_id
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Double booking detected: dentist already has an appointment at this date and time.';
    END IF;
END$$

DELIMITER ;

-- View: vw_daily_schedule
CREATE OR REPLACE VIEW vw_daily_schedule AS
SELECT 
    a.appointment_id,
    a.appointment_no,
    a.appointment_date,
    a.appointment_time,
    a.status,
    a.treatment_type,
    a.notes,
    p.patient_id,
    p.name AS patient_name,
    p.contact AS patient_contact,
    d.dentist_id,
    d.name AS dentist_name,
    d.specialization
FROM appointments a
JOIN patients p ON a.patient_id = p.patient_id
JOIN dentists d ON a.dentist_id = d.dentist_id;

-- ============================================================
-- Seed Data
-- ============================================================

-- Staff (password_hash is 'admin123' hashed with BCrypt — for demo only)
INSERT INTO staff (name, email, contact, address, NIC, password_hash, role, shift_hours, is_active) VALUES
('System Admin', 'admin@sunrisedental.com', '0770000001', 'Colombo 07', '123456789V', '$2a$10$rOjXqJQZ8QZ8QZ8QZ8QZ8O', 'ADMIN', '08:00 - 18:00', TRUE),
('Receptionist One', 'reception@sunrisedental.com', '0770000002', 'Colombo 03', '987654321V', '$2a$10$rOjXqJQZ8QZ8QZ8QZ8QZ8O', 'RECEPTIONIST', '08:00 - 17:00', TRUE),
('Dr. Perera', 'perera@sunrisedental.com', '0770000003', 'Colombo 05', '456789123V', '$2a$10$rOjXqJQZ8QZ8QZ8QZ8O', 'DENTIST', '09:00 - 17:00', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Dentists
INSERT INTO dentists (name, specialization, contact, email, available_days, is_active) VALUES
('Dr. Perera', 'General Dentistry', '0770000003', 'perera@sunrisedental.com', 'Mon,Tue,Wed,Thu,Fri', TRUE),
('Dr. Silva', 'Orthodontics', '0770000004', 'silva@sunrisedental.com', 'Mon,Wed,Fri', TRUE),
('Dr. Fernando', 'Oral Surgery', '0770000005', 'fernando@sunrisedental.com', 'Tue,Thu,Sat', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Treatments
INSERT INTO treatments (treatment_code, treatment_name, base_price, consultation_fee, category, duration_minutes, description) VALUES
('CHKUP', 'Dental Checkup', 1500.00, 500.00, 'General', 30, 'Routine dental examination'),
('EXTRACT', 'Tooth Extraction', 3000.00, 500.00, 'General', 45, 'Simple tooth extraction'),
('ROOTCANAL', 'Root Canal Treatment', 15000.00, 500.00, 'Specialist', 90, 'Root canal therapy'),
('FILLING', 'Dental Filling', 4000.00, 500.00, 'General', 45, 'Composite or amalgam filling'),
('CLEANING', 'Teeth Cleaning', 2500.00, 500.00, 'General', 30, 'Scaling and polishing'),
('WHITENING', 'Teeth Whitening', 8000.00, 500.00, 'Cosmetic', 60, 'Professional teeth whitening'),
('CROWN', 'Dental Crown', 25000.00, 500.00, 'Restorative', 60, 'Crown placement'),
('BRACES', 'Braces (Orthodontics)', 80000.00, 1000.00, 'Orthodontics', 30, 'Orthodontic braces consultation'),
('IMPLANT', 'Dental Implant', 60000.00, 1000.00, 'Surgical', 90, 'Dental implant consultation'),
('WISDOM', 'Wisdom Tooth Removal', 5000.00, 500.00, 'Surgical', 60, 'Wisdom tooth extraction consultation')
ON DUPLICATE KEY UPDATE treatment_name = VALUES(treatment_name);
