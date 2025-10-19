# SQL Server Connection Troubleshooting Guide

## Current Issue: Login failed for user 'sa'

The application cannot connect to SQL Server because of authentication issues. Here are the steps to fix this:

## Step 1: Check SQL Server Authentication Mode

1. **Open SQL Server Management Studio (SSMS)**
2. **Connect to your SQL Server instance**
3. **Right-click on the server name** → **Properties**
4. **Go to Security tab**
5. **Make sure "SQL Server and Windows Authentication mode" is selected**
6. **Click OK and restart SQL Server service**

## Step 2: Enable SA Account

1. **In SSMS, expand Security** → **Logins**
2. **Right-click on 'sa'** → **Properties**
3. **Go to General tab**:
   - **Set a strong password** (remember this password!)
   - **Uncheck "Enforce password policy"** if needed
4. **Go to Status tab**:
   - **Set "Login" to "Enabled"**
5. **Click OK**

## Step 3: Update Application Properties

Update your `src/main/resources/application.properties` with the correct credentials:

```properties
# SQL Server Database Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=adbridge;encrypt=true;trustServerCertificate=true;integratedSecurity=false
spring.datasource.username=sa
spring.datasource.password=YOUR_ACTUAL_SA_PASSWORD
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

## Step 4: Alternative - Use Windows Authentication

If you prefer Windows Authentication, update the connection string:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=adbridge;encrypt=true;trustServerCertificate=true;integratedSecurity=true
spring.datasource.username=
spring.datasource.password=
```

## Step 5: Create Database User (Alternative to SA)

Instead of using SA, create a dedicated database user:

```sql
-- Run this in SSMS
USE master;
GO

-- Create login
CREATE LOGIN adbridge_user WITH PASSWORD = 'YourSecurePassword123!';
GO

-- Create user in adbridge database
USE adbridge;
GO

CREATE USER adbridge_user FOR LOGIN adbridge_user;
GO

-- Grant permissions
ALTER ROLE db_owner ADD MEMBER adbridge_user;
GO
```

Then update your application.properties:
```properties
spring.datasource.username=adbridge_user
spring.datasource.password=YourSecurePassword123!
```

## Step 6: Test Connection

1. **Run the test script**: `migration/test_connection.sql` in SSMS
2. **Check if tables exist** in your adbridge database
3. **Start your Spring Boot application**

## Common Issues and Solutions

### Issue 1: "Login failed for user 'sa'"
- **Solution**: Enable SA account and set password
- **Alternative**: Create a new database user

### Issue 2: "Cannot connect to server"
- **Solution**: Check if SQL Server is running
- **Check**: Windows Services → SQL Server (MSSQLSERVER)

### Issue 3: "Database 'adbridge' does not exist"
- **Solution**: Run the table creation script first
- **Script**: `migration/create_tables_sqlserver.sql`

### Issue 4: "Port 1433 not accessible"
- **Solution**: Enable TCP/IP in SQL Server Configuration Manager
- **Check**: SQL Server Network Configuration → Protocols for MSSQLSERVER → TCP/IP → Enabled

## Quick Fix Commands

If you have access to SQL Server, run these commands:

```sql
-- Enable SA account
ALTER LOGIN sa ENABLE;
ALTER LOGIN sa WITH PASSWORD = 'YourNewPassword123!';

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'adbridge')
BEGIN
    CREATE DATABASE adbridge;
END
```

## Next Steps

1. **Fix the authentication issue** using one of the methods above
2. **Update your application.properties** with correct credentials
3. **Run the table creation script** if you haven't already
4. **Start your Spring Boot application**

Your application should now connect successfully to SQL Server!



