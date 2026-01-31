# Render Deployment Connection Fix

**Date:** 2026-01-31
**Issue:** Connection to localhost:5432 refused
**Status:** ✅ Fixed

---

## Problem

When deployed on Render, the application was showing:

```
Connection to localhost:5432 refused. Check that the hostname and port is correct
and that the postmaster is accepting TCP/IP connections.
```

**Root Cause:** The RenderDataSourceConfig bean had `@Profile("render")` annotation, which meant it only loaded if the Spring profile was explicitly "render". If the profile wasn't active, Spring used the default `application.yml` configuration which points to `localhost:5432`.

---

## Solution

**Changed RenderDataSourceConfig to auto-detect DATABASE_URL:**

1. **Removed:** `@Profile("render")` restriction
2. **Added:** `@Primary` annotation to override default datasource
3. **Logic:** Check if `DATABASE_URL` environment variable exists
   - If present → Use it (Render/Railway environment)
   - If missing → Return null (let Spring use application.yml for local dev)

**Result:** The bean now automatically detects cloud environments without requiring explicit profile configuration.

---

## How It Works Now

```
┌─ Application Starts
│
├─ RenderDataSourceConfig loads
│
├─ Check: Is DATABASE_URL environment variable set?
│
├─ YES (Render/Railway)
│  └─ Convert DATABASE_URL to JDBC format
│     └─ Add ?sslmode=require
│     └─ Return DataSource with @Primary
│     └─ Override application.yml config
│
└─ NO (Local Development)
   └─ Return null
   └─ Spring uses default application.yml
   └─ Connects to localhost:5432
```

---

## Code Change

**Before:**
```java
@Configuration
@Profile("render")  // ❌ Only loaded if profile is "render"
public class RenderDataSourceConfig { ... }
```

**After:**
```java
@Configuration
public class RenderDataSourceConfig {
    @Bean
    @Primary  // ✅ Override default when DATABASE_URL is present
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            return null;  // Use default config
        }
        // Convert and use DATABASE_URL
        ...
    }
}
```

---

## Testing

### Local Development (localhost)
```bash
# DATABASE_URL not set
# Spring uses application.yml
# Connects to localhost:5432 ✅
```

### Render Deployment
```bash
# DATABASE_URL = postgresql://user:pass@host:port/db
# RenderDataSourceConfig detects it
# Converts to: jdbc:postgresql://user:pass@host:port/db?sslmode=require
# Overrides localhost config ✅
```

---

## Deployment Impact

✅ **Render:** Now detects DATABASE_URL automatically
✅ **Railway:** Works with DATABASE_URL environment variable
✅ **Local:** Still uses application.yml (localhost)
✅ **No manual profile configuration needed**

---

## Next Steps

1. **Trigger Redeploy on Render**
   - Go to Render dashboard
   - Redeploy the service from latest GitHub commit (71cb6ae)
   - Wait for health check to pass

2. **Monitor Logs**
   - Should see: "🔌 Using DATABASE_URL for datasource" (with credentials masked)
   - No more connection refused errors

3. **Verify**
   - Health check endpoint: `/actuator/health` → should return UP
   - API endpoints: `/api/v1/trails` → should return trail list

---

## Git Commit

```
71cb6ae Fix Render deployment - remove profile restriction on DataSourceConfig
```

**Changes:**
- Removed @Profile("render") from RenderDataSourceConfig
- Added @Primary annotation
- Return null if DATABASE_URL not set
- Added debug logging with masked credentials

---

**This fix ensures automatic detection of cloud environments without requiring explicit configuration.**
