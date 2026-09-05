-- Test data for Sunrise Dental Clinic H2 tests
-- Explicit IDs used to match JUnit test expectations

-- Staff (tests reference by email/NIC, created_by=15 for notices/appointments)
INSERT INTO staff (staff_id, name, email, contact, address, NIC, password, role, shift_hours, is_active) VALUES
(15, 'Ashan Perera',      'ashan@sunrisedental.lk',      '0711234501', 'No. 5, Galle Road, Colombo 03',     '199001012345V', 'Admin@123',     'SYSTEM_ADMIN', '08:00-17:00', 1),
(16, 'Nimali Fernando',   'nimali@sunrisedental.lk',     '0711234502', 'No. 12, Baseline Road, Colombo 09', '199205054321V', 'Admin@123',     'ADMIN',        '08:00-17:00', 1),
(17, 'Roshan Silva',      'roshan@sunrisedental.lk',     '0711234503', 'No. 8, Duplication Road, Colombo 03','198803033456V', 'Admin@123',     'ADMIN',        '09:00-18:00', 1),
(18, 'Dilini Rajapaksa',  'dilini@sunrisedental.lk',     '0711234504', 'No. 22, High Level Road, Nugegoda', '199507074567V', 'Reception@123', 'RECEPTIONIST', '08:00-16:00', 1);

-- Patients (tests reference by NIC/email, need 21+ for appointment tests)
INSERT INTO patients (patient_id, name, date_of_birth, gender, address, contact, email, NIC, allergies, is_active) VALUES
(1,  'Amara Silva',        '1990-04-15', 'Female', 'No. 14, Galle Road, Panadura',          '0723456001', 'amara.silva@gmail.com',      '199004152201V', 'Penicillin',         1),
(2,  'Lakshan Fernando',   '1985-11-22', 'Male',   'No. 7, Station Road, Moratuwa',         '0723456002', 'lakshan.f@gmail.com',        '198511227382V', NULL,                 1),
(3,  'Priya Dissanayake',  '1998-07-03', 'Female', 'No. 3, Temple Road, Piliyandala',       '0723456003', 'priya.d@gmail.com',          '199807033456V', 'Latex',              1),
(4,  'Ruchira Jayawardena','1975-02-18', 'Male',   'No. 22, Kandy Road, Kegalle',           '0723456004', 'ruchira.j@yahoo.com',        '197502184567V', NULL,                 1),
(5,  'Sanduni Madushani',  '2002-09-10', 'Female', 'No. 5, Main Street, Horana',            '0723456005', 'sanduni.m@gmail.com',        '200209105678V', 'Aspirin',            1),
(6,  'Chaminda Rathnayake','1968-06-25', 'Male',   'No. 88, High Level Road, Nugegoda',     '0723456006', 'chaminda.r@gmail.com',       '196806256789V', NULL,                 1),
(7,  'Niluka Perera',      '1993-12-01', 'Female', 'No. 11, Park Avenue, Rajagiriya',       '0723456007', 'niluka.p@hotmail.com',       '199312017890V', NULL,                 1),
(8,  'Isuru Bandara',      '1988-03-14', 'Male',   'No. 34, Baseline Road, Colombo 09',     '0723456008', 'isuru.b@gmail.com',          '198803148901V', 'Ibuprofen',          1),
(9,  'Hasini Rodrigo',     '2000-08-27', 'Female', 'No. 6, Lake Road, Nugegoda',            '0723456009', 'hasini.r@gmail.com',         '200008279012V', NULL,                 1),
(10, 'Tharaka Wijesekara', '1979-01-30', 'Male',   'No. 19, Independence Avenue, Colombo 07','0723456010', 'tharaka.w@gmail.com',       '197901301234V', NULL,                 1),
(11, 'Madushika Samarasinghe','1995-05-20','Female','No. 2, Hospital Road, Maharagama',     '0723456011', 'madushika.s@gmail.com',      '199505202345V', 'Sulfa drugs',        1),
(12, 'Dinesh Kumara',      '1982-10-08', 'Male',   'No. 67, Duplication Road, Colombo 03',  '0723456012', 'dinesh.k@gmail.com',         '198210083456V', NULL,                 1),
(13, 'Kavindi Abeysinghe', '2001-04-16', 'Female', 'No. 15, Flower Road, Colombo 07',       '0723456013', 'kavindi.a@gmail.com',        '200104164567V', NULL,                 1),
(14, 'Roshan Senanayake',  '1970-07-22', 'Male',   'No. 33, Union Place, Colombo 02',       '0723456014', 'roshan.s@yahoo.com',         '197007225678V', 'Codeine',            1),
(15, 'Thilini Weerasinghe','1997-03-05', 'Female', 'No. 9, Elvitigala Mawatha, Colombo 08', '0723456015', 'thilini.w@gmail.com',        '199703056789V', NULL,                 1),
(16, 'Nimal Gamage',       '1960-09-14', 'Male',   'No. 44, Main Street, Kotte',            '0723456016', 'nimal.g@gmail.com',          '196009147890V', NULL,                 1),
(17, 'Anusha Karunaratne', '1992-11-28', 'Female', 'No. 8, Gothami Road, Colombo 10',       '0723456017', 'anusha.k@gmail.com',         '199211288901V', 'Amoxicillin',        1),
(18, 'Mahesh Liyanage',    '1987-06-11', 'Male',   'No. 21, Norris Canal Road, Colombo 10', '0723456018', 'mahesh.l@gmail.com',         '198706119012V', NULL,                 1),
(19, 'Iresha Dissanayake', '2003-02-24', 'Female', 'No. 12, Denzil Kobbekaduwa Mw, Colombo 07','0723456019','iresha.d@gmail.com',      '200302241234V', NULL,                 1),
(20, 'Sahan Wijeratne',    '1965-08-19', 'Male',   'No. 58, Dutugemunu Street, Kohuwala',   '0723456020', 'sahan.w@gmail.com',          '196508192345V', NULL,                 1),
(21, 'Test Patient One',   '1995-05-15', 'Female', 'No. 100, Test Street, Colombo',         '0723456777', 'test.patient1@test.com',     '199505199999V', 'None',               1);

