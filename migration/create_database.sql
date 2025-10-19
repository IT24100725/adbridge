-- SQL Server Migration Script for AdBridge Application
-- This script creates the database and all tables for SQL Server

-- Create database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'adbridge')
BEGIN
    CREATE DATABASE adbridge;
END
GO

USE adbridge;
GO

-- Enable foreign key constraints
EXEC sp_configure 'show advanced options', 1;
RECONFIGURE;
EXEC sp_configure 'foreign key checks', 1;
RECONFIGURE;
GO



