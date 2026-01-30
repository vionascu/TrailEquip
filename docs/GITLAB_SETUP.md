# GitLab Setup Guide for TrailEquip

## 🎯 Quick Start with GitLab

Since you have a GitLab account, here are the best ways to run TrailEquip:

---

## **Option 1: Push to GitLab & Run CI/CD in Cloud** (Easiest)

No local setup needed. GitLab's cloud runners build and test your code.

### **Step 1: Create GitLab Repository**

```bash
# Option A: Use web UI
# 1. Go to https://gitlab.com/projects/new
# 2. Create new project "trail-equip"
# 3. Choose "Create blank project"

# Option B: Use GitLab CLI
# Install: https://gitlab.com/cli/cli/-/releases
# Then:
gitlab project create --name trail-equip
```

### **Step 2: Push Your Code**

```bash
cd ~/Projects/TrailEquip

# Add GitLab remote
git remote add gitlab https://gitlab.com/YOUR-USERNAME/trail-equip.git

# Push main branch
git push -u gitlab main
```

### **Step 3: Watch Pipeline Run**

```bash
# Open browser
https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines
```

**You'll see:**
- ✅ Build stage (5-10 min) - Compiles Java + React
- ✅ Test stage (5-10 min) - Unit tests, linting
- ✅ Package stage - Docker images created
- ✅ Deploy stage - Documentation

**View artifacts after build:**
- https://gitlab.com/YOUR-USERNAME/trail-equip/-/jobs
- Download JAR files, React dist, test reports

---

## **Option 2: Install GitLab Runner Locally** (Best for Development)

Run CI/CD jobs on your machine, triggered by GitLab.

### **Step 1: Install GitLab Runner**

```bash
# Mac (using Homebrew)
brew install gitlab-runner

# Windows
# Download: https://docs.gitlab.com/runner/install/windows.html

# Linux (Debian/Ubuntu)
sudo apt-get update
sudo apt-get install gitlab-runner
```

**Verify installation:**
```bash
gitlab-runner --version
```

### **Step 2: Get Registration Token**

```bash
# Go to: https://gitlab.com/YOUR-USERNAME/trail-equip/-/settings/ci_cd
# Find "Runners" section
# Copy the "Runner registration token" (looks like: GR1348941...)
```

### **Step 3: Register Runner**

```bash
gitlab-runner register

# When prompted, enter:
# GitLab instance URL: https://gitlab.com/
# Registration token: (paste from Step 2)
# Runner description: local-dev
# Tags: local
# Executor: shell
# Shell: bash
```

**Verify:**
```bash
gitlab-runner list
```

### **Step 4: Start Runner**

```bash
# Start runner (keep running in background)
gitlab-runner run

# Or run as service
sudo gitlab-runner install
sudo gitlab-runner start
```

### **Step 5: Push Code & Watch It Build**

```bash
cd ~/Projects/TrailEquip

# Make a change (or just re-push)
git push gitlab main

# Watch pipeline: https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines
# Jobs will run on YOUR machine!
```

---

## **Option 3: Run Services Directly (Without CI/CD)**

Skip CI/CD and just run the services locally.

### **Prerequisites:**

```bash
# Java 21+
java -version

# npm
npm --version

# PostgreSQL + PostGIS
# Mac: brew install postgresql postgis
# Ubuntu: sudo apt-get install postgresql postgresql-contrib postgis
# Windows: https://www.postgresql.org/download/windows/
```

### **Setup Database:**

```bash
# Start PostgreSQL
# Mac: brew services start postgresql
# Ubuntu: sudo service postgresql start
# Windows: Start PostgreSQL service

# Create database
createdb trailequip

# Load schema + seed data
psql trailequip < ~/Projects/TrailEquip/infra/db/init.sql

# Verify
psql trailequip -c "SELECT COUNT(*) FROM trails;"
# Should return: 3
```

### **Run Services (5 Terminals):**

**Terminal 1: Trail Service**
```bash
cd ~/Projects/TrailEquip
./gradlew :services:trail-service:bootRun
# Runs on http://localhost:8081
```

**Terminal 2: Weather Service**
```bash
cd ~/Projects/TrailEquip
./gradlew :services:weather-service:bootRun
# Runs on http://localhost:8082
```

**Terminal 3: Recommendation Service**
```bash
cd ~/Projects/TrailEquip
./gradlew :services:recommendation-service:bootRun
# Runs on http://localhost:8083
```

**Terminal 4: API Gateway**
```bash
cd ~/Projects/TrailEquip
./gradlew :services:api-gateway:bootRun
# Runs on http://localhost:8080
```

**Terminal 5: React UI**
```bash
cd ~/Projects/TrailEquip/ui
npm install
npm run dev
# Runs on http://localhost:3000
```

### **Test:**

```bash
curl http://localhost:8080/api/v1/health
open http://localhost:3000
open http://localhost:8080/swagger-ui.html
```

---

## **Option 4: Use GitLab Runner with Modified Pipeline** (Recommended)

Use the alternative CI/CD config that doesn't require Docker.

### **Step 1: Install GitLab Runner** (as above)

### **Step 2: Use Alternative Pipeline**

```bash
cd ~/Projects/TrailEquip

# Replace default pipeline
cp .gitlab-ci-no-docker.yml .gitlab-ci.yml

# Commit and push
git add .gitlab-ci.yml
git commit -m "Use shell executor CI/CD"
git push gitlab main
```

### **Step 3: Configure Runner Tag**

