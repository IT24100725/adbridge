-- Grant permissions for Windows user: pc2\oshan
-- Run this script in SQL Server Management Studio

USE adbridge;
GO

-- Create user for your Windows account
CREATE USER [pc2\oshan] FOR LOGIN [pc2\oshan];
GO

-- Grant database owner permissions
ALTER ROLE db_owner ADD MEMBER [pc2\oshan];
GO

-- Verify the user was created
SELECT name, type_desc, is_disabled 
FROM sys.database_principals 
WHERE name = 'pc2\oshan';
GO

PRINT 'Permissions granted successfully for pc2\oshan!';
GO



