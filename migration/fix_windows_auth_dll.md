# Fix Windows Authentication DLL Issue

## The Problem
The error "Unable to load authentication DLL mssql-jdbc_auth-12.4.2.x64" means the SQL Server JDBC driver is missing the authentication DLL for Windows Authentication.

## Solution: Download and Install Authentication DLL

### Method 1: Download Authentication DLL (Recommended)

1. **Download the authentication DLL:**
   - Go to: https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
   - Download the latest JDBC driver (it includes the authentication DLL)
   - Or download just the authentication DLL: `mssql-jdbc_auth-12.4.2.x64.dll`

2. **Install the DLL:**
   - Copy `mssql-jdbc_auth-12.4.2.x64.dll` to your Java installation directory
   - Usually: `C:\Program Files\Java\jdk-22\bin\`
   - Or: `C:\Program Files\Java\jre-22\bin\`

3. **Alternative locations:**
   - Copy to your project's `src/main/resources` folder
   - Copy to your system's `System32` folder: `C:\Windows\System32\`

### Method 2: Use System Property (Quick Fix)

Add this JVM argument when running your application:
```
-Djava.library.path=C:\path\to\your\dll\folder
```

### Method 3: Alternative JDBC Driver

If the above doesn't work, try using a different JDBC driver version:

```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>11.2.3.jre8</version>
    <scope>runtime</scope>
</dependency>
```

## Test Your Application

After installing the DLL, restart your Spring Boot application. It should connect using your Windows credentials!

## Current Configuration
Your `application.properties` is now set for Windows Authentication:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=adbridge;encrypt=true;trustServerCertificate=true;integratedSecurity=true
spring.datasource.username=
spring.datasource.password=
```