```bash
# Make sure your runner has tag "local"
gitlab-runner register
# When asked for tags, enter: local
```

### **Step 4: Push Triggers Pipeline**

```bash
cd ~/Projects/TrailEquip
git push gitlab main

# Pipeline runs on your machine via GitLab Runner
# Watch: https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines
```

---

## **Option 5: GitLab Pages + UI** (View UI in Cloud)

Deploy just the React UI to GitLab Pages for viewing in browser.

### **Setup:**

```bash
# Edit .gitlab-ci.yml to add pages job:
cat >> .gitlab-ci.yml << 'EOF'

pages:
  stage: deploy
  artifacts:
    paths:
      - public
  script:
    - mkdir -p public
    - cp -r ui/dist/* public/ || true
  only:
    - main
EOF

# Commit and push
git add .gitlab-ci.yml
git commit -m "Add GitLab Pages deployment"
git push gitlab main
```

**Result:**
- UI deployed at: `https://YOUR-USERNAME.gitlab.io/trail-equip`
- Updates automatically on each push to main

---

## **Option 6: GitLab Environments (Advanced)**

Create deployment environments to track running services.

```bash
# Edit .gitlab-ci.yml
cat >> .gitlab-ci.yml << 'EOF'

deploy_staging:
  stage: deploy
  environment:
    name: staging
    url: http://localhost:8080
  script:
    - echo "Services ready to run"
    - echo "Trail Service: http://localhost:8081"
    - echo "API Gateway: http://localhost:8080"
  only:
    - main
EOF

git add .gitlab-ci.yml
git commit -m "Add staging environment"
git push gitlab main
```

**View environments:**
https://gitlab.com/YOUR-USERNAME/trail-equip/-/environments

---

## **Comparison Table**

| Option | Setup Time | CI/CD | Local Dev | Cloud |
|--------|-----------|-------|-----------|-------|
| **Option 1: Push & Wait** | 5 min | ✅ Cloud | ❌ No | ✅ Yes |
| **Option 2: GitLab Runner** | 15 min | ✅ Local | ✅ Yes | ❌ No |
| **Option 3: Direct Run** | 20 min | ❌ No | ✅ Yes | ❌ No |
| **Option 4: Runner + Shell** | 15 min | ✅ Local | ✅ Yes | ❌ No |
| **Option 5: Pages UI** | 10 min | ✅ Cloud | ❌ Limited | ✅ UI only |
| **Option 6: Environments** | 10 min | ✅ Cloud | ✅ Yes | ✅ Tracked |

---

## **My Recommendation for You**

### **Best Setup: Option 2 + Option 3**

1. **Install GitLab Runner** (one-time, 5 min)
   ```bash
   brew install gitlab-runner
   gitlab-runner register  # Tag: local
   gitlab-runner run
   ```

2. **Run services locally** (5 terminals):
   ```bash
   # Terminal 1-4: Run services
   ./gradlew :services:trail-service:bootRun
   # ... repeat for other services

   # Terminal 5: Run UI
   cd ui && npm run dev
   ```

3. **Push to GitLab**:
   ```bash
   git push gitlab main
   ```

4. **See CI/CD run on your machine**:
   ```
   https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines
   ```

**Benefits:**
- ✅ Local development works perfectly
- ✅ CI/CD runs on your machine automatically
- ✅ Builds verified before pushing
- ✅ Test reports in GitLab
- ✅ Artifacts available for download

---

## **Step-by-Step for You**

```bash
# 1. Install GitLab Runner
brew install gitlab-runner

# 2. Register runner
gitlab-runner register
# Enter:
#   URL: https://gitlab.com/
#   Token: (from https://gitlab.com/YOUR-USERNAME/trail-equip/-/settings/ci_cd)
#   Description: local
#   Tags: local
#   Executor: shell

# 3. Start runner
gitlab-runner run

# 4. In new terminal, push code
cd ~/Projects/TrailEquip
git remote add gitlab https://gitlab.com/YOUR-USERNAME/trail-equip.git
git push -u gitlab main

# 5. Watch pipeline
# https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines

# 6. Run services locally (separate terminals)
./gradlew :services:trail-service:bootRun
./gradlew :services:weather-service:bootRun
./gradlew :services:recommendation-service:bootRun
./gradlew :services:api-gateway:bootRun
cd ui && npm run dev

# 7. Access
# http://localhost:3000 (UI)
# http://localhost:8080/swagger-ui.html (API docs)
```

---

## **Troubleshooting**

### **Runner not picking up jobs**

```bash
# Check runner status
gitlab-runner verify

# Check runner tags
gitlab-runner list

# Make sure .gitlab-ci.yml has matching tags
# - jobs should have: tags: [local]
```

### **Jobs failing with "image not found"**

```bash
# Use .gitlab-ci-no-docker.yml instead
cp .gitlab-ci-no-docker.yml .gitlab-ci.yml
git push gitlab main
```

### **Need to stop runner**

```bash
# Press Ctrl+C in terminal running gitlab-runner run
# OR if running as service:
sudo gitlab-runner stop
```

---

## **Next Steps**

For deployment details, see [DEPLOYMENT_SUMMARY.md](DEPLOYMENT_SUMMARY.md)

For local PostgreSQL setup, see [POSTGRES_LOCAL_SETUP.md](POSTGRES_LOCAL_SETUP.md)

For general setup, see [STARTUP.md](STARTUP.md)

---

**You now have full control over TrailEquip with GitLab! 🚀**
