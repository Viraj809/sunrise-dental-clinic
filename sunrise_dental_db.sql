-- ============================================================
-- Sunrise Dental Clinic - Database Schema
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
    contact           VARCHAR(15),
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

DROP PROCEDURE IF EXISTS sp_generate_bill$$
CREATE PROCEDURE sp_generate_bill(
    IN p_appointment_id INT
)
BEGIN
    DECLARE v_treatment_code VARCHAR(100);
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

    -- fn_get_treatment_cost already applies the 10% senior discount to v_treatment_fee,
    -- so v_discount is purely informational (base price - charged fee) and must NOT be
    -- subtracted again from the total. This keeps the calculation accurate (single 10%).
    SET v_discount = ROUND(v_base_price - v_treatment_fee, 2);

    SET v_total = v_consultation_fee + v_treatment_fee + v_tax;

    INSERT INTO bills (appointment_id, consultation_fee, treatment_fee, discount, tax, total_amount, payment_method, payment_status, issued_by, issued_at)
    VALUES (p_appointment_id, v_consultation_fee, v_treatment_fee, v_discount, v_tax, v_total, 'CASH', 'PENDING', 1, NOW())
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

-- Function: fn_calculate_age(p_patient_id)
-- Returns the precise age in whole years using TIMESTAMPDIFF (handles leap years / month boundaries).
DELIMITER $$

DROP FUNCTION IF EXISTS fn_calculate_age$$
CREATE FUNCTION fn_calculate_age(p_patient_id INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_dob DATE;
    DECLARE v_age INT DEFAULT 0;
    SELECT date_of_birth INTO v_dob FROM patients WHERE patient_id = p_patient_id;
    IF v_dob IS NOT NULL THEN
        SET v_age = TIMESTAMPDIFF(YEAR, v_dob, CURDATE());
    END IF;
    RETURN v_age;
END$$

-- Function: fn_get_treatment_cost
-- Returns the treatment base price, applying the 10% senior-citizen discount when the
-- patient is 60 years or older (uses fn_calculate_age for an exact age).
DROP FUNCTION IF EXISTS fn_get_treatment_cost$$
CREATE FUNCTION fn_get_treatment_cost(
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

    SET v_age = fn_calculate_age(p_patient_id);

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

DROP TRIGGER IF EXISTS trg_appointment_audit_insert$$
CREATE TRIGGER trg_appointment_audit_insert
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action_type, table_name, record_id, performed_by, old_value, new_value)
    VALUES ('INSERT', 'appointments', NEW.appointment_id, NEW.created_by, NULL,
            CONCAT('appointment_no=', NEW.appointment_no, ', patient_id=', NEW.patient_id,
                   ', dentist_id=', NEW.dentist_id, ', status=', NEW.status));
END$$

DROP TRIGGER IF EXISTS trg_appointment_audit_update$$
CREATE TRIGGER trg_appointment_audit_update
AFTER UPDATE ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action_type, table_name, record_id, performed_by, old_value, new_value)
    VALUES ('UPDATE', 'appointments', NEW.appointment_id, NEW.created_by,
            CONCAT('status=', OLD.status, ', date=', DATE_FORMAT(OLD.appointment_date, '%Y-%m-%d'), ', time=', TIME_FORMAT(OLD.appointment_time, '%H:%i')),
            CONCAT('status=', NEW.status, ', date=', DATE_FORMAT(NEW.appointment_date, '%Y-%m-%d'), ', time=', TIME_FORMAT(NEW.appointment_time, '%H:%i')));
END$$

DROP TRIGGER IF EXISTS trg_appointment_audit_delete$$
CREATE TRIGGER trg_appointment_audit_delete
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

