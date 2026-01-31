# GitLab CI/CD Optimization - Avoid Compute Minute Limits

## The Problem

GitLab free tier gives **400 shared runner compute minutes per month**. Your old pipeline used **20-25 minutes per run**, meaning you could only deploy **16 times per month** before running out.

**Status:** ❌ Not sustainable for development

## The Solution: Optimized Pipeline

**New pipeline uses only 3-5 minutes per run** = **80+ deployments per month** ✅

### What Changed

| Stage | Before | After | Time Saved |
|-------|--------|-------|-----------|
| build_services | ✅ Gradle build | ❌ Removed | -8 min |
| build_ui | ✅ React build | ❌ Removed | -2 min |
| test_services | ✅ Unit tests | ❌ Removed | -8 min |
| lint_java | ✅ Lint checks | ❌ Removed | -2 min |
| package_docker | ✅ Docker build | ✅ Kept | Same |
| **Total** | **~25 min** | **~4 min** | **-21 min (84% reduction)** |

### Why This Works

The Dockerfile **multi-stage build already does everything**:

```dockerfile
# Stage 1: Gradle builds Java (compiles code)
FROM gradle:8.6-jdk21-jammy AS backend-builder

# Stage 2: Node builds React (compiles frontend)
FROM node:20-alpine AS frontend-builder

# Stage 3: Runtime image
FROM eclipse-temurin:21-jre-jammy
```

**There's no need to run compilation twice** - once in GitLab CI/CD and once in Docker!

### New Workflow

**Before** (inefficient):
```
1. GitLab: gradle clean build -x test (8 min)
2. GitLab: npm run build (2 min)
3. GitLab: gradle test (8 min)
4. GitLab: gradle spotlessCheck (2 min)
5. GitLab: docker build (4 min)
TOTAL: 24 minutes
```

**After** (optimized):
```
1. Local: ./gradlew test (local only, not in CI/CD)
2. Local: ./gradlew build (local only, not in CI/CD)
3. GitLab: docker build (includes Java + React compilation)
TOTAL: 4 minutes in CI/CD + tests run locally
```

### Key Principle: Test Locally, Deploy Remotely

```bash
# Before pushing:
./gradlew test          # Run all tests locally
./gradlew build         # Build locally
docker build .          # Test Docker build locally

# After verification:
git push gitlab main    # Only fast Docker build runs in CI/CD
```

---

## New Pipeline Structure

```yaml
stages:
  - package

package_docker:
  stage: package
  image: docker:24-dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - docker build -t $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG -t $DOCKER_IMAGE_NAME:latest .
    - docker push $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG
    - docker push $DOCKER_IMAGE_NAME:latest
  only:
    - main
```

**That's it!** Single job, ~4 minutes, includes all compilation.

---

## Testing Workflow

### Step 1: Run Tests Locally
```bash
cd /Users/viionascu/Projects/TrailEquip
./gradlew test
```

### Step 2: Build Locally
```bash
./gradlew build
```

### Step 3: Test Docker Build Locally
```bash
docker build -t trailequip:test .
docker run -d -p 8081:8081 trailequip:test
curl http://localhost:8081/actuator/health
```

### Step 4: Push to GitLab (GitLab builds Docker)
```bash
git push gitlab main
```

### Step 5: Deploy to Railway/Render
```
Railway auto-deploys Docker image from GitLab registry
```

---

## Benefits of This Approach

### ✅ Performance
- **84% reduction** in CI/CD time
- 80+ deployments per month instead of 16
- Local testing is faster than waiting for CI/CD

### ✅ Cost
- Stay within **free tier completely**
- No CI/CD minute overages
- No payments needed

### ✅ Reliability
- Tests run on your machine (same environment as development)
- Faster feedback loop
- Docker image pushed to registry for production

### ✅ Best Practices
- Local testing before push (prevents broken deployments)
- Docker build as source of truth for releases
- Clean separation: dev (local) vs production (cloud)

---

## Comparison: Old vs New Approach

