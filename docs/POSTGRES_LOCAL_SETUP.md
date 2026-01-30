# PostgreSQL Local Setup Guide

This guide documents how to set up PostgreSQL locally for development without Docker.

## Prerequisites

- macOS with Homebrew installed (or equivalent package manager for your OS)
- No sudo/admin access required

---

## Step 1: Install PostgreSQL via Homebrew

```bash
brew install postgresql
```

---

## Step 2: Start PostgreSQL Service

Start the PostgreSQL service using Homebrew:

```bash
brew services start postgresql
```

**Verify it's running:**
```bash
brew services list
```

You should see `postgresql` with status `started`.

---

## Step 3: Initialize Database in Home Directory

Since you may not have sudo access, initialize PostgreSQL in your home directory:

```bash
rm -rf ~/postgres_data    # Clean up any previous attempts
initdb ~/postgres_data
pg_ctl -D ~/postgres_data start
```

**Optional:** Add logging to the startup command:
```bash
pg_ctl -D ~/postgres_data -l ~/postgres_data/logfile start
```

**Check if PostgreSQL is running:**
```bash
pg_ctl -D ~/postgres_data status
```

---

## Step 4: Create the TrailEquip Database

Connect to PostgreSQL using your system username (Homebrew doesn't create a `postgres` user):

```bash
psql -h localhost -U $(whoami) -d postgres -c "CREATE DATABASE trailequip;"
```

**Note:** If you get "database does not exist" error, this is normal. The `-d postgres` flag connects to the default database first.

---

## Step 5: Create a Database User (Optional)

For better security, create a dedicated user:

```bash
psql -h localhost -U $(whoami) -d postgres -c "CREATE USER trailequip WITH PASSWORD 'your_password';"
psql -h localhost -U $(whoami) -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE trailequip TO trailequip;"
```

---

## Step 6: Verify Database Creation

List all databases:

```bash
psql -h localhost -U $(whoami) -d postgres -c "\l"
```

You should see `trailequip` in the list.

Connect to it:

```bash
psql -h localhost -U $(whoami) -d trailequip
```

Exit with:
```
\q
```

---

## Step 7: Configure Spring Boot Application

Update `services/api-gateway/src/main/resources/application.yml`:

### Option A: Using your system username (no password)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trailequip
    username: viionascu
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Option B: Using dedicated trailequip user (with password)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trailequip
    username: trailequip
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## Step 8: Start Your Application

Your Spring Boot app should now connect to PostgreSQL on startup.

Tables will be created automatically via Hibernate (because of `ddl-auto: update`).

---

## Useful Commands

### Stop PostgreSQL

```bash
pg_ctl -D ~/postgres_data stop
```

Or via Homebrew:
```bash
brew services stop postgresql
```

### Start PostgreSQL (after stopping)

```bash
pg_ctl -D ~/postgres_data start
```

### View PostgreSQL logs

```bash
tail -f ~/postgres_data/logfile
```

### Connect to database

```bash
psql -h localhost -U viionascu -d trailequip
```

### Reset database (delete all data)

```bash
pg_ctl -D ~/postgres_data stop
rm -rf ~/postgres_data
initdb ~/postgres_data
pg_ctl -D ~/postgres_data start
# Then recreate the database
psql -h localhost -U $(whoami) -d postgres -c "CREATE DATABASE trailequip;"
```

---

## Troubleshooting

### Connection fails: "could not bind IPv4 address"

This means PostgreSQL or another process is already using port 5432. Stop any running instance:

```bash
brew services stop postgresql
pg_ctl -D ~/postgres_data stop  # If you started it manually
```

### Connection fails: "FATAL: database does not exist"

The database directory exists but is not initialized. Clean up and reinitialize:

```bash
rm -rf ~/postgres_data
initdb ~/postgres_data
pg_ctl -D ~/postgres_data start
psql -h localhost -U $(whoami) -d postgres -c "CREATE DATABASE trailequip;"
```

### Permission denied creating directory

If you see "Permission denied" errors, make sure you're using your home directory:

```bash
initdb ~/postgres_data  # Correct ✓
initdb /usr/local/var/postgres  # Wrong - requires sudo ✗
```

### psql: command not found

PostgreSQL might not be in your PATH. Try:

```bash
/usr/local/bin/psql -h localhost -U $(whoami)
```

Or add to your shell profile (`~/.bash_profile` or `~/.zshrc`):

```bash
export PATH="/usr/local/bin:$PATH"
```

---

## Environment Setup (One-Time)

To make this easier in the future, you can automate the startup:

**Create a shell script** `~/start_postgres.sh`:

```bash
#!/bin/bash
pg_ctl -D ~/postgres_data -l ~/postgres_data/logfile start
echo "PostgreSQL started"
```

Make it executable:

```bash
chmod +x ~/start_postgres.sh
```

Then just run:

```bash
~/start_postgres.sh
```

---

## Summary

1. ✅ Install PostgreSQL via Homebrew
2. ✅ Initialize database in home directory (`~/postgres_data`)
3. ✅ Start PostgreSQL service
4. ✅ Create `trailequip` database
5. ✅ Configure Spring Boot `application.yml`
6. ✅ Start your application

PostgreSQL will now persist data locally and your Spring Boot app will manage the schema.

---

**Last Updated:** January 30, 2026
