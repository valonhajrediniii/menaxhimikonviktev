CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    student_id VARCHAR(40),
    email VARCHAR(150) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS student_id VARCHAR(40);
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_student_id
    ON users(student_id)
    WHERE student_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    faculty VARCHAR(120) NOT NULL,
    study_program VARCHAR(140),
    year_of_study INT NOT NULL CHECK (year_of_study BETWEEN 1 AND 6),
    gender VARCHAR(20),
    phone VARCHAR(30),
    city VARCHAR(120),
    photo_url TEXT
);

ALTER TABLE student_profiles ADD COLUMN IF NOT EXISTS study_program VARCHAR(140);
ALTER TABLE student_profiles ADD COLUMN IF NOT EXISTS photo_url TEXT;

CREATE TABLE IF NOT EXISTS dormitories (
    id BIGSERIAL PRIMARY KEY,
    dorm_number VARCHAR(20) NOT NULL UNIQUE,
    dorm_name VARCHAR(120) NOT NULL,
    location VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    dormitory_id BIGINT NOT NULL REFERENCES dormitories(id) ON DELETE CASCADE,
    room_number VARCHAR(20) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    occupied_beds INT NOT NULL DEFAULT 0 CHECK (occupied_beds >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('FREE', 'PARTIAL', 'FULL')),
    UNIQUE (dormitory_id, room_number),
    CHECK (occupied_beds <= capacity)
);

CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    application_date TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    dormitory_id BIGINT REFERENCES dormitories(id) ON DELETE SET NULL,
    room_id BIGINT REFERENCES rooms(id) ON DELETE SET NULL,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    qr_data TEXT NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS complaints (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dormitory_id BIGINT REFERENCES dormitories(id) ON DELETE SET NULL,
    subject VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'ANSWERED', 'CLOSED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE complaints ADD COLUMN IF NOT EXISTS dormitory_id BIGINT REFERENCES dormitories(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS complaint_replies (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    admin_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reply_message TEXT NOT NULL,
    replied_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status);
CREATE INDEX IF NOT EXISTS idx_complaints_dormitory_id ON complaints(dormitory_id);
CREATE INDEX IF NOT EXISTS idx_complaint_replies_complaint_id ON complaint_replies(complaint_id);

INSERT INTO users (full_name, email, password, role)
VALUES ('System Admin', 'admin@dormitory.local', 'admin123', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO dormitories (dorm_number, dorm_name, location)
VALUES
    ('D-1', 'Dormitory One', 'Campus North'),
    ('D-2', 'Dormitory Two', 'Campus South'),
    ('D-3', 'Dormitory Three', 'Campus East'),
    ('D-4', 'Dormitory Four', 'Campus West'),
    ('D-5', 'Dormitory Five', 'Campus Center'),
    ('D-6', 'Dormitory Six', 'Campus River'),
    ('D-7', 'Dormitory Seven', 'Campus Park'),
    ('D-8', 'Dormitory Eight', 'Campus Hill')
ON CONFLICT (dorm_number) DO NOTHING;

INSERT INTO rooms (dormitory_id, room_number, capacity, occupied_beds, status)
SELECT d.id, r.room_number, r.capacity, 0, 'FREE'
FROM dormitories d
JOIN (
    VALUES
        ('101', 2),
        ('102', 3),
        ('201', 2)
) AS r(room_number, capacity) ON TRUE
WHERE d.dorm_number IN ('D-1', 'D-2')
ON CONFLICT (dormitory_id, room_number) DO NOTHING;
