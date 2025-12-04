-- ===========================================================
-- TELECOMDB DATABASE (Duration-based Services + Auto Expiry)
-- Full schema in ONE file
-- ===========================================================

DROP DATABASE IF EXISTS telecomdb;
CREATE DATABASE telecomdb;
USE telecomdb;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

-- ===========================================================
-- 1. USERS TABLE
-- ===========================================================
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','CUSTOMER') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ===========================================================
-- 2. CUSTOMERS TABLE
-- ===========================================================
CREATE TABLE customers (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    user_id INT UNIQUE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ===========================================================
-- 3. SERVICES TABLE (With duration + charge)
-- ===========================================================
CREATE TABLE services (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    charge DECIMAL(10,2) NOT NULL,
    duration_value INT NOT NULL DEFAULT 1,
    duration_unit ENUM('HOUR', 'DAY', 'WEEK', 'MONTH') NOT NULL DEFAULT 'DAY',
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ===========================================================
-- 4. SUBSCRIPTIONS TABLE (Auto-expiry)
-- ===========================================================
CREATE TABLE subscriptions (
    id INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    service_id INT NOT NULL,
    purchase_date DATETIME NOT NULL,
    expiry_date DATETIME NOT NULL,
    status ENUM('ACTIVE','EXPIRED') DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (service_id) REFERENCES services(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ===========================================================
-- 5. BILLING TABLE
-- ===========================================================
CREATE TABLE billing (
    id INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    service_id INT NOT NULL,
    amount DOUBLE,
    billing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (service_id) REFERENCES services(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ===========================================================
-- SAMPLE DATA
-- ===========================================================

-- Users
INSERT INTO users (username, password, role) VALUES
('admin', 'admin123', 'ADMIN'),
('john_doe', 'password123', 'CUSTOMER'),
('mary_kimani', 'password123', 'CUSTOMER');

-- Customers
INSERT INTO customers (name, phone, email, user_id) VALUES
('John Doe', '0712345678', 'john@example.com', 2),
('Mary Kimani', '0798765432', 'mary@example.com', 3);

-- Services with durations
INSERT INTO services (name, description, charge, duration_value, duration_unit, active) VALUES
('1 Hour Data 1GB', '1GB valid for one hour', 100.00, 1, 'HOUR', 1),
('Daily Unlimited', 'Unlimited internet for 1 day', 250.00, 1, 'DAY', 1),
('Weekly 10GB', '10GB valid for one week', 500.00, 1, 'WEEK', 1),
('Monthly Freedom 50GB', '50GB monthly', 1500.00, 1, 'MONTH', 1);

-- Subscriptions with auto expiry
INSERT INTO subscriptions (customer_id, service_id, purchase_date, expiry_date, status) VALUES
(1, 1, '2025-01-01 10:00:00', '2025-01-01 11:00:00', 'EXPIRED'),
(2, 3, '2025-02-10 09:00:00', '2025-02-17 09:00:00', 'ACTIVE');

-- Billing examples
INSERT INTO billing (customer_id, service_id, amount, billing_date, paid) VALUES
(1, 1, 100.00, '2025-01-01', 1),
(2, 3, 500.00, '2025-02-10', 1);

-- ===========================================================
-- END
-- ===========================================================

COMMIT;
