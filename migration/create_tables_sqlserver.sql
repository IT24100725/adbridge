-- SQL Server Table Creation Script for AdBridge Application
-- Run this script in your existing 'adbridge' database

USE adbridge;
GO

-- Create users table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    CREATE TABLE users (
        user_id VARCHAR(10) PRIMARY KEY,
        full_name VARCHAR(80) NOT NULL,
        email VARCHAR(100) NOT NULL,
        phone VARCHAR(20) NOT NULL,
        company_name VARCHAR(100),
        username VARCHAR(30) NOT NULL UNIQUE,
        password VARCHAR(120) NOT NULL,
        role VARCHAR(20) NOT NULL DEFAULT 'USER',
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        last_login DATETIME2,
        profile_picture VARCHAR(255),
        address TEXT,
        city VARCHAR(50),
        country VARCHAR(50),
        postal_code VARCHAR(20),
        date_of_birth DATE,
        gender VARCHAR(10),
        occupation VARCHAR(100),
        website VARCHAR(255),
        bio TEXT,
        preferences TEXT,
        notification_settings TEXT,
        email_verified BIT NOT NULL DEFAULT 0,
        phone_verified BIT NOT NULL DEFAULT 0,
        two_factor_enabled BIT NOT NULL DEFAULT 0,
        account_locked BIT NOT NULL DEFAULT 0,
        failed_login_attempts INT NOT NULL DEFAULT 0,
        locked_until DATETIME2,
        password_reset_token VARCHAR(255),
        password_reset_expires DATETIME2,
        email_verification_token VARCHAR(255),
        phone_verification_code VARCHAR(10),
        phone_verification_expires DATETIME2,
        last_password_change DATETIME2,
        terms_accepted BIT NOT NULL DEFAULT 0,
        terms_accepted_at DATETIME2,
        privacy_policy_accepted BIT NOT NULL DEFAULT 0,
        privacy_policy_accepted_at DATETIME2,
        marketing_consent BIT NOT NULL DEFAULT 0,
        data_retention_consent BIT NOT NULL DEFAULT 0,
        gdpr_consent BIT NOT NULL DEFAULT 0,
        cookie_consent BIT NOT NULL DEFAULT 0,
        ip_address VARCHAR(45),
        user_agent TEXT,
        referrer VARCHAR(255),
        utm_source VARCHAR(100),
        utm_medium VARCHAR(100),
        utm_campaign VARCHAR(100),
        utm_term VARCHAR(100),
        utm_content VARCHAR(100),
        session_id VARCHAR(255),
        device_type VARCHAR(50),
        browser VARCHAR(100),
        operating_system VARCHAR(100),
        timezone VARCHAR(50),
        language VARCHAR(10),
        currency VARCHAR(3),
        country_code VARCHAR(2),
        region VARCHAR(100),
        latitude DECIMAL(10, 8),
        longitude DECIMAL(11, 8),
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100),
        version INT NOT NULL DEFAULT 1,
        created_by VARCHAR(100),
        updated_by VARCHAR(100)
    );
    
    CREATE INDEX idx_users_email ON users(email);
    CREATE INDEX idx_users_username ON users(username);
    CREATE INDEX idx_users_role ON users(role);
    CREATE INDEX idx_users_is_active ON users(is_active);
END
GO

