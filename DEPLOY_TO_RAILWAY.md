# Deploy to Railway in 5 Minutes

The absolute fastest way to get your TrailEquip app online and shareable with anyone.

## Why Railway?
- ✅ **Free tier** with $5/month credit (enough for development)
- ✅ **PostgreSQL included** (no extra setup)
- ✅ **Auto-deploy** from GitLab push
- ✅ **Always running** (no hibernation delays)
- ✅ **Public URL** anyone can access
- ✅ **Logs built-in** for debugging

## Step 1: Sign Up (1 minute)

Go to **https://railway.app**

Click "Start Project" → Sign in with GitHub/Email

No credit card required! ✅

## Step 2: Create New Project (1 minute)

1. Click "Create New Project"
2. Select "Deploy from GitHub" OR "Deploy from GitLab"
3. Authorize Railway to access your repositories
4. Select **TrailEquip** repository
5. Click "Deploy"

Railway automatically detects `Dockerfile` and starts building.

## Step 3: Add PostgreSQL Database (2 minutes)

1. In your Railway dashboard, click "Add Service"
2. Select "Database" → "PostgreSQL"
3. Leave defaults:
   - Version: Latest
   - Name: `postgres` (auto-set)

Wait for database to initialize (~1 min)

## Step 4: Done! Get Your Public URL (1 minute)

Railway automatically deploys and builds your app (~3-5 min build time)

Once deployed:
1. Click the **app service** in dashboard
2. Go to "Settings" tab
3. Find **"Domains"** section
4. Your public URL is shown: `https://trailequip-production.up.railway.app`

**Copy this URL and share it!** Anyone can now access your app.

---

## Access Your App

### Frontend (React UI)
```
https://trailequip-production.up.railway.app
```

### API Documentation
```
https://trailequip-production.up.railway.app/api/v1/trails
```

### Swagger API Docs
```
https://trailequip-production.up.railway.app/swagger-ui.html
```

---

## Troubleshooting

### Build Failed?
Click the service → "View Logs"

Common issues:
- **"out of memory"**: Your service needs more RAM
  - Solution: Settings → Resources → Increase RAM to 512MB+

- **"database connection failed"**: Database not linked
  - Solution: See Step 3 above - add PostgreSQL service

- **"port not found"**: Application not listening correctly
  - Solution: We already configured this in Dockerfile ✅

### Deployment Takes Too Long?
First deploy takes 5-10 minutes (building frontend + backend)
Subsequent deploys with code changes: 2-3 minutes

### Application Keeps Restarting?
Check logs for errors:
1. Click service → "View Logs"
2. Look for red error messages
3. Common causes:
   - Database not initialized yet (wait another minute)
   - Environment variables missing (see below)

---

## Environment Variables

Railway should auto-configure database connection.

If not, manually add in service settings:
1. Service → Settings → "Variable Reference"
2. Add these:
```
SPRING_DATASOURCE_URL = jdbc:postgresql://postgres:5432/railway
SPRING_DATASOURCE_USERNAME = postgres
SPRING_DATASOURCE_PASSWORD = (auto-filled by Railway)
SPRING_JPA_HIBERNATE_DDL_AUTO = update
```

---

## View Logs & Monitor

### Real-time Logs
1. Click service
2. "View Logs" tab
3. See what's happening now

### Metrics
1. "Metrics" tab
2. View CPU, memory, requests over time

### Database Status
1. Click postgres service
2. See database size, connections
3. Logs for any errors

---

## Deploy Updates

Every time you push to GitLab main:
```bash
git push gitlab main
```

Railway automatically:
1. ✅ Pulls latest code
2. ✅ Rebuilds Docker image
3. ✅ Deploys new version
4. ✅ Keeps database intact

No downtime! (usually ~2-3 min update time)

---

## Share Your App

Your URL: `https://trailequip-production.up.railway.app`

Share with:
- **Family/Friends**: Just send the URL, they can open in browser
- **Teammates**: Add to project documentation
- **Portfolio**: Show on GitHub/LinkedIn
- **Social media**: Tweet your project link!

---

## Scale When Needed

If you get popular and hit limits:

1. Upgrade service tier:
   - Settings → Resources → Increase CPU/RAM

2. Add more replicas (if needed):
   - Services → Add more instances

3. Upgrade database:
   - Postgres service → Settings

Cost scales as you grow (pay only what you use)

---

## What's Included in Free Tier?

| Resource | Limit |
|----------|-------|
| App CPU | Shared CPU (limited) |
| App Memory | 256-512 MB recommended |
| Database | 10 GB PostgreSQL |
| Bandwidth | Generous (pay-as-you-go after free credit) |
| Build Time | Unlimited |
| Deployments | Unlimited |
| **Monthly Credit** | **$5** (usually enough for development) |

---

## Keep Your App Accessible

To ensure your app stays on the free tier:

1. ✅ Keep monthly usage under $5
2. ✅ Don't store huge amounts of data
3. ✅ Monitor bandwidth usage
4. ✅ Check logs for errors/crashes
5. ✅ Set email alerts for overages

---

## If You Hit Free Tier Limits

Options:
1. **Upgrade to paid**: $5-20/month for production
2. **Switch to Render**: Free tier with hibernation
3. **Use Fly.io**: More generous free tier
4. **Create new Railway project**: Reset free credits (not recommended)

---

## Support & Docs

**Railway Documentation**: https://docs.railway.app/
**Getting Help**: Railway Discord community (linked in docs)

---

## Next: Tell Me When Done!

Once deployed:
1. ✅ Save your public URL
2. ✅ Test it in browser
3. ✅ Share the URL
4. ✅ Let me know if any issues!

Your app is now live on the internet! 🎉

---

## Summary

| Step | Time | What You Do |
|------|------|-----------|
| 1. Sign up | 1 min | Create Railway account |
| 2. Connect repo | 1 min | Select TrailEquip on GitLab |
| 3. Add Postgres | 2 min | Click "Add Service" → Database |
| 4. Wait & Deploy | 5 min | Railway builds & deploys |
| 5. Get URL | 1 min | Copy public URL from dashboard |
| **TOTAL** | **~10 min** | **Live on the internet!** |

🎯 **You're done! Your app is now live and shareable!**

Go to: https://railway.app now! 🚀