-- NOTE: NEW.appointment_id is NULL on INSERT, so `appointment_id <> NEW.appointment_id`
-- evaluates to UNKNOWN and would wrongly suppress the conflict check. We therefore use
-- (appointment_id <> NEW.appointment_id OR NEW.appointment_id IS NULL) so INSERTs are
-- correctly validated against existing rows.
DROP TRIGGER IF EXISTS trg_prevent_double_booking_insert$$
CREATE TRIGGER trg_prevent_double_booking_insert
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM appointments
         WHERE dentist_id = NEW.dentist_id
           AND appointment_date = NEW.appointment_date
           AND appointment_time = NEW.appointment_time
           AND status NOT IN ('Cancelled')
           AND (appointment_id <> NEW.appointment_id OR NEW.appointment_id IS NULL)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Double booking detected: dentist already has an appointment at this date and time.';
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_prevent_double_booking_update$$
CREATE TRIGGER trg_prevent_double_booking_update
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

-- Staff (password_hash is BCrypt-hashed demo passwords)
INSERT INTO staff (name, email, contact, address, NIC, password_hash, role, shift_hours, is_active) VALUES
('System Admin', 'admin@sunrisedental.com', '0770000001', 'Colombo 07', '123456789V', '$2a$10$7dnupNlC1efH2b0prmbabeIgBN0KHXehX3HZ2Tbxlq6UtA1fZBElu', 'ADMIN', '08:00 - 18:00', TRUE),
('Receptionist One', 'reception@sunrisedental.com', '0770000002', 'Colombo 03', '987654321V', '$2a$10$wzVS.PmJfc9l9cqCAjQ5z.gcb96I9FzE7KUXVEcslL0p3Oli9Vdt6', 'RECEPTIONIST', '08:00 - 17:00', TRUE),
('Dr. Perera', 'perera@sunrisedental.com', '0770000003', 'Colombo 05', '456789123V', '$2a$10$1zSgjfnH95gqFqXkTCvJ6etuoX17I9gO4m8MckbcKDbrFZVqW4NA.', 'DENTIST', '09:00 - 17:00', TRUE) ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Dentists
INSERT INTO dentists (name, specialization, contact, email, available_days, is_active) VALUES
('Dr. Perera', 'General Dentistry', '0770000003', 'perera@sunrisedental.com', 'Mon,Tue,Wed,Thu,Fri', TRUE),
('Dr. Silva', 'Orthodontics', '0770000004', 'silva@sunrisedental.com', 'Mon,Wed,Fri', TRUE),
('Dr. Fernando', 'Oral Surgery', '0770000005', 'fernando@sunrisedental.com', 'Tue,Thu,Sat', TRUE) ON DUPLICATE KEY UPDATE name = VALUES(name);

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
('WISDOM', 'Wisdom Tooth Removal', 5000.00, 500.00, 'Surgical', 60, 'Wisdom tooth extraction consultation') ON DUPLICATE KEY UPDATE treatment_name = VALUES(treatment_name);

