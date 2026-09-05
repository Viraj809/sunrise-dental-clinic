-- H2-compatible schema for Sunrise Dental Clinic tests
-- Parent tables must be created before child tables due to foreign keys

DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS dentist_available_days;
DROP TABLE IF EXISTS dentist_schedule;
DROP TABLE IF EXISTS treatments;
DROP TABLE IF EXISTS dentists;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS staff;

-- Staff (parent table for appointments, bills, notices)
CREATE TABLE staff (
    staff_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    contact VARCHAR(15) NOT NULL,
    address VARCHAR(255) DEFAULT NULL,
    NIC VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    shift_hours VARCHAR(50) DEFAULT NULL,
    is_active TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (staff_id),
    UNIQUE (email),
    UNIQUE (NIC)
);

-- Patients (parent table for appointments)
CREATE TABLE patients (
    patient_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE DEFAULT NULL,
    gender VARCHAR(20) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    contact VARCHAR(15) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    NIC VARCHAR(20) NOT NULL,
    allergies VARCHAR(255) DEFAULT NULL,
    is_active TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id),
    UNIQUE (contact),
    UNIQUE (NIC),
    UNIQUE (email)
);

-- Dentists (parent table for appointments, schedules)
CREATE TABLE dentists (
    dentist_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) DEFAULT NULL,
    contact VARCHAR(15) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    NIC VARCHAR(20) DEFAULT NULL,
    password VARCHAR(255) DEFAULT NULL,
    available_days VARCHAR(100) DEFAULT NULL,
    is_active TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (dentist_id),
    UNIQUE (contact),
    UNIQUE (email),
    UNIQUE (NIC)
);

-- Treatments
CREATE TABLE treatments (
    treatment_id INT NOT NULL AUTO_INCREMENT,
    treatment_code VARCHAR(20) NOT NULL,
    treatment_name VARCHAR(100) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) DEFAULT NULL,
    duration_minutes INT DEFAULT NULL,
    description TEXT,
    PRIMARY KEY (treatment_id),
    UNIQUE (treatment_code)
);

-- Dentist Available Days (parent: dentists)
CREATE TABLE dentist_available_days (
    id INT NOT NULL AUTO_INCREMENT,
    dentist_id INT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (dentist_id, day_of_week),
    CONSTRAINT fk_dad_dentist FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id) ON DELETE CASCADE
);

-- Dentist Schedule (parent: dentists)
CREATE TABLE dentist_schedule (
    schedule_id INT NOT NULL AUTO_INCREMENT,
    dentist_id INT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    availability_status VARCHAR(20) DEFAULT 'Available',
    unavailable_date DATE DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (schedule_id),
    UNIQUE (dentist_id, day_of_week),
    CONSTRAINT fk_schedule_dentist FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id) ON DELETE CASCADE
);

-- Appointments (parents: staff, dentists, patients)
CREATE TABLE appointments (
    appointment_id INT NOT NULL AUTO_INCREMENT,
    appointment_no VARCHAR(20) NOT NULL,
    patient_id INT NOT NULL,
    dentist_id INT NOT NULL,
    treatment_type VARCHAR(100) NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    appointment_type VARCHAR(20) NOT NULL DEFAULT 'Consultation',
    status VARCHAR(30) DEFAULT 'Pending',
    notes TEXT,
    contact VARCHAR(15) DEFAULT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (appointment_id),
    UNIQUE (appointment_no),
    CONSTRAINT fk_appt_created_by FOREIGN KEY (created_by) REFERENCES staff(staff_id),
    CONSTRAINT fk_appt_dentist FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- Bills (parents: appointments, staff)
CREATE TABLE bills (
    bill_id INT NOT NULL AUTO_INCREMENT,
    appointment_id INT NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    treatment_fee DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,
    tax DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) DEFAULT 'CASH',
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    issued_by INT NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (bill_id),
    UNIQUE (appointment_id),
    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
    CONSTRAINT fk_bill_issued_by FOREIGN KEY (issued_by) REFERENCES staff(staff_id)
);

-- Notifications (parent: appointments)
CREATE TABLE notifications (
    notification_id INT NOT NULL AUTO_INCREMENT,
    user_id INT DEFAULT NULL,
    appointment_id INT DEFAULT NULL,
    title VARCHAR(255) DEFAULT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'IN_APP',
    recipient VARCHAR(255) DEFAULT NULL,
    notification_type VARCHAR(50) DEFAULT 'GENERAL',
    message TEXT NOT NULL,
    is_read TINYINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    CONSTRAINT fk_notif_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
);

-- Notices (parent: staff)
CREATE TABLE notices (
    notice_id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'General',
    target_role VARCHAR(50) NOT NULL DEFAULT 'ALL',
    target_dentist_id INT DEFAULT NULL,
    publish_date DATE DEFAULT NULL,
    expiry_date DATE DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'Published',
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notice_id),
    CONSTRAINT fk_notice_created_by FOREIGN KEY (created_by) REFERENCES staff(staff_id) ON DELETE CASCADE
);