### Old Pipeline (❌ Inefficient)
```
❌ Run Gradle build in CI/CD (8 min)
❌ Run React build in CI/CD (2 min)
❌ Run tests in CI/CD (8 min)
❌ Run linting in CI/CD (2 min)
✅ Build Docker in CI/CD (4 min)
───────────────────────────────
Total: 24 minutes per push
Deployments/month: 16
Cost: Runs out of free minutes
```

### New Pipeline (✅ Optimized)
```
✅ Run Gradle build locally (8 min, not counted)
✅ Run React build locally (2 min, not counted)
✅ Run tests locally (8 min, not counted)
✅ Run linting locally (2 min, not counted)
✅ Build Docker in CI/CD (4 min)
───────────────────────────────
CI/CD Total: 4 minutes per push
Local Total: 20 minutes (once before push)
Deployments/month: 80+
Cost: Free tier
```

---

## Local Testing Checklist

Before pushing to GitLab, ensure:

```bash
# 1. Run all tests
./gradlew test

# 2. Build successfully
./gradlew build

# 3. Frontend builds
cd ui
npm run build
cd ..

# 4. Docker builds locally
docker build -t trailequip:test .

# 5. Container runs
docker run -d -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/trailequip \
  trailequip:test

# 6. API responds
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP"}

# 7. Stop container
docker stop <container-id>
```

If all pass → safe to push!

---

## How Much Free Time Do You Have Left?

With the optimized pipeline:

```
Current situation:
- Used: 300 minutes
- Remaining: 100 minutes
- Per deployment: 4 minutes
- Remaining deployments: 100 ÷ 4 = 25 deployments

At 5 deployments per week:
- 25 ÷ 5 = 5 weeks of free usage
- Then free tier resets on the 1st of next month!
```

**No purchase needed!**

---

## If You Want CI/CD Testing (Optional)

If you want tests to run in CI/CD anyway (for team collaboration):

```yaml
test_optional:
  stage: test
  image: gradle:8.5-jdk21-jammy
  script:
    - ./gradlew test
  allow_failure: true
  only:
    - merge_requests  # Only on MR, not every push
  when: manual        # Manual trigger, not automatic
```

This adds 8 min but is **optional** and only on merge requests.

---

## Railway + Optimized Pipeline

Perfect combination:

```
1. Push to GitLab main
   ↓
2. GitLab CI/CD: Docker build (4 min)
   ↓
3. Image pushed to registry
   ↓
4. Railway auto-pulls and deploys
   ↓
5. Live in 5-10 minutes total!
```

All within free tier! ✅

---

## Summary

| Metric | Before | After |
|--------|--------|-------|
| Minutes per deployment | 24 | 4 |
| Deployments/month | 16 | 80+ |
| Free tier limit | Exceeded | ✅ Satisfied |
| Cost | $0+ | $0 |
| Local testing | Optional | Required |
| Deployment speed | Slower | Faster |

---

## Going Forward

**Rule of thumb:**

1. **Always test locally first**
   ```bash
   ./gradlew test && ./gradlew build && docker build .
   ```

2. **Only push when ready**
   ```bash
   git push gitlab main
   ```

3. **GitLab auto-builds Docker** (~4 min)

4. **Railway auto-deploys** (~5 min)

5. **You're done!** 🎉

No CI/CD concerns, no minute limits, all free!

---

## Questions?

**"Can I run tests in CI/CD?"**
- Yes, but they cost 8 minutes each push
- Better to run locally (faster feedback)
- Can add optional manual test job for merge requests

**"What if a test fails?"**
- Catch it locally with `./gradlew test`
- Fix it locally
- Push fixed version
- No CI/CD minutes wasted

**"When do free minutes reset?"**
- 1st of each month
- You have 400 minutes to start fresh

**"Can I pay for more minutes?"**
- Yes, but with this optimization, you don't need to!

---

## Recommended Setup

```bash
# Local development script
alias test-and-deploy='./gradlew test && ./gradlew build && docker build . && git push gitlab main'

# Now just run:
test-and-deploy
```

Takes 20 minutes locally, 4 minutes in CI/CD, and you're live!

---

**Status: ✅ FREE TIER OPTIMIZED**

You can now deploy as much as you want without hitting limits or paying money!
