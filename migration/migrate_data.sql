-- Data Migration Script for AdBridge Application
-- This script provides examples of how to migrate data from MySQL to SQL Server
-- Run this AFTER creating the tables with create_tables_sqlserver.sql

USE adbridge;
GO

-- Example data migration queries (modify based on your actual data)
-- You can use SQL Server Import/Export Wizard or SSIS for bulk data migration

-- 1. Migrate users data
-- INSERT INTO users (user_id, full_name, email, phone, company_name, username, password, role, ...)
-- SELECT user_id, full_name, email, phone, company_name, username, password, role, ...
-- FROM [MySQL_Server].[database_name].[users];

-- 2. Migrate bookings data
-- INSERT INTO booking (booking_id, full_name, company_name, contact_number, email, service_type, ...)
-- SELECT booking_id, full_name, company_name, contact_number, email, service_type, ...
-- FROM [MySQL_Server].[database_name].[booking];

-- 3. Migrate payments data
-- INSERT INTO payments (payment_id, booking_id, payment_method, payment_status, amount, ...)
-- SELECT payment_id, booking_id, payment_method, payment_status, amount, ...
-- FROM [MySQL_Server].[database_name].[payments];

-- 4. Migrate other tables similarly...

-- After data migration, you may need to reset identity columns
-- DBCC CHECKIDENT ('users', RESEED, [new_value]);
-- DBCC CHECKIDENT ('booking', RESEED, [new_value]);
-- etc.

PRINT 'Data migration script ready. Please modify the queries based on your actual data structure.';
GO



