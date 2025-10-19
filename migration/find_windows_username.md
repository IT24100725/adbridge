# How to Find Your Windows Username for SQL Server

## Method 1: Using Command Prompt
1. **Press `Windows + R`**
2. **Type `cmd` and press Enter**
3. **Type `whoami` and press Enter**
4. **Copy the result** (e.g., `DOMAIN\username` or `COMPUTERNAME\username`)

## Method 2: Using PowerShell
1. **Press `Windows + X`**
2. **Select "Windows PowerShell"**
3. **Type `$env:USERNAME` and press Enter**
4. **Also type `$env:USERDOMAIN` to get the domain**

## Method 3: Check Current User in SQL Server
Run this query in SSMS to see who you're currently logged in as:
```sql
SELECT SUSER_NAME() AS 'Current Login',
       USER_NAME() AS 'Current User',
       SYSTEM_USER AS 'System User';
```

## Method 4: Check Available Logins
Run this query to see all available logins:
```sql
SELECT name, type_desc, is_disabled 
FROM sys.server_principals 
WHERE type IN ('S', 'U', 'G')
ORDER BY name;
```

## Common Windows Username Formats:
- `COMPUTERNAME\username` (for local accounts)
- `DOMAIN\username` (for domain accounts)
- `username` (just the username part)

## Example:
If your computer name is "DESKTOP-ABC123" and your username is "john", you would use:
- `DESKTOP-ABC123\john` (full format)
- `john` (short format)

## For SQL Server Permissions:
Use the **full format** (with computer name or domain) when granting permissions.