-- Dentists (tests reference IDs 9, 10, 13, 16)
INSERT INTO dentists (dentist_id, name, specialization, contact, email, NIC, password, available_days, is_active) VALUES
(1,  'Dr. Rajeev Mendis',       'Periodontics',         '0712345603', 'rmendis@sunrisedental.lk',      '197803039012V', 'Dentist@123', 'Tuesday,Thursday,Saturday',                   1),
(2,  'Dr. Chamari Seneviratne', 'Endodontics',          '0712345604', 'cseneviratne@sunrisedental.lk', '199104041234V', 'Dentist@123', 'Monday,Tuesday,Wednesday,Thursday',           1),
(3,  'Dr. Prabhath Rodrigo',    'Prosthodontics',       '0712345606', 'prodrigo@sunrisedental.lk',     '197012023456V', 'Dentist@123', 'Monday,Tuesday,Friday',                       1),
(4,  'Dr. Shalini Weerasinghe', 'Pediatric Dentistry', '0712345607', 'sweerasinghe@sunrisedental.lk', '198808084567V', 'Dentist@123', 'Tuesday,Wednesday,Thursday,Saturday',         1),
(5,  'Dr. Arjuna Koswatta',     'Cosmetic Dentistry',   '0712345620', 'akoswatta5@sunrisedental.lk',    '198103035678V', 'Dentist@123', 'Monday,Wednesday,Friday,Saturday',            1),
(6,  'Dr. Dinesh Perera',       'General Dentistry',    '0712345601', 'dperera3@sunrisedental.lk',     '198502027891V', 'Dentist@123', 'Monday,Tuesday,Wednesday,Thursday,Friday',    1),
(7,  'Dr. Thilini Gunawardena', 'Orthodontics',         '0712345602', 'tgunawardena3@sunrisedental.lk','198906068902V', 'Dentist@123', 'Monday,Wednesday,Friday',                     1),
(8,  'Dr. Nuwan Jayasuriya',    'Oral Surgery',         '0712345605', 'njayasuriya3@sunrisedental.lk', '198607072346V', 'Dentist@123', 'Wednesday,Thursday,Friday,Saturday',          1),
(9,  'Dr. Dinesh Perera',       'General Dentistry',    '0712345609', 'dperera@sunrisedental.lk',      '198502027890V', 'Dentist@123', 'Monday,Tuesday,Wednesday,Thursday,Friday',    1),
(10, 'Dr. Thilini Gunawardena', 'Orthodontics',         '0712345610', 'tgunawardena@sunrisedental.lk', '198906068901V', 'Dentist@123', 'Monday,Wednesday,Friday',                     1),
(11, 'Dr. Rajeev Mendis',       'Periodontics',         '0712345611', 'rmendis2@sunrisedental.lk',     '197803039013V', 'Dentist@123', 'Tuesday,Thursday,Saturday',                   1),
(12, 'Dr. Chamari Seneviratne', 'Endodontics',          '0712345612', 'cseneviratna2@sunrisedental.lk','199104041235V', 'Dentist@123', 'Monday,Tuesday,Wednesday,Thursday',           1),
(13, 'Dr. Nuwan Jayasuriya',    'Oral Surgery',         '0712345613', 'njayasuriya@sunrisedental.lk',  '198607072345V', 'Dentist@123', 'Wednesday,Thursday,Friday,Saturday',          1),
(14, 'Dr. Prabhath Rodrigo',    'Prosthodontics',       '0712345614', 'prodrigo2@sunrisedental.lk',    '197012023457V', 'Dentist@123', 'Monday,Tuesday,Friday',                       1),
(15, 'Dr. Shalini Weerasinghe', 'Pediatric Dentistry', '0712345615', 'sweerasinghe2@sunrisedental.lk','198808084568V', 'Dentist@123', 'Tuesday,Wednesday,Thursday,Saturday',         1),
(16, 'Dr. Arjuna Koswatta',     'Cosmetic Dentistry',   '0712345608', 'akoswatta@sunrisedental.lk',    '198103035679V', 'Dentist@123', 'Monday,Wednesday,Friday,Saturday',            1);