-- Create bookings table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='booking' AND xtype='U')
BEGIN
    CREATE TABLE booking (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id VARCHAR(255) NOT NULL UNIQUE,
        full_name VARCHAR(100) NOT NULL,
        company_name VARCHAR(150),
        contact_number VARCHAR(20) NOT NULL,
        email VARCHAR(150) NOT NULL,
        service_type VARCHAR(50) NOT NULL,
        option_one VARCHAR(255),
        option_two VARCHAR(255),
        option_three VARCHAR(255),
        option_four VARCHAR(255),
        option_five VARCHAR(255),
        option_six VARCHAR(255),
        option_seven VARCHAR(255),
        option_eight VARCHAR(255),
        option_nine VARCHAR(255),
        option_ten VARCHAR(255),
        total_amount DECIMAL(10,2) NOT NULL,
        payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        campaign_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        current_version INT NOT NULL DEFAULT 0,
        is_edited BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        last_modified_at DATETIME2,
        notes TEXT,
        special_requirements TEXT,
        budget_range VARCHAR(50),
        timeline VARCHAR(100),
        target_audience TEXT,
        campaign_objectives TEXT,
        preferred_channels TEXT,
        brand_guidelines TEXT,
        competitor_analysis TEXT,
        success_metrics TEXT,
        approval_notes TEXT,
        rejection_reason TEXT,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_booking_booking_id ON booking(booking_id);
    CREATE INDEX idx_booking_email ON booking(email);
    CREATE INDEX idx_booking_payment_status ON booking(payment_status);
    CREATE INDEX idx_booking_campaign_status ON booking(campaign_status);
END
GO

-- Create payments table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='payments' AND xtype='U')
BEGIN
    CREATE TABLE payments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        payment_id VARCHAR(255) NOT NULL UNIQUE,
        booking_id VARCHAR(255) NOT NULL,
        payment_method VARCHAR(20) NOT NULL,
        payment_status VARCHAR(20) NOT NULL,
        amount INT NOT NULL,
        currency VARCHAR(3) NOT NULL DEFAULT 'LKR',
        card_number VARCHAR(255),
        cardholder_name VARCHAR(255),
        expiry_date VARCHAR(10),
        cvv VARCHAR(10),
        billing_address TEXT,
        preferred_date VARCHAR(50),
        preferred_time VARCHAR(50),
        payment_location VARCHAR(255),
        contact_number VARCHAR(20),
        special_instructions TEXT,
        bank_name VARCHAR(100),
        account_number VARCHAR(50),
        transaction_reference VARCHAR(255),
        payment_notes TEXT,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        external_transaction_id VARCHAR(255),
        external_status VARCHAR(50),
        external_message TEXT,
        deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100),
        last_action VARCHAR(50),
        last_action_by VARCHAR(100),
        last_action_at DATETIME2,
        action_reason TEXT
    );
    
    CREATE INDEX idx_payments_payment_id ON payments(payment_id);
    CREATE INDEX idx_payments_booking_id ON payments(booking_id);
    CREATE INDEX idx_payments_payment_status ON payments(payment_status);
    CREATE INDEX idx_payments_payment_method ON payments(payment_method);
END
GO

-- Create notifications table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='notifications' AND xtype='U')
BEGIN
    CREATE TABLE notifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id VARCHAR(10) NOT NULL,
        title VARCHAR(200) NOT NULL,
        message VARCHAR(1000) NOT NULL,
        is_read BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        read_at DATETIME2,
        type VARCHAR(20) NOT NULL DEFAULT 'INFO'
    );
    
    CREATE INDEX idx_notifications_user_id ON notifications(user_id);
    CREATE INDEX idx_notifications_is_read ON notifications(is_read);
    CREATE INDEX idx_notifications_created_at ON notifications(created_at);
END
GO

-- Create tasks table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tasks' AND xtype='U')
BEGIN
    CREATE TABLE tasks (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title VARCHAR(200) NOT NULL,
        description TEXT,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
        assigned_to VARCHAR(10),
        created_by VARCHAR(10) NOT NULL,
        due_date DATETIME2,
        completed_at DATETIME2,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        tags VARCHAR(500),
        estimated_hours DECIMAL(5,2),
        actual_hours DECIMAL(5,2),
        progress_percentage INT NOT NULL DEFAULT 0,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
    CREATE INDEX idx_tasks_created_by ON tasks(created_by);
    CREATE INDEX idx_tasks_status ON tasks(status);
    CREATE INDEX idx_tasks_priority ON tasks(priority);
END
GO

-- Create support_requests table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='support_requests' AND xtype='U')
BEGIN
    CREATE TABLE support_requests (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id VARCHAR(10) NOT NULL,
        subject VARCHAR(200) NOT NULL,
        description TEXT NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
        priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
        category VARCHAR(50) NOT NULL,
        assigned_to VARCHAR(10),
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        resolved_at DATETIME2,
        resolution_notes TEXT,
        attachments VARCHAR(1000),
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_support_requests_user_id ON support_requests(user_id);
    CREATE INDEX idx_support_requests_assigned_to ON support_requests(assigned_to);
    CREATE INDEX idx_support_requests_status ON support_requests(status);
    CREATE INDEX idx_support_requests_priority ON support_requests(priority);
END
GO

-- Create support_comments table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='support_comments' AND xtype='U')
BEGIN
    CREATE TABLE support_comments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        support_request_id BIGINT NOT NULL,
        user_id VARCHAR(10) NOT NULL,
        comment TEXT NOT NULL,
        is_internal BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_support_comments_support_request_id ON support_comments(support_request_id);
    CREATE INDEX idx_support_comments_user_id ON support_comments(user_id);
    CREATE INDEX idx_support_comments_created_at ON support_comments(created_at);
