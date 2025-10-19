-- Fix existing user permissions for pc2\oshan
-- Run this script in SQL Server Management Studio

USE adbridge;
GO

-- Check if user already exists and what permissions they have
SELECT 
    dp.name AS 'User Name',
    dp.type_desc AS 'User Type',
    r.name AS 'Role Name'
FROM sys.database_principals dp
LEFT JOIN sys.database_role_members rm ON dp.principal_id = rm.member_principal_id
LEFT JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
WHERE dp.name = 'pc2\oshan';
GO

-- If user exists but doesn't have db_owner role, add it
IF EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'pc2\oshan')
BEGIN
    -- Add to db_owner role if not already a member
    IF NOT EXISTS (
        SELECT 1 
        FROM sys.database_role_members rm
        JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
        JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id
        WHERE r.name = 'db_owner' AND u.name = 'pc2\oshan'
    )
    BEGIN
        ALTER ROLE db_owner ADD MEMBER [pc2\oshan];
        PRINT 'Added pc2\oshan to db_owner role';
    END
    ELSE
    BEGIN
        PRINT 'pc2\oshan is already a member of db_owner role';
    END
END
ELSE
BEGIN
    -- Create user if it doesn't exist
    CREATE USER [pc2\oshan] FOR LOGIN [pc2\oshan];
    ALTER ROLE db_owner ADD MEMBER [pc2\oshan];
    PRINT 'Created user pc2\oshan and added to db_owner role';
END
GO

-- Verify final permissions
SELECT 
    dp.name AS 'User Name',
    dp.type_desc AS 'User Type',
    r.name AS 'Role Name'
FROM sys.database_principals dp
LEFT JOIN sys.database_role_members rm ON dp.principal_id = rm.member_principal_id
LEFT JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
WHERE dp.name = 'pc2\oshan';
GO

PRINT 'User permissions check completed!';
GO