-- Dummy Patients
INSERT INTO patients (name, date_of_birth, gender, address, contact, email, NIC, blood_group, allergies) VALUES
('Kamal Perera', '1985-04-12', 'Male', '12, Galle Road, Colombo', '0712345678', 'kamal@gmail.com', '851030123V', 'O+', 'None'),
('Nimali Silva', '1990-08-25', 'Female', '45, Kandy Road, Kadawatha', '0779876543', 'nimali@yahoo.com', '907384123V', 'A+', 'Penicillin'),
('Sunil Fernando', '1960-02-15', 'Male', '78, Negombo Road, Wattala', '0723456789', 'sunil.f@hotmail.com', '600460123V', 'B-', 'Latex'),
('Amal Perera', '1995-11-05', 'Male', '34, Main Street, Gampaha', '0754321987', 'amal.p@gmail.com', '953100123V', 'AB+', 'None'),
('Kumari Silva', '1988-07-20', 'Female', '56, Temple Road, Kelaniya', '0787654321', 'kumari.s@yahoo.com', '887020123V', 'O-', 'Ibuprofen') ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Dummy Appointments (Some today, some past, some future)
-- Assume created_by = 2 (Receptionist)
-- We use ON DUPLICATE KEY UPDATE to make re-running the script idempotent.
INSERT INTO appointments (appointment_no, patient_id, dentist_id, treatment_type, appointment_date, appointment_time, status, notes, contact, created_by) VALUES
('SDC-2026-0001', 1, 1, 'CHKUP', CURDATE(), '09:00:00', 'Completed', 'Regular checkup', '0712345678', 2),
('SDC-2026-0002', 2, 2, 'BRACES', CURDATE(), '10:30:00', 'Scheduled', 'Braces adjustment', '0779876543', 2),
('SDC-2026-0003', 3, 3, 'EXTRACT', CURDATE(), '14:00:00', 'Confirmed', 'Wisdom tooth', '0723456789', 2),
('SDC-2026-0004', 4, 1, 'CLEANING', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', 'Scheduled', 'Cleaning', '0754321987', 2),
('SDC-2026-0005', 5, 2, 'WHITENING', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '11:00:00', 'Completed', 'Whitening', '0787654321', 2),
('SDC-2026-0006', 1, 3, 'ROOTCANAL', CURDATE(), '16:00:00', 'Cancelled', 'Patient sick', '0712345678', 2) ON DUPLICATE KEY UPDATE status = VALUES(status);

-- Additional patients (ids 6-15) so reports have meaningful volume
INSERT INTO patients (name, date_of_birth, gender, address, contact, email, NIC, blood_group, allergies) VALUES
('Deshan Jayasuriya',   '1992-03-10', 'Male',   '15, Templers Road, Nuwara Eliya', '0711111111', 'deshan.j@yahoo.com',     '923456789V', 'A+',  'None'),
('Vishmi Gunawardena',  '1970-09-09', 'Female', '22, Hill Street, Matara',        '0722222222', 'vishmi.g@gmail.com',      '704567891V', 'O+',  'None'),
('Nuwan Athapattu',     '1988-12-01', 'Male',   '9, Beach Road, Kalutara',        '0733333333', 'nuwan.a@yahoo.com',       '883456789V', 'B+',  'Dust'),
('Tharaka Rathnayake',  '1999-06-15', 'Female', '33, Lake View, Kurunegala',      '0744444444', 'tharaka.r@gmail.com',     '994567812V', 'AB+', 'None'),
('Buddhika Silva',      '1962-01-20', 'Male',   '50, Station Road, Galle',        '0755555555', 'buddhika.s@yahoo.com',    '624567813V', 'O-',  'None'),
('Ishara Bandara',      '1995-04-04', 'Male',   '12, Colombo Road, Panadura',     '0766666666', 'ishara.b@gmail.com',      '954567814V', 'A-',  'None'),
('Madhavi Perera',      '1983-07-07', 'Female', '88, Park Avenue, Colombo 05',    '0777777777', 'madhavi.p@yahoo.com',     '834567815V', 'B+',  'Penicillin'),
('Pasan Weerasinghe',   '2000-11-11', 'Male',   '5, Temple Lane, Avissawella',    '0788888888', 'pasan.w@gmail.com',       '004567816V', 'AB-', 'None'),
('Chamari Dissanayake', '1991-02-02', 'Female', '61, Green Path, Colombo 03',     '0799999999', 'chamari.d@yahoo.com',     '914567817V', 'O+',  'None'),
('Ravi Thalagala',      '1975-05-05', 'Male',   '27, Main Street, Horana',        '0700000000', 'ravi.t@gmail.com',        '754567818V', 'A+',  'None') ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Additional appointments spread over the last ~3 weeks (varied statuses & treatments)
INSERT INTO appointments (appointment_no, patient_id, dentist_id, treatment_type, appointment_date, appointment_time, status, notes, contact, created_by) VALUES
('SDC-2026-0101', 6,  1, 'CHKUP',     DATE_SUB(CURDATE(), INTERVAL 18 DAY), '09:00:00', 'Completed', 'Routine checkup',            '0711111111', 2),
('SDC-2026-0102', 7,  2, 'FILLING',   DATE_SUB(CURDATE(), INTERVAL 16 DAY), '10:00:00', 'Completed', 'Cavity filling (senior)',    '0722222222', 2),
('SDC-2026-0103', 8,  3, 'EXTRACT',   DATE_SUB(CURDATE(), INTERVAL 15 DAY), '14:00:00', 'Completed', 'Molar extraction',           '0733333333', 2),
('SDC-2026-0104', 9,  1, 'CLEANING',  DATE_SUB(CURDATE(), INTERVAL 14 DAY), '11:00:00', 'Completed', 'Scaling and polishing',       '0744444444', 2),
('SDC-2026-0105', 10, 2, 'ROOTCANAL', DATE_SUB(CURDATE(), INTERVAL 12 DAY), '09:30:00', 'Completed', 'Root canal session 1 (sr)',  '0755555555', 2),
('SDC-2026-0106', 11, 3, 'WHITENING', DATE_SUB(CURDATE(), INTERVAL 10 DAY), '15:00:00', 'Completed', 'Whitening',                  '0766666666', 2),
('SDC-2026-0107', 12, 1, 'CROWN',     DATE_SUB(CURDATE(), INTERVAL 8 DAY),  '13:00:00', 'Completed', 'Crown fitting',              '0777777777', 2),
('SDC-2026-0108', 13, 2, 'BRACES',    DATE_SUB(CURDATE(), INTERVAL 6 DAY),  '10:30:00', 'Confirmed', 'Braces adjustment',          '0788888888', 2),
('SDC-2026-0109', 14, 3, 'CHKUP',     DATE_SUB(CURDATE(), INTERVAL 5 DAY),  '09:00:00', 'Completed', 'Checkup',                    '0799999999', 2),
('SDC-2026-0110', 15, 1, 'FILLING',   DATE_SUB(CURDATE(), INTERVAL 4 DAY),  '16:00:00', 'Completed', 'Filling',                    '0700000000', 2),
('SDC-2026-0111', 6,  2, 'CLEANING',  DATE_SUB(CURDATE(), INTERVAL 3 DAY),  '11:30:00', 'Scheduled', 'Cleaning',                   '0711111111', 2),
('SDC-2026-0112', 7,  3, 'EXTRACT',   DATE_SUB(CURDATE(), INTERVAL 2 DAY),  '14:30:00', 'Cancelled', 'Patient cancelled',          '0722222222', 2),
('SDC-2026-0113', 8,  1, 'CLEANING',  DATE_SUB(CURDATE(), INTERVAL 1 DAY),  '09:00:00', 'Completed', 'Cleaning',                   '0733333333', 2),
('SDC-2026-0114', 9,  2, 'CHKUP',     CURDATE(),                            '11:00:00', 'Scheduled', 'Checkup',                    '0744444444', 2),
('SDC-2026-0115', 10, 3, 'FILLING',   CURDATE(),                            '15:00:00', 'Confirmed', 'Filling (senior)',            '0755555555', 2),
('SDC-2026-0116', 11, 1, 'WHITENING', DATE_ADD(CURDATE(), INTERVAL 1 DAY),  '10:00:00', 'Scheduled', 'Whitening',                  '0766666666', 2) ON DUPLICATE KEY UPDATE status = VALUES(status);

-- Generate dummy bills (using the stored procedure) for the COMPLETED appointments.
-- Referenced by appointment_no so the script stays idempotent regardless of auto-increment ids.
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0001'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0005'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0101'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0102'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0103'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0104'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0105'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0106'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0107'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0109'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0110'));
CALL sp_generate_bill((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0113'));

-- Vary payment methods / statuses so reports look realistic
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CARD'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0001');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CASH'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0005');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CARD'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0101');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CASH'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0102');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'INSURANCE' WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0103');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CASH'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0104');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CARD'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0105');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CASH'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0106');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'INSURANCE' WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0107');
UPDATE bills SET payment_status = 'PENDING',   payment_method = 'CASH'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0109');
UPDATE bills SET payment_status = 'PAID',      payment_method = 'CARD'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0110');
UPDATE bills SET payment_status = 'PENDING',   payment_method = 'CASH'     WHERE appointment_id = (SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0113');

-- Sample notifications (the Observer pattern also inserts these at runtime on create/update)
INSERT INTO notifications (appointment_id, channel, recipient, message, status) VALUES
((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0001'), 'EMAIL', 'kamal@gmail.com',     'Appointment SDC-2026-0001 confirmed', 'SENT'),
((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0005'), 'SMS',   '0787654321',          'Reminder: Whitening appt SDC-2026-0005', 'SENT'),
((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0102'), 'IN_APP','reception@sunrisedental.com', 'Senior discount applied to SDC-2026-0102', 'SENT'),
((SELECT appointment_id FROM appointments WHERE appointment_no = 'SDC-2026-0105'), 'EMAIL', 'buddhika.s@yahoo.com','Root canal SDC-2026-0105 scheduled', 'SENT') ON DUPLICATE KEY UPDATE message = VALUES(message);

-- ============================================================
-- 4.3 Additional Advanced DB Objects (reporting support)
-- ============================================================

-- Stored Procedure: sp_revenue_summary(p_period)
-- Centralises revenue reporting (daily / monthly / yearly) on the DB side.
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_revenue_summary$$
CREATE PROCEDURE sp_revenue_summary(IN p_period VARCHAR(10))
BEGIN
    IF p_period = 'daily' THEN
        SELECT COALESCE(SUM(total_amount), 0) AS revenue, COUNT(*) AS bills
          FROM bills WHERE DATE(issued_at) = CURDATE();
    ELSEIF p_period = 'monthly' THEN
        SELECT COALESCE(SUM(total_amount), 0) AS revenue, COUNT(*) AS bills
          FROM bills WHERE MONTH(issued_at) = MONTH(CURDATE()) AND YEAR(issued_at) = YEAR(CURDATE());
    ELSE
        SELECT COALESCE(SUM(total_amount), 0) AS revenue, COUNT(*) AS bills
          FROM bills WHERE YEAR(issued_at) = YEAR(CURDATE());
    END IF;
END$$

-- Stored Procedure: sp_daily_appointments(p_date)
-- Returns a pre-joined daily appointment list for the reports dashboard.
DROP PROCEDURE IF EXISTS sp_daily_appointments$$
CREATE PROCEDURE sp_daily_appointments(IN p_date DATE)
BEGIN
    SELECT a.appointment_no,
           p.name      AS patient_name,
           p.contact   AS patient_contact,
           d.name      AS dentist_name,
           a.treatment_type,
           a.appointment_time,
           a.status,
           a.notes
      FROM appointments a
      JOIN patients p ON a.patient_id = p.patient_id
      JOIN dentists d ON a.dentist_id = d.dentist_id
     WHERE a.appointment_date = p_date
     ORDER BY a.appointment_time;
END$$
DELIMITER ;

-- View: vw_monthly_revenue  (trend over months)
CREATE OR REPLACE VIEW vw_monthly_revenue AS
SELECT DATE_FORMAT(issued_at, '%Y-%m') AS month,
       COUNT(*)                         AS bills,
       SUM(total_amount)               AS revenue,
       SUM(discount)                   AS total_discount
FROM bills
GROUP BY DATE_FORMAT(issued_at, '%Y-%m');

-- View: vw_treatment_revenue  (treatment performance / popularity + revenue)
CREATE OR REPLACE VIEW vw_treatment_revenue AS
SELECT t.treatment_code,
       t.treatment_name,
       t.category,
       COUNT(a.appointment_id)                                   AS bookings,
       COALESCE(SUM(b.total_amount), 0)                          AS revenue
FROM appointments a
JOIN treatments t ON a.treatment_type = t.treatment_code
LEFT JOIN bills b ON b.appointment_id = a.appointment_id
GROUP BY t.treatment_code, t.treatment_name, t.category;

-- View: vw_dentist_performance  (dentist workload & completion rates)
CREATE OR REPLACE VIEW vw_dentist_performance AS
SELECT d.dentist_id,
       d.name,
       d.specialization,
       COUNT(a.appointment_id)                                             AS total_appointments,
       SUM(CASE WHEN a.status = 'Completed' THEN 1 ELSE 0 END)            AS completed,
       SUM(CASE WHEN a.status = 'Cancelled' THEN 1 ELSE 0 END)            AS cancelled
FROM dentists d
LEFT JOIN appointments a ON d.dentist_id = a.dentist_id
GROUP BY d.dentist_id, d.name, d.specialization;
