# Fix SQL Server Browser Service Issue

## The Problem
The error shows: "Verify that the SQL Server Browser Service is running on the host"

## Quick Fix - Start SQL Server Browser Service

### Method 1: Using Services (Easiest)
1. **Press `Windows + R`**
2. **Type `services.msc` and press Enter**
3. **Find "SQL Server Browser"**
4. **Right-click → Start**
5. **Right-click → Properties → Set Startup Type to "Automatic"**

### Method 2: Using Command Prompt
1. **Press `Windows + X`**
2. **Select "Windows PowerShell (Admin)"**
3. **Run these commands:**
```cmd
net start "SQL Server Browser"
sc config "SQL Server Browser" start= auto
```

### Method 3: Alternative Connection String
If Browser Service won't start, use direct port connection:

Update your `application.properties`:
```properties
# Use direct port instead of named instance
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=adbridge;encrypt=true;trustServerCertificate=true;integratedSecurity=true
```

## Check SQL Server Express Port
1. **Open SQL Server Configuration Manager**
2. **Go to: SQL Server Network Configuration → Protocols for SQLEXPRESS**
3. **Right-click TCP/IP → Properties**
4. **Go to IP Addresses tab**
5. **Find "IPAll" section**
6. **Note the TCP Port (usually 1433 or 1434)**

## Alternative: Use Default Instance
If you have SQL Server default instance running:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=adbridge;encrypt=true;trustServerCertificate=true;integratedSecurity=true
```

## Test Connection
After starting Browser Service, test your connection in SSMS first, then start your Spring Boot application.