END
GO

-- Create documents table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='documents' AND xtype='U')
BEGIN
    CREATE TABLE documents (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title VARCHAR(200) NOT NULL,
        description TEXT,
        file_path VARCHAR(500) NOT NULL,
        file_name VARCHAR(255) NOT NULL,
        file_size BIGINT NOT NULL,
        mime_type VARCHAR(100) NOT NULL,
        uploaded_by VARCHAR(10) NOT NULL,
        category VARCHAR(50),
        tags VARCHAR(500),
        is_public BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by);
    CREATE INDEX idx_documents_category ON documents(category);
    CREATE INDEX idx_documents_is_public ON documents(is_public);
    CREATE INDEX idx_documents_created_at ON documents(created_at);
END
GO

-- Create invoices table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='invoices' AND xtype='U')
BEGIN
    CREATE TABLE invoices (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        invoice_number VARCHAR(50) NOT NULL UNIQUE,
        booking_id VARCHAR(255) NOT NULL,
        user_id VARCHAR(10) NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        total_amount DECIMAL(10,2) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
        due_date DATE,
        paid_at DATETIME2,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        notes TEXT,
        payment_method VARCHAR(50),
        payment_reference VARCHAR(255),
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);
    CREATE INDEX idx_invoices_booking_id ON invoices(booking_id);
    CREATE INDEX idx_invoices_user_id ON invoices(user_id);
    CREATE INDEX idx_invoices_status ON invoices(status);
END
GO

-- Create budgets table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='budgets' AND xtype='U')
BEGIN
    CREATE TABLE budgets (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(200) NOT NULL,
        description TEXT,
        amount DECIMAL(12,2) NOT NULL,
        spent_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
        remaining_amount DECIMAL(12,2) NOT NULL,
        category VARCHAR(50) NOT NULL,
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        created_by VARCHAR(10) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        is_active BIT NOT NULL DEFAULT 1,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_budgets_created_by ON budgets(created_by);
    CREATE INDEX idx_budgets_category ON budgets(category);
    CREATE INDEX idx_budgets_is_active ON budgets(is_active);
    CREATE INDEX idx_budgets_start_date ON budgets(start_date);
    CREATE INDEX idx_budgets_end_date ON budgets(end_date);
END
GO

-- Create expenses table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='expenses' AND xtype='U')
BEGIN
    CREATE TABLE expenses (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        budget_id BIGINT NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        description TEXT NOT NULL,
        category VARCHAR(50) NOT NULL,
        expense_date DATE NOT NULL,
        created_by VARCHAR(10) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        receipt_path VARCHAR(500),
        is_approved BIT NOT NULL DEFAULT 0,
        approved_by VARCHAR(10),
        approved_at DATETIME2,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_expenses_budget_id ON expenses(budget_id);
    CREATE INDEX idx_expenses_created_by ON expenses(created_by);
    CREATE INDEX idx_expenses_category ON expenses(category);
    CREATE INDEX idx_expenses_expense_date ON expenses(expense_date);
    CREATE INDEX idx_expenses_is_approved ON expenses(is_approved);
END
GO

-- Create services table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='services' AND xtype='U')
BEGIN
    CREATE TABLE services (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(200) NOT NULL,
        description TEXT,
        price DECIMAL(10,2) NOT NULL,
        category VARCHAR(50) NOT NULL,
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        created_by VARCHAR(10) NOT NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_services_category ON services(category);
    CREATE INDEX idx_services_is_active ON services(is_active);
    CREATE INDEX idx_services_created_by ON services(created_by);
END
GO

-- Create contacts table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='contacts' AND xtype='U')
BEGIN
    CREATE TABLE contacts (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        email VARCHAR(150),
        phone VARCHAR(20),
        company VARCHAR(100),
        position VARCHAR(100),
        notes TEXT,
        created_by VARCHAR(10) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2,
        deleted_by VARCHAR(100)
    );
    
    CREATE INDEX idx_contacts_created_by ON contacts(created_by);
    CREATE INDEX idx_contacts_email ON contacts(email);
    CREATE INDEX idx_contacts_company ON contacts(company);
