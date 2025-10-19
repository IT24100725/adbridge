-- Test Connection Script for AdBridge Application
-- Run this to test your SQL Server connection and verify the database setup

USE adbridge;
GO

-- Test 1: Check if database exists and is accessible
SELECT 
    name as 'Database Name',
    database_id as 'Database ID',
    create_date as 'Created Date'
FROM sys.databases 
WHERE name = 'adbridge';
GO

-- Test 2: Check if tables exist
SELECT 
    TABLE_NAME as 'Table Name',
    TABLE_TYPE as 'Table Type'
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_CATALOG = 'adbridge'
ORDER BY TABLE_NAME;
GO

-- Test 3: Check table row counts (will be 0 for new tables)
SELECT 
    t.name AS 'Table Name',
    p.rows AS 'Row Count'
FROM sys.tables t
INNER JOIN sys.partitions p ON t.object_id = p.object_id
WHERE t.name IN ('users', 'booking', 'payments', 'notifications', 'tasks')
    AND p.index_id IN (0, 1)
ORDER BY t.name;
GO

-- Test 4: Test basic insert/select operations
-- Create a test user (you can delete this later)
IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 'TEST001')
BEGIN
    INSERT INTO users (user_id, full_name, email, phone, username, password, role)
    VALUES ('TEST001', 'Test User', 'test@example.com', '1234567890', 'testuser', 'password123', 'USER');
    
    PRINT 'Test user created successfully!';
END
ELSE
BEGIN
    PRINT 'Test user already exists!';
END
GO

-- Test 5: Verify the test user was created
SELECT user_id, full_name, email, username, role, created_at
FROM users 
WHERE user_id = 'TEST001';
GO

-- Test 6: Clean up test data
DELETE FROM users WHERE user_id = 'TEST001';
PRINT 'Test user deleted successfully!';
GO

PRINT 'All connection tests completed successfully!';
PRINT 'Your SQL Server database is ready for the AdBridge application.';
GO



