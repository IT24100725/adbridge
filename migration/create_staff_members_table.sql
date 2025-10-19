-- Create staff_members table for AdBridge Application
-- Run this script in your existing 'adbridge' database

USE adbridge;
GO

-- Create staff_members table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='staff_members' AND xtype='U')
BEGIN
    CREATE TABLE staff_members (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        full_name VARCHAR(100) NOT NULL,
        email VARCHAR(150) NOT NULL,
        phone VARCHAR(20),
        address TEXT,
        position VARCHAR(100) NOT NULL,
        role VARCHAR(50) NOT NULL,
        username VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(100) NOT NULL,
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        created_by VARCHAR(50),
        updated_by VARCHAR(50),

        deleted_at DATETIME2,
        deleted_by VARCHAR(50)
    );
    
    CREATE INDEX idx_staff_members_role ON staff_members(role);
    CREATE INDEX idx_staff_members_username ON staff_members(username);
    CREATE INDEX idx_staff_members_is_active ON staff_members(is_active);
    CREATE INDEX idx_staff_members_email ON staff_members(email);
END
GO

PRINT 'staff_members table created successfully!';
GO