END
GO

-- Create communication_history table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='communication_history' AND xtype='U')
-- BEGIN
--     CREATE TABLE communication_history (
--         id BIGINT IDENTITY(1,1) PRIMARY KEY,
--         contact_id BIGINT NOT NULL,
--         user_id VARCHAR(10) NOT NULL,
--         communication_type VARCHAR(20) NOT NULL,
--         subject VARCHAR(200),
--         message TEXT,
--         created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
--         is_deleted BIT NOT NULL DEFAULT 0,
--         deleted_at DATETIME2,
--         deleted_by VARCHAR(100)
--     );
--
--     CREATE INDEX idx_communication_history_contact_id ON communication_history(contact_id);
--     CREATE INDEX idx_communication_history_user_id ON communication_history(user_id);
--     CREATE INDEX idx_communication_history_communication_type ON communication_history(communication_type);
--     CREATE INDEX idx_communication_history_created_at ON communication_history(created_at);
-- END
-- GO

-- Create booking_history table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='booking_history' AND xtype='U')
BEGIN
    CREATE TABLE booking_history (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id VARCHAR(255) NOT NULL,
        action VARCHAR(50) NOT NULL,
        old_value TEXT,
        new_value TEXT,
        changed_by VARCHAR(10) NOT NULL,
        changed_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        reason TEXT
    );
    
    CREATE INDEX idx_booking_history_booking_id ON booking_history(booking_id);
    CREATE INDEX idx_booking_history_changed_by ON booking_history(changed_by);
    CREATE INDEX idx_booking_history_changed_at ON booking_history(changed_at);
END
GO

-- Create payment_audit_logs table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='payment_audit_logs' AND xtype='U')
BEGIN
    CREATE TABLE payment_audit_logs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        payment_id VARCHAR(255) NOT NULL,
        action VARCHAR(50) NOT NULL,
        old_status VARCHAR(20),
        new_status VARCHAR(20),
        changed_by VARCHAR(10) NOT NULL,
        changed_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        reason TEXT,
        ip_address VARCHAR(45),
        user_agent TEXT
    );
    
    CREATE INDEX idx_payment_audit_logs_payment_id ON payment_audit_logs(payment_id);
    CREATE INDEX idx_payment_audit_logs_changed_by ON payment_audit_logs(changed_by);
    CREATE INDEX idx_payment_audit_logs_changed_at ON payment_audit_logs(changed_at);
END
GO

-- Create system_settings table
-- IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='system_settings' AND xtype='U')
BEGIN
    CREATE TABLE system_settings (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        setting_key VARCHAR(100) NOT NULL UNIQUE,
        setting_value TEXT,
        description TEXT,
        category VARCHAR(50),
        is_encrypted BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        updated_by VARCHAR(10)
    );
    
    CREATE INDEX idx_system_settings_setting_key ON system_settings(setting_key);
    CREATE INDEX idx_system_settings_category ON system_settings(category);
END
GO

-- Add foreign key constraints
-- Note: These will be added after all tables are created to avoid dependency issues

-- Add foreign key for notifications -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_notifications_users')
BEGIN
    ALTER TABLE notifications ADD CONSTRAINT FK_notifications_users 
    FOREIGN KEY (user_id) REFERENCES users(user_id);
END
GO

-- Add foreign key for tasks -> users (assigned_to)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_tasks_assigned_to')
BEGIN
    ALTER TABLE tasks ADD CONSTRAINT FK_tasks_assigned_to 
    FOREIGN KEY (assigned_to) REFERENCES users(user_id);
END
GO

-- Add foreign key for tasks -> users (created_by)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_tasks_created_by')
BEGIN
    ALTER TABLE tasks ADD CONSTRAINT FK_tasks_created_by 
    FOREIGN KEY (created_by) REFERENCES users(user_id);
END
GO

