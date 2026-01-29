-- Clear existing data and insert controlled test data
-- We have 5 patients total, 1 of them is deleted (soft delete)

DELETE FROM patients;

-- Active patients (deleted = false)
INSERT INTO patients (id, first_name, last_name, date_of_birth, gender, insurance_number, blood_type, deleted, created_at, version)
VALUES (1, 'John', 'Doe', '1985-06-15', 'MALE', 'M8506151234', 'O_POS', false, CURRENT_TIMESTAMP, 0);

INSERT INTO patients (id, first_name, last_name, date_of_birth, gender, insurance_number, blood_type, deleted, created_at, version)
VALUES (2, 'Jane', 'Smith', '1990-03-20', 'FEMALE', 'F9003205678', 'A_POS', false, CURRENT_TIMESTAMP, 0);

-- Deleted patient (deleted = true) - Mark Johnson
INSERT INTO patients (id, first_name, last_name, date_of_birth, gender, insurance_number, blood_type, deleted, created_at, version)
VALUES (3, 'Mark', 'Johnson', '1975-11-10', 'MALE', 'M7511105678', 'B_NEG', true, CURRENT_TIMESTAMP, 0);

-- More active patients
INSERT INTO patients (id, first_name, last_name, date_of_birth, gender, insurance_number, blood_type, deleted, created_at, version)
VALUES (4, 'Sarah', 'Williams', '1995-08-25', 'FEMALE', 'F9508259012', 'AB_POS', false, CURRENT_TIMESTAMP, 0);

INSERT INTO patients (id, first_name, last_name, date_of_birth, gender, insurance_number, blood_type, deleted, created_at, version)
VALUES (5, 'Michael', 'Brown', '2000-02-15', 'MALE', 'M0002153456', 'O_NEG', false, CURRENT_TIMESTAMP, 0);