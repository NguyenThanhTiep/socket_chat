-- =========================
-- SimpleChat - init database + user
-- =========================

-- 1) Create database
CREATE DATABASE IF NOT EXISTS simplechat
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2) Create user for localhost
CREATE USER IF NOT EXISTS 'simplechat'@'localhost'
IDENTIFIED BY 'simplechat123';

-- 3) Grant privileges
GRANT ALL PRIVILEGES ON simplechat.* TO 'simplechat'@'localhost';

FLUSH PRIVILEGES;

-- 4) Quick check
SHOW DATABASES LIKE 'simplechat';
SELECT user, host FROM mysql.user WHERE user='simplechat';