-- Treatments
INSERT INTO treatments (treatment_id, treatment_code, treatment_name, base_price, consultation_fee, category, duration_minutes, description) VALUES
(1,  'TRT-001', 'Dental Consultation',      1500.00,  1500.00, 'General',      30,  'Initial consultation and oral examination'),
(2,  'TRT-002', 'Dental Cleaning',          3500.00,  1500.00, 'Preventive',   45,  'Professional cleaning including scaling and polishing'),
(3,  'TRT-003', 'Tooth Extraction',         5000.00,  1500.00, 'Oral Surgery',  30,  'Simple tooth extraction under local anesthesia'),
(4,  'TRT-004', 'Surgical Extraction',     12000.00,  1500.00, 'Oral Surgery',  60,  'Surgical removal of impacted or complex teeth'),
(5,  'TRT-005', 'Dental Filling',           4500.00,  1500.00, 'Restorative',  45,  'Composite or amalgam tooth filling'),
(6,  'TRT-006', 'Root Canal Treatment',    18000.00,  1500.00, 'Endodontics',  90,  'Complete root canal therapy including crown'),
(7,  'TRT-007', 'Dental Crown',            25000.00,  1500.00, 'Prosthodontics',90, 'Porcelain or metal-ceramic crown placement'),
(8,  'TRT-008', 'Dental Bridge',           45000.00,  1500.00, 'Prosthodontics',120,'Fixed dental bridge for missing teeth'),
(9,  'TRT-009', 'Dentures (Full)',         55000.00,  1500.00, 'Prosthodontics',90, 'Complete upper or lower denture fabrication'),
(10, 'TRT-010', 'Dentures (Partial)',      35000.00,  1500.00, 'Prosthodontics',60, 'Removable partial denture fabrication'),
(11, 'TRT-011', 'Dental Implant',         120000.00,  1500.00, 'Oral Surgery', 120, 'Single tooth titanium implant with crown'),
(12, 'TRT-012', 'Teeth Whitening',         15000.00,  1500.00, 'Cosmetic',     60,  'In-clinic professional teeth whitening'),
(13, 'TRT-013', 'Dental Veneers',          20000.00,  1500.00, 'Cosmetic',     90,  'Porcelain veneer per tooth'),
(14, 'TRT-014', 'Braces (Metal)',          80000.00,  1500.00, 'Orthodontics', 60,  'Full metal bracket orthodontic braces'),
(15, 'TRT-015', 'Braces (Ceramic)',       110000.00,  1500.00, 'Orthodontics', 60,  'Tooth-coloured ceramic bracket braces'),
(16, 'TRT-016', 'Gum Treatment',           8000.00,  1500.00, 'Periodontics', 60,  'Scaling, root planing and gum disease treatment'),
(17, 'TRT-017', 'Fluoride Treatment',      2000.00,  1500.00, 'Preventive',   20,  'Topical fluoride application for cavity prevention');

