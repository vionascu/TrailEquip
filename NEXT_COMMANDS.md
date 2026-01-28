# Your Next Commands to Run

## Copy-Paste Ready Commands

After Bootstrap is Complete, Run These on Your Laptop:

---

## 🎯 IMMEDIATE: Run Locally (Right Now on Your Laptop)

```bash
# Step 1: Navigate to project
cd ~/Projects/TrailEquip/infra

# Step 2: Start all services (if Docker is installed and running)
docker-compose up -d

# Step 3: Wait for initialization
sleep 30

# Step 4: Test API health
curl http://localhost:8080/api/v1/health

# Step 5: Open in browser
open http://localhost:3000
open http://localhost:8080/swagger-ui.html
```

**Expected Output:**
- Terminal: `{"status":"UP","services":{"trail-service":"UP",...}}`
- Browser 1: Trail list with 3 trails displayed
- Browser 2: Swagger API documentation

---

## 🛠️ DEVELOPMENT: Make Changes and Test

```bash
# If you modify Java code in services/trail-service/...
cd ~/Projects/TrailEquip/infra
docker-compose restart trail-service
docker-compose logs -f trail-service

# If you modify React code in ui/src/...
# Just save the file - Vite auto-reloads at http://localhost:3000

# Run Java tests (from laptop, requires local Gradle/JDK)
cd ~/Projects/TrailEquip
gradle test integrationTest

# View database
docker exec -it trailequip-db psql -U trailequip -d trailequip
# Then: SELECT * FROM trails;
```

---

## 🌐 GIT: Push to GitLab

```bash
# Step 1: Create new project on GitLab.com
# Go to https://gitlab.com/new and create "trail-equip"

# Step 2: Add remote
cd ~/Projects/TrailEquip
git remote add origin https://gitlab.com/YOUR-USERNAME/trail-equip.git

# Step 3: Push to GitLab
git push -u origin main

# Step 4: Check CI/CD
# Visit: https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines
```

**What happens automatically:**
- GitLab detects `.gitlab-ci.yml`
- Build stage runs: `gradle build -x test` + `npm build`
- Test stage runs: `gradle test` + linting
- Package stage runs: Docker images built
- Results appear in pipeline view

---

## 🧪 VERIFY SETUP

```bash
# Confirm all services running
cd ~/Projects/TrailEquip/infra
docker-compose ps

# Test all endpoints
curl http://localhost:8080/api/v1/trails | jq '.'
curl "http://localhost:8080/api/v1/weather/forecast?lat=45.421&lon=25.505&startDate=2024-02-15&endDate=2024-02-16" | jq '.'
curl -X POST http://localhost:8080/api/v1/recommendations/equipment \
  -H "Content-Type: application/json" \
  -d '{"trailId":"550e8400-e29b-41d4-a716-446655440001","forecastStart":"2024-02-15T09:00:00Z","forecastEnd":"2024-02-15T18:00:00Z"}'

# View logs
docker-compose logs -f api-gateway
docker-compose logs -f trail-service
docker-compose logs -f postgres

# Database queries
docker exec -it trailequip-db psql -U trailequip -d trailequip -c "SELECT COUNT(*) FROM trails;"
```

---

## 🛑 STOP & CLEAN UP

```bash
# Stop containers (keep data)
cd ~/Projects/TrailEquip/infra
docker-compose stop

# Restart containers
docker-compose restart

# Stop and remove containers (keep data)
docker-compose down

# Stop and remove everything (delete database)
docker-compose down -v
```

---

## 📂 PROJECT LOCATIONS

```
~/Projects/TrailEquip/
├── services/trail-service/        ← Modify Trail service code here
├── services/weather-service/      ← Modify Weather service code here
├── services/recommendation-service/ ← Modify Recommendation service code here
├── services/api-gateway/          ← Modify Gateway code here
├── ui/                            ← Modify React UI code here
│   └── src/App.tsx
├── infra/
│   ├── docker-compose.yml         ← Service orchestration
│   └── db/init.sql                ← Database schema
├── .gitlab-ci.yml                 ← CI/CD pipeline
└── docs/                          ← Documentation
```

---

## ⚡ QUICK REFERENCE

| Action | Command |
|--------|---------|
| Start services | `cd infra && docker-compose up -d` |
| Stop services | `cd infra && docker-compose down` |
| View logs | `docker-compose logs -f trail-service` |
| Open UI | `open http://localhost:3000` |
| Test API | `curl http://localhost:8080/api/v1/health` |
| View docs | `open http://localhost:8080/swagger-ui.html` |
| Database | `docker exec -it trailequip-db psql -U trailequip` |
| Push to Git | `git push -u origin main` |
| Check CI/CD | `https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines` |

---

## 🎯 YOUR FIRST 5 MINUTES

```bash
# 1. Install Docker Desktop (5 min if not installed)
# Download from https://www.docker.com/products/docker-desktop

# 2. Start services (1 min)
cd ~/Projects/TrailEquip/infra && docker-compose up -d && sleep 30

# 3. Test (1 min)
curl http://localhost:8080/api/v1/health

# 4. Open UI (1 min)
open http://localhost:3000

# 5. Celebrate! 🎉
```

---

## 💡 TIPS

1. **First run takes longer** – Docker pulls images and builds services (5-10 min)
2. **Logs are your friend** – Use `docker-compose logs -f SERVICE_NAME` to debug
3. **Don't forget sleep** – Wait 30 seconds after `docker-compose up -d` for services to initialize
4. **Use jq** – Install with `brew install jq` for pretty JSON output
5. **Check ports** – If port in use: `lsof -i :8080` then `kill -9 <PID>`

---

## 🚀 READY TO BEGIN?

Run this NOW on your laptop:

```bash
cd ~/Projects/TrailEquip/infra && docker-compose up -d && sleep 30 && curl http://localhost:8080/api/v1/health && open http://localhost:3000
```

All in one command! ⚡

---

**Questions?** Check `SETUP_INSTRUCTIONS.md` for troubleshooting.

**Happy coding! 🎉**