-- Add foreign key for support_requests -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_support_requests_user_id')
BEGIN
    ALTER TABLE support_requests ADD CONSTRAINT FK_support_requests_user_id 
    FOREIGN KEY (user_id) REFERENCES users(user_id);
END
GO

-- Add foreign key for support_requests -> users (assigned_to)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_support_requests_assigned_to')
BEGIN
    ALTER TABLE support_requests ADD CONSTRAINT FK_support_requests_assigned_to 
    FOREIGN KEY (assigned_to) REFERENCES users(user_id);
END
GO

-- Add foreign key for support_comments -> support_requests
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_support_comments_support_request_id')
BEGIN
    ALTER TABLE support_comments ADD CONSTRAINT FK_support_comments_support_request_id 
    FOREIGN KEY (support_request_id) REFERENCES support_requests(id);
END
GO

-- Add foreign key for support_comments -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_support_comments_user_id')
BEGIN
    ALTER TABLE support_comments ADD CONSTRAINT FK_support_comments_user_id 
    FOREIGN KEY (user_id) REFERENCES users(user_id);
END
GO

-- Add foreign key for documents -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_documents_uploaded_by')
BEGIN
    ALTER TABLE documents ADD CONSTRAINT FK_documents_uploaded_by 
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id);
END
GO

-- Add foreign key for invoices -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_invoices_user_id')
BEGIN
    ALTER TABLE invoices ADD CONSTRAINT FK_invoices_user_id 
    FOREIGN KEY (user_id) REFERENCES users(user_id);
END
GO

-- Add foreign key for budgets -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_budgets_created_by')
BEGIN
    ALTER TABLE budgets ADD CONSTRAINT FK_budgets_created_by 
    FOREIGN KEY (created_by) REFERENCES users(user_id);
END
GO

-- Add foreign key for expenses -> budgets
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_expenses_budget_id')
BEGIN
    ALTER TABLE expenses ADD CONSTRAINT FK_expenses_budget_id 
    FOREIGN KEY (budget_id) REFERENCES budgets(id);
END
GO

-- Add foreign key for expenses -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_expenses_created_by')
BEGIN
    ALTER TABLE expenses ADD CONSTRAINT FK_expenses_created_by 
    FOREIGN KEY (created_by) REFERENCES users(user_id);
END
GO

-- Add foreign key for services -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_services_created_by')
BEGIN
    ALTER TABLE services ADD CONSTRAINT FK_services_created_by 
    FOREIGN KEY (created_by) REFERENCES users(user_id);
END
GO

-- Add foreign key for contacts -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_contacts_created_by')
BEGIN
    ALTER TABLE contacts ADD CONSTRAINT FK_contacts_created_by 
    FOREIGN KEY (created_by) REFERENCES users(user_id);
END
GO

-- -- Add foreign key for communication_history -> contacts
-- IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_communication_history_contact_id')
-- BEGIN
--     ALTER TABLE communication_history ADD CONSTRAINT FK_communication_history_contact_id
--     FOREIGN KEY (contact_id) REFERENCES contacts(id);
-- END
-- GO

-- Add foreign key for communication_history -> users
-- IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_communication_history_user_id')
-- BEGIN
--     ALTER TABLE communication_history ADD CONSTRAINT FK_communication_history_user_id
--     FOREIGN KEY (user_id) REFERENCES users(user_id);
-- END
-- GO

-- Add foreign key for booking_history -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_booking_history_changed_by')
BEGIN
    ALTER TABLE booking_history ADD CONSTRAINT FK_booking_history_changed_by 
    FOREIGN KEY (changed_by) REFERENCES users(user_id);
END
GO

-- Add foreign key for payment_audit_logs -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_payment_audit_logs_changed_by')
BEGIN
    ALTER TABLE payment_audit_logs ADD CONSTRAINT FK_payment_audit_logs_changed_by 
    FOREIGN KEY (changed_by) REFERENCES users(user_id);
END
GO

-- Add foreign key for system_settings -> users
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_system_settings_updated_by')
BEGIN
    ALTER TABLE system_settings ADD CONSTRAINT FK_system_settings_updated_by 
    FOREIGN KEY (updated_by) REFERENCES users(user_id);
END
GO

PRINT 'All tables created successfully in the adbridge database!';
GO



