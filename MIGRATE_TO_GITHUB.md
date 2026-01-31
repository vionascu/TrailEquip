# Migrate TrailEquip to GitHub @vionascu

**Goal**: Move all code from GitLab to your GitHub account with full history preserved.

---

## Step 1: Create Repository on GitHub

1. Go to https://github.com/new
2. **Repository name**: `trail-equip` (or `TrailEquip`)
3. **Description**: "Hiking trail discovery app with interactive maps and weather forecasts"
4. **Visibility**: Public (recommended for open source)
5. **Initialize**: Leave empty (we're mirroring from GitLab)
6. Click "Create repository"

**Your repo URL**: `https://github.com/vionascu/trail-equip`

---

## Step 2: Mirror All Code from GitLab

### Option A: Mirror with Full History (Recommended)

This preserves all commits, branches, and tags.

```bash
cd /Users/viionascu/Projects/TrailEquip

# Add GitHub as a remote
git remote add github https://github.com/vionascu/trail-equip.git

# Push all branches to GitHub
git push github --all

# Push all tags to GitHub
git push github --tags

# Verify
git remote -v
# Should show:
# gitlab  https://gitlab.com/vic.ionascu/trail-equip.git (fetch)
# gitlab  https://gitlab.com/vic.ionascu/trail-equip.git (push)
# github  https://github.com/vionascu/trail-equip.git (fetch)
# github  https://github.com/vionascu/trail-equip.git (push)
```

**Result**: All commits, branches, and history on GitHub! ✅

### Option B: Mirror Only Main Branch

If you only want the current main branch:

```bash
git push github main
```

---

## Step 3: Verify on GitHub

1. Visit: https://github.com/vionascu/trail-equip
2. Check:
   - ✅ All files present
   - ✅ Commit history showing
   - ✅ Branches visible (if you pushed --all)
   - ✅ README displays correctly
   - ✅ `docs/` folder visible

---

## Step 4: Setup GitHub Actions

GitHub Actions is **completely free** with **unlimited CI/CD minutes**.

### Create Workflow File

Create `.github/workflows/deploy.yml`:

```bash
mkdir -p .github/workflows
```

Create the file `.github/workflows/deploy.yml`:

```yaml
name: Build and Deploy Docker

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2

      - name: Login to GitHub Container Registry
        uses: docker/login-action@v2
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and Push Docker image
        uses: docker/build-push-action@v4
        with:
          context: .
          push: true
          tags: |
            ghcr.io/${{ github.repository }}:latest
            ghcr.io/${{ github.repository }}:${{ github.sha }}
          cache-from: type=registry,ref=ghcr.io/${{ github.repository }}:buildcache
          cache-to: type=registry,ref=ghcr.io/${{ github.repository }}:buildcache,mode=max

      - name: Log deployment info
        run: |
          echo "Docker image built and pushed!"
          echo "Image: ghcr.io/${{ github.repository }}:latest"
          echo "GitHub Actions compute minutes: UNLIMITED ✅"
```

### Commit Workflow

```bash
git add .github/workflows/deploy.yml
git commit -m "Add GitHub Actions CI/CD workflow (unlimited free minutes)"
git push github main
```

---

## Step 5: Configure Railway to Deploy from GitHub

Railway auto-deploys from GitHub (and it's simpler than GitLab!).

### In Railway Dashboard

1. Create new project
2. Select "Deploy from GitHub"
3. Authorize Railway to access your GitHub account
4. Select `vionascu/trail-equip` repository
5. Select branch: `main`
6. Click "Deploy"
7. Railway auto-configures everything!

**Result**: Every push to `github main` → Railway auto-deploys ✅

---

## Step 6: Update Your Local Repository

Make GitHub your primary remote:

```bash
# Update remote URLs
git remote set-url origin https://github.com/vionascu/trail-equip.git

# Or use GitHub as primary, GitLab as backup
git remote remove gitlab
git remote add gitlab https://gitlab.com/vic.ionascu/trail-equip.git
git remote set-url origin https://github.com/vionascu/trail-equip.git

# Verify
git remote -v
# Should show:
# origin  https://github.com/vionascu/trail-equip.git (fetch)
# origin  https://github.com/vionascu/trail-equip.git (push)
# gitlab  https://gitlab.com/vic.ionascu/trail-equip.git (fetch)
# gitlab  https://gitlab.com/vic.ionascu/trail-equip.git (push)
```

---

## Step 7: Verify Everything Works

### Test GitHub Actions

```bash
# Make a test change
echo "# Updated on GitHub" >> README.md

# Commit and push
git add README.md
git commit -m "Test GitHub Actions deployment"
git push origin main

# Watch GitHub Actions build (unlimited minutes!)
# Go to: https://github.com/vionascu/trail-equip/actions
```

### Test Railway Deployment

1. Go to Railway dashboard
2. Watch deployment logs
3. Get public URL
4. Test in browser

---

## Step 8: Migrate Other Projects (Optional)

Use the same approach for other GitLab projects:

```bash
# For each project
cd /path/to/project
git remote add github https://github.com/vionascu/project-name.git
git push github --all
git push github --tags
```

---

## Configuration for GitHub

### Enable Branch Protection

Protect `main` branch:

1. Settings → Branches
2. Add rule for `main`
3. Require pull request before merging (optional)
4. Require status checks to pass (GitHub Actions)

### Setup GitHub Secrets (Optional)

If you need credentials:

1. Settings → Secrets and variables → Actions
2. Add any secrets (Docker credentials, API keys, etc.)
3. Reference in workflow: `${{ secrets.SECRET_NAME }}`

---

## Comparison: GitLab vs GitHub

| Feature | GitLab | GitHub |
|---------|--------|--------|
| **CI/CD Minutes** | 400/month | Unlimited |
| **Free Tier** | Generous | Excellent |
| **Cost** | Free | Free |
| **Learning Curve** | Moderate | Easy |
| **Community** | Growing | Massive |
| **Integrations** | Good | Best-in-class |
| **Actions/Workflows** | Good | Industry standard |

**GitHub wins for you**: Unlimited CI/CD, no minute counting ever again!

---

## Workflow After Migration

### Daily Development

```bash
# Make changes
vim src/main/java/...

# Test locally
./gradlew test
./gradlew build

# Commit and push
git add .
git commit -m "Your changes"
git push origin main

# GitHub Actions runs (unlimited minutes!)
# Railway auto-deploys
# Done!
```

### No More CI/CD Worries

- ✅ Push as many times as you want
- ✅ No minute counting
- ✅ No payments needed
- ✅ Automatic deployments
- ✅ Build logs always available

---

## Cost Summary

### Before (GitLab)
```
GitLab CI/CD:  Free (but limited to 400 min/month)
Railway:       $0/month
───────────────────────────────
Total:         $0/month (with optimization)
Problem:       Limited to 100 deployments/month
```

### After (GitHub)
```
GitHub CI/CD:  Free (unlimited minutes)
Railway:       $0/month
───────────────────────────────
Total:         $0/month
Benefit:       Deploy as many times as you want!
```

---

## Troubleshooting

### Q: Did I lose any commits?
**A**: No! Mirror push preserves full history.

### Q: Can I keep GitLab as backup?
**A**: Yes! Keep both remotes configured.

### Q: What if GitHub Actions fails?
**A**: Check logs: https://github.com/vionascu/trail-equip/actions

### Q: Can I run tests in GitHub Actions?
**A**: Yes! Add another step to workflow (but not necessary with Docker).

### Q: What about PR checks?
**A**: GitHub Actions can run on PRs too (optional).

### Q: Do I need a personal access token?
**A**: No! `${{ secrets.GITHUB_TOKEN }}` is automatic.

---

## Next: Update Documentation

Once on GitHub, update these files:

```markdown
# README.md
- Update repository link from GitLab to GitHub
- Add GitHub Actions badge
- Update deployment instructions

# docs/
- Update any GitLab-specific instructions
- Add GitHub Actions info
```

Example:

```markdown
## CI/CD Status

![Build Status](https://github.com/vionascu/trail-equip/actions/workflows/deploy.yml/badge.svg)

## Build & Deploy

- **CI/CD**: GitHub Actions (unlimited free)
- **Deployment**: Railway (free tier)
- **Status**: ✅ Fully automated
```

---

## Summary: GitHub Migration

| Step | Time | Status |
|------|------|--------|
| 1. Create GitHub repo | 2 min | ⏳ Next |
| 2. Mirror from GitLab | 1 min | ⏳ Next |
| 3. Verify on GitHub | 1 min | ⏳ Next |
| 4. Setup GitHub Actions | 5 min | ⏳ Next |
| 5. Test deployment | 5 min | ⏳ Next |
| 6. Update local config | 2 min | ⏳ Next |
| **Total** | **~15 min** | ⏳ |

---

## Quick Commands (Copy-Paste)

```bash
# Navigate to project
cd /Users/viionascu/Projects/TrailEquip

# Add GitHub remote
git remote add github https://github.com/vionascu/trail-equip.git

# Push everything to GitHub
git push github --all
git push github --tags

# Verify
git remote -v

# Create GitHub Actions workflow
mkdir -p .github/workflows
# Copy the workflow YAML content to .github/workflows/deploy.yml

# Commit and push workflow
git add .github/workflows/deploy.yml
git commit -m "Add GitHub Actions CI/CD workflow"
git push github main

# Done! 🎉
```

---

## After Migration Complete

✅ Code on GitHub at `https://github.com/vionascu/trail-equip`
✅ Unlimited CI/CD (GitHub Actions)
✅ Auto-deploying to Railway
✅ Full commit history preserved
✅ No more minute limits ever
✅ Can deploy 100+ times per month
✅ Completely free

**You're now ready for unlimited development and deployment!** 🚀

---

## Keep GitLab?

You can keep GitLab as backup:

```bash
# Push to both simultaneously
git push github main
git push gitlab main

# Or setup automatic mirroring in GitLab settings
```

But GitHub is your new primary. GitHub Actions = unlimited, no stress!

---

**Next Action**: Start Step 1 above and you'll be on GitHub in 15 minutes! 🎉