-- Dentist Available Days (for dentist 9 and others)
INSERT INTO dentist_available_days (dentist_id, day_of_week) VALUES
(9, 'Monday'), (9, 'Tuesday'), (9, 'Wednesday'), (9, 'Thursday'), (9, 'Friday');

-- Dentist Schedule (for dentist 9)
INSERT INTO dentist_schedule (dentist_id, day_of_week, start_time, end_time, availability_status) VALUES
(9, 'Monday',    '09:00:00', '17:00:00', 'Available'),
(9, 'Tuesday',   '09:00:00', '17:00:00', 'Available'),
(9, 'Wednesday', '09:00:00', '17:00:00', 'Available'),
(9, 'Thursday',  '09:00:00', '17:00:00', 'Available'),
(9, 'Friday',    '09:00:00', '15:00:00', 'Available');

-- Appointments (tests reference IDs 33 and 34)
INSERT INTO appointments (appointment_id, appointment_no, patient_id, dentist_id, treatment_type, appointment_date, appointment_time, appointment_type, status, notes, contact, created_by) VALUES
(33, 'SDC-2026-0001', 21, 16, 'Dental Cleaning', '2026-08-10', '09:00:00', 'Treatment', 'Completed', 'Regular cleaning visit', '0723456999', 15),
(34, 'SDC-2026-0002', 21, 16, 'TRT-010',         '2026-08-11', '10:00:00', 'Treatment', 'Pending',   'Upper molar crown',      '0723456999', 15);

-- Bills (tests reference bill_id 21 for appointment_id 33)
INSERT INTO bills (bill_id, appointment_id, consultation_fee, treatment_fee, discount, tax, total_amount, payment_method, payment_status, issued_by) VALUES
(21, 33, 1500.00, 4800.00, 0.00, 0.00, 6300.00, 'CASH', 'PAID', 15);

-- Notifications (minimal for tests)
INSERT INTO notifications (notification_id, user_id, appointment_id, title, channel, recipient, notification_type, message, is_read, status, sent_at) VALUES
(1, 15, 33, 'Appointment Confirmed', 'IN_APP', 'test@test.com', 'APPOINTMENT_CONFIRMATION', 'Test notification', 0, 'PENDING', NULL);

-- Notices (minimal for tests)
INSERT INTO notices (notice_id, title, description, priority, target_role, target_dentist_id, publish_date, expiry_date, status, created_by) VALUES
(1, 'Staff Meeting', 'Monthly staff meeting will be held on Friday at 5.00 PM.', 'Important', 'ALL', NULL, '2026-09-01', '2026-09-10', 'Published', 15);
