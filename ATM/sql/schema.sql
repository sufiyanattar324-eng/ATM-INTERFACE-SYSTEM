-- ============================================
-- ATM Interface System - Database Schema
-- ============================================
-- Run this script in MySQL to create the database and tables.

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS atm_system;
USE atm_system;

-- Step 2: Create Users Table
-- Stores personal details of each user
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    mobile        VARCHAR(15) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    pin           VARCHAR(10) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 3: Create Accounts Table
-- Stores bank account details linked to each user
CREATE TABLE IF NOT EXISTS accounts (
    account_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    ifsc_code      VARCHAR(15) NOT NULL,
    balance        DOUBLE DEFAULT 0.0,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Step 4: Create Transactions Table
-- Stores all transaction history
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    account_number   VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,  -- DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
    amount           DOUBLE NOT NULL,
    balance_after    DOUBLE NOT NULL,
    description      VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

-- ============================================
-- End of Schema
-- ============================================
