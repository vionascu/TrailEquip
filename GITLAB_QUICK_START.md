# GitLab Quick Start (No Docker Needed) ⚡

## **FASTEST WAY: 3 Steps**

### **Step 1: Push to GitLab (2 minutes)**

```bash
cd ~/Projects/TrailEquip

# Create project on https://gitlab.com/projects/new (call it "trail-equip")
# Then push:

git remote add gitlab https://gitlab.com/YOUR-USERNAME/trail-equip.git
git push -u gitlab main
```

### **Step 2: Watch Cloud Pipeline (5-10 minutes)**

```
Open: https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines
```

**What you'll see:**
- ✅ Build running (Gradle compiles Java, npm builds React)
- ✅ Tests running
- ✅ Artifacts created (JAR files, React dist)
- ✅ Success! ✨

**No installation needed. GitLab's cloud runners do the work.**

### **Step 3: Download Artifacts**

```
Click the pipeline → Click build job → Download artifacts
- services/trail-service/build/libs/trail-service-0.1.0-SNAPSHOT.jar
- services/weather-service/build/libs/...
- services/recommendation-service/build/libs/...
- services/api-gateway/build/libs/...
- ui/dist/
```

---

## **Want to Run Locally? (15 minutes)**

```bash
# Prerequisites
java -version          # Should be Java 21+
npm --version          # Should be npm 20+
brew install postgresql postgis  # Or your OS equivalent

# Setup database
createdb trailequip
psql trailequip < ~/Projects/TrailEquip/infra/db/init.sql

# Run 5 services (in 5 different terminals)

# Terminal 1
cd ~/Projects/TrailEquip && ./gradlew :services:trail-service:bootRun

# Terminal 2
cd ~/Projects/TrailEquip && ./gradlew :services:weather-service:bootRun

# Terminal 3
cd ~/Projects/TrailEquip && ./gradlew :services:recommendation-service:bootRun

# Terminal 4
cd ~/Projects/TrailEquip && ./gradlew :services:api-gateway:bootRun

# Terminal 5
cd ~/Projects/TrailEquip/ui && npm install && npm run dev
```

**Then access:**
- UI: http://localhost:3000
- API: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/api/v1/health

---

## **Want CI/CD on Your Machine? (20 minutes)**

```bash
# Install GitLab Runner
brew install gitlab-runner

# Register runner
gitlab-runner register
# When prompted:
# - URL: https://gitlab.com/
# - Token: Get from https://gitlab.com/YOUR-USERNAME/trail-equip/-/settings/ci_cd
# - Description: local
# - Tags: local
# - Executor: shell

# Start runner (keep running)
gitlab-runner run

# Now every time you push, GitLab runs tests on YOUR machine!
cd ~/Projects/TrailEquip
git push gitlab main
```

**Watch pipeline:**
https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines

---

## **Which Option?**

| Want... | Do This |
|---------|---------|
| **See it work ASAP** | Step 1 + Step 2 above ⭐ |
| **Run services locally** | Install Java/npm/PostgreSQL + run 5 terminals |
| **Automate testing** | Install GitLab Runner + gitlab-runner register |
| **Both local + automated** | Install GitLab Runner + run services locally |

---

## **Commands Copy-Paste Ready**

```bash
# OPTION A: Just push and watch cloud CI/CD
cd ~/Projects/TrailEquip
git remote add gitlab https://gitlab.com/YOUR-USERNAME/trail-equip.git
git push -u gitlab main
# Then open: https://gitlab.com/YOUR-USERNAME/trail-equip/-/pipelines

# OPTION B: Run locally
java -version
npm --version
brew install postgresql postgis
createdb trailequip
psql trailequip < ~/Projects/TrailEquip/infra/db/init.sql
# Then open 5 terminals and run:
cd ~/Projects/TrailEquip && ./gradlew :services:trail-service:bootRun
# (repeat for each service)
cd ~/Projects/TrailEquip/ui && npm install && npm run dev

# OPTION C: Install local CI/CD runner
brew install gitlab-runner
gitlab-runner register
gitlab-runner run
# Then: git push gitlab main
```

---

## **FAQ**

**Q: Do I need Docker?**
A: No! You have other options.

**Q: Will CI/CD work without Docker?**
A: Yes! Use GitLab Runner with shell executor.

**Q: Can I see the UI?**
A: Yes! Run locally or use GitLab Pages.

**Q: Where are my artifacts?**
A: Pipeline page → Job → Artifacts → Download

**Q: How do I debug?**
A: Click job in pipeline → View full logs

---

## **Everything Ready**

Your TrailEquip is **COMPLETE** and ready to use with GitLab:

✅ Code pushed to your laptop
✅ `.gitlab-ci.yml` configured
✅ Documentation provided
✅ Multiple options to run it

**Pick your option above and go! 🚀**
