# Run All Services in One Terminal ⚡

## 🎯 The Easy Way - Single Command

Instead of opening 5 terminals, just run one command to start everything:

```bash
cd ~/Projects/TrailEquip
./run-all.sh
```

That's it! All 5 services start in the background with logs saved to files.

---

## 📋 What This Does

1. ✅ Checks prerequisites (Java, npm, PostgreSQL)
2. ✅ Checks that all ports are available
3. ✅ Starts Trail Service (8081) in background
4. ✅ Starts Weather Service (8082) in background
5. ✅ Starts Recommendation Service (8083) in background
6. ✅ Starts API Gateway (8080) in background
7. ✅ Starts React UI (3000) in background
8. ✅ Shows you where everything is running
9. ✅ Saves logs to `.logs/` directory
10. ✅ Cleans up everything when you press Ctrl+C

---

## 🚀 Quick Start

### 1. Setup Database (One-time)

```bash
# Create database
createdb trailequip

# Load schema + seed data
psql trailequip < ~/Projects/TrailEquip/infra/db/init.sql

# Verify
psql trailequip -c "SELECT COUNT(*) FROM trails;"
# Should return: 3
```

### 2. Start All Services

```bash
cd ~/Projects/TrailEquip
./run-all.sh
```

### 3. Open in Browser

While the script is running (keep it open), open new browser tabs:

```bash
# Option A: Click these links manually
# UI:                  http://localhost:3000
# API Docs:            http://localhost:8080/swagger-ui.html
# Health:              http://localhost:8080/api/v1/health

# Option B: Use commands to open
open http://localhost:3000
open http://localhost:8080/swagger-ui.html
open http://localhost:8080/api/v1/health
```

### 4. View Logs (in another terminal)

```bash
# View a specific service
./view-logs.sh trail       # Trail Service logs
./view-logs.sh weather     # Weather Service logs
./view-logs.sh gateway     # API Gateway logs
./view-logs.sh ui          # React UI logs

# Or view all logs at once
tail -f .logs/*.log
```

### 5. Stop Everything

In the terminal where `./run-all.sh` is running, press:

```
Ctrl+C
```

All services will stop gracefully.

---

## 📊 Output Example

When you run `./run-all.sh`, you'll see:

```
════════════════════════════════════════════════════════════
       TrailEquip - Starting All Services
════════════════════════════════════════════════════════════

✓ Checking prerequisites...
✓ Prerequisites OK

✓ Checking ports...
✓ All ports available

Starting Trail Service (8081)...
✓ Trail Service started (PID: 12345)
  Log: .logs/trail-service.log

Starting Weather Service (8082)...
✓ Weather Service started (PID: 12346)
  Log: .logs/weather-service.log

Starting Recommendation Service (8083)...
✓ Recommendation Service started (PID: 12347)
  Log: .logs/recommendation-service.log

Starting API Gateway (8080)...
✓ API Gateway started (PID: 12348)
  Log: .logs/api-gateway.log

Starting React UI (3000)...
✓ React UI started (PID: 12349)
  Log: .logs/ui.log

════════════════════════════════════════════════════════════
✓ All services started!
════════════════════════════════════════════════════════════

📍 Services Available:
  UI:                    http://localhost:3000
  API Gateway:          http://localhost:8080
  API Docs (Swagger):   http://localhost:8080/swagger-ui.html
  Health Check:         http://localhost:8080/api/v1/health

📋 Service Logs:
  Trail Service:       tail -f .logs/trail-service.log
  Weather Service:     tail -f .logs/weather-service.log
  Recommendation Svc:  tail -f .logs/recommendation-service.log
  API Gateway:         tail -f .logs/api-gateway.log
  React UI:            tail -f .logs/ui.log

⏹️  To stop all services: Press Ctrl+C
```

---

## 🔍 Monitor Services

While services are running, in **another terminal**:

### View specific service logs:
```bash
./view-logs.sh trail           # Trail Service
./view-logs.sh weather         # Weather Service
./view-logs.sh recommendation  # Recommendation Service
./view-logs.sh gateway         # API Gateway
./view-logs.sh ui              # React UI
```

### View all logs in one view:
```bash
tail -f .logs/*.log
```

### Check service health:
```bash
curl http://localhost:8080/api/v1/health
```

### Test API:
```bash
# List trails
curl http://localhost:8080/api/v1/trails | jq '.'

# Get weather forecast
curl "http://localhost:8080/api/v1/weather/forecast?lat=45.421&lon=25.505&startDate=2024-02-15&endDate=2024-02-16" | jq '.forecastData'
```

---

## 🛠️ Troubleshooting

### Error: "Port X is already in use"

```bash
# Find what's using the port
lsof -i :8080

# Kill the process
kill -9 <PID>

# Then try again
./run-all.sh
```

### Error: "Database 'trailequip' not found"

```bash
# Create it
createdb trailequip
psql trailequip < ~/Projects/TrailEquip/infra/db/init.sql

# Then try again
./run-all.sh
```

### Error: "Java not found"

```bash
# Install Java 21
# Download from: https://jdk.java.net/21/

# Verify after install
java -version
```

### Services won't start

```bash
# Check logs
tail -f .logs/trail-service.log
tail -f .logs/api-gateway.log

# Check if PostgreSQL is running
psql -U postgres -c "SELECT 1;"

# Restart PostgreSQL
brew services restart postgresql
```

---

## 📝 Files

- `run-all.sh` - Main script to start all services
- `view-logs.sh` - Helper script to view service logs
- `.logs/` - Directory where logs are saved (created automatically)

---

## ⚡ Pro Tips

1. **Keep the terminal running** - `./run-all.sh` should keep running in one terminal
2. **Open another terminal** - Use another terminal to view logs or run curl commands
3. **Press Ctrl+C once** - Will gracefully stop all services
4. **Check logs first** - If something breaks, `tail -f .logs/*.log` shows what went wrong
5. **Start fresh** - If stuck, stop with Ctrl+C and run `./run-all.sh` again

---

## 🎉 You're All Set!

```bash
# One command to rule them all:
./run-all.sh

# Then open in browser:
open http://localhost:3000
```

Enjoy TrailEquip! 🚀
