INSERT INTO tutor (name, bio) VALUES ('튜터1', '튜터설명1');
INSERT INTO tutor (name, bio) VALUES ('튜터2', '튜터설명2');
INSERT INTO tutor (name, bio) VALUES ('튜터3', '튜터설명3');

INSERT INTO student (name, email) VALUES ('학생1', 'student1@test.com');
INSERT INTO student (name, email) VALUES ('학생2', 'student2@test.com');
INSERT INTO student (name, email) VALUES ('학생3', 'student3@test.com');

INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (1, '2025-07-10 10:00:00', '2025-07-10 10:30:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (1, '2025-07-10 10:30:00', '2025-07-10 11:00:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (1, '2025-07-10 14:00:00', '2025-07-10 15:00:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (1, '2025-07-11 11:00:00', '2025-07-11 11:30:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (2, '2025-07-10 16:00:00', '2025-07-10 16:30:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (2, '2025-07-10 16:30:00', '2025-07-10 17:00:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (2, '2025-07-12 09:00:00', '2025-07-12 10:00:00', 'BOOKED');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (3, '2025-07-15 19:00:00', '2025-07-15 19:30:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (3, '2025-07-15 19:30:00', '2025-07-15 20:00:00', 'AVAILABLE');
INSERT INTO session_slot (tutor_id, start_time, end_time, status) VALUES (3, '2025-07-15 20:00:00', '2025-07-15 20:30:00', 'AVAILABLE');

INSERT INTO session_booking (student_id, tutor_id, start_time, end_time, duration_minutes) VALUES (1, 2, '2025-07-12 09:00:00', '2025-07-12 10:00:00', 60);

INSERT INTO slot_booking_mapping (slot_id, booking_id) VALUES (7, 1);