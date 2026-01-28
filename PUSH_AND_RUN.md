# Push to GitLab & Run TrailEquip

## 📍 Your Complete Setup Guide

Follow these steps to push your code to GitLab and run it locally.

---

## **Step 1: Push to GitLab** (5 minutes)

### 1.1 Create GitLab Project

Go to: https://gitlab.com/dashboard/projects

Click:
1. "New project"
2. "Create blank project"
3. Enter project name: `trail-equip`
4. Click "Create project"

### 1.2 Get Your GitLab Username

Go to: https://gitlab.com/-/profile

Copy your username (shown at top of page, e.g., `john-doe`)

### 1.3 Push Code to GitLab

```bash
cd ~/Projects/TrailEquip

# Replace YOUR-USERNAME with your actual GitLab username
git remote add gitlab https://gitlab.com/YOUR-USERNAME/trail-equip.git

# Verify it worked
git remote -v

# Push code
git push -u gitlab main
```

**Expected output:**
```
Enumerating objects: ...
Writing objects: 100% ...
To https://gitlab.com/YOUR-USERNAME/trail-equip.git
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'gitlab'.
```

### 1.4 Verify on GitLab

Open: https://gitlab.com/YOUR-USERNAME/trail-equip

You should see:
- All your files listed
- 10 commits visible
- CI/CD pipeline started (or waiting)

---

## **Step 2: Setup Local Environment** (10 minutes)

On your laptop, run:

```bash
# Install dependencies (one-time)
brew install postgresql postgis

# Create database
createdb trailequip

# Load schema + sample trails
psql trailequip < ~/Projects/TrailEquip/infra/db/init.sql

# Verify
psql trailequip -c "SELECT COUNT(*) FROM trails;"
# Should return: 3
```

---

## **Step 3: Run TrailEquip** (Single Terminal! 🎉)

```bash
cd ~/Projects/TrailEquip
./run-all.sh
```

**You'll see:**
```
════════════════════════════════════════════════════════════
       TrailEquip - Starting All Services
════════════════════════════════════════════════════════════

✓ Prerequisites OK
✓ All ports available
✓ Trail Service started (PID: 12345)
✓ Weather Service started (PID: 12346)
✓ Recommendation Service started (PID: 12347)
✓ API Gateway started (PID: 12348)
✓ React UI started (PID: 12349)

📍 Services Available:
  UI:                    http://localhost:3000
  API Gateway:          http://localhost:8080
  API Docs (Swagger):   http://localhost:8080/swagger-ui.html

⏹️  To stop all services: Press Ctrl+C
```

**Keep this terminal open!**

---

## **Step 4: Open in Browser** (New terminal tab)

```bash
# Open UI
open http://localhost:3000

# Open API Docs (try out endpoints!)
open http://localhost:8080/swagger-ui.html
```

**You should see:**
- ✅ Trail list with 3 Bucegi mountains trails
- ✅ Trail details (click one to see info)
- ✅ API Swagger UI with all endpoints
- ✅ Health check endpoint working

---

## **Step 5: Play With It!** 🎮

### Test the API (in another terminal):

```bash
# List all trails
curl http://localhost:8080/api/v1/trails | jq '.'

# Get a specific trail
curl http://localhost:8080/api/v1/trails/550e8400-e29b-41d4-a716-446655440001 | jq '.'

# Get weather forecast
curl "http://localhost:8080/api/v1/weather/forecast?lat=45.421&lon=25.505&startDate=2024-02-15&endDate=2024-02-16" | jq '.forecastData'

# Get equipment recommendations
curl -X POST http://localhost:8080/api/v1/recommendations/equipment \
  -H "Content-Type: application/json" \
  -d '{
    "trailId": "550e8400-e29b-41d4-a716-446655440001",
    "forecastStart": "2024-02-15T09:00:00Z",
    "forecastEnd": "2024-02-15T18:00:00Z",
    "timezone": "Europe/Bucharest"
  }' | jq '.'

# Check health
curl http://localhost:8080/api/v1/health | jq '.'
```

### View Logs (in another terminal):

```bash
# Watch all logs
tail -f .logs/*.log

# Or specific service
./view-logs.sh trail
./view-logs.sh gateway
./view-logs.sh ui
```

---

## **Step 6: Stop Everything**

In the terminal where `./run-all.sh` is running:

```
Press Ctrl+C
```

All services stop gracefully.

---

## 📊 What You Have Now

✅ Code pushed to GitLab at:
   https://gitlab.com/YOUR-USERNAME/trail-equip

✅ All 5 services running locally:
   - Trail Service (8081)
   - Weather Service (8082)
   - Recommendation Service (8083)
   - API Gateway (8080)
   - React UI (3000)

✅ Database with 3 sample trails

✅ CI/CD pipeline in GitLab watching your code

---

## 🛠️ Troubleshooting

### "port already in use"
```bash
lsof -i :8080
kill -9 <PID>
./run-all.sh
```

### "Database not found"
```bash
createdb trailequip
psql trailequip < ~/Projects/TrailEquip/infra/db/init.sql
./run-all.sh
```

### Services won't start
```bash
# Check logs
tail -f .logs/api-gateway.log

# Make sure PostgreSQL is running
brew services start postgresql
```

### Can't push to GitLab
```bash
# Verify remote is correct
git remote -v

# Should show:
# gitlab  https://gitlab.com/YOUR-USERNAME/trail-equip.git (fetch)
# gitlab  https://gitlab.com/YOUR-USERNAME/trail-equip.git (push)

# If wrong, remove and re-add
git remote remove gitlab
git remote add gitlab https://gitlab.com/YOUR-USERNAME/trail-equip.git
git push -u gitlab main
```

---

## 🎉 You're Done!

You now have:
✅ TrailEquip code in GitLab
✅ All services running locally
✅ UI in browser
✅ API documentation
✅ Sample data with real Bucegi trails

**Start exploring and building!** 🚀

---

## 📚 Next Steps

1. **Modify the code** - Change anything in `services/` or `ui/`
2. **Git commit** - `git add . && git commit -m "Your message"`
3. **Push to GitLab** - `git push gitlab main`
4. **Watch CI/CD** - Go to https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines
5. **See pipeline run** - Tests and builds run automatically!

Enjoy! 🎯
