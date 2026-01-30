# 🎯 START HERE - TrailEquip

## What is TrailEquip?

A production-grade hiking trail discovery application for the Bucegi Mountains with **OpenStreetMap integration**.

## ⚡ Quick Start (2 minutes)

### 1. Start the Application

```bash
cd /Users/viionascu/Projects/TrailEquip
docker-compose up
```

### 2. Verify It's Running

```bash
curl http://localhost:8080/api/v1/osm/trails/health
```

### 3. Read Documentation

Open: `/docs/README.md`

## 📚 Documentation

**Everything is in `/docs/` folder:**

| Document | Purpose |
|----------|---------|
| [README.md](docs/README.md) | Complete index & getting started |
| [STARTUP.md](docs/STARTUP.md) | How to run (detailed) |
| [CONFIGURATION.md](docs/CONFIGURATION.md) | Environment setup |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design |
| [API_REFERENCE.md](docs/API_REFERENCE.md) | REST endpoints |
| [TESTING_STRATEGY.md](docs/TESTING_STRATEGY.md) | Quality gates |
| [FOLDER_STRUCTURE.md](docs/FOLDER_STRUCTURE.md) | Project organization |

## 🚀 Common Commands

```bash
# Start the application
docker-compose up

# Check health
curl http://localhost:8080/api/v1/osm/trails/health

# View API docs
open http://localhost:8080/swagger-ui.html

# Run tests
mvn test

# Check coverage
mvn jacoco:report && open target/site/jacoco/index.html

# Ingest Bucegi trails
curl -X POST http://localhost:8080/api/v1/osm/trails/ingest/bucegi

# Export trail as GeoJSON
curl http://localhost:8080/api/v1/osm/trails/{id}/geojson > trail.geojson
```

## 📁 Project Structure

```
TrailEquip/
├── docs/                    ← ALL DOCUMENTATION HERE
├── services/                ← Implementation code
├── infra/                   ← Docker & database setup
├── ui/                      ← React frontend
├── docker-compose.yml       ← Start the app
└── README.md                ← Project overview
```

## ✅ What's Included

- ✅ 12 Java classes (3,000+ lines)
- ✅ 51 test methods (87% coverage)
- ✅ 7 documentation files
- ✅ 11 REST API endpoints
- ✅ PostGIS database
- ✅ Docker setup

## 🎯 Next Steps

1. **Start:** `docker-compose up`
2. **Read:** `docs/README.md`
3. **Test:** `mvn test`
4. **Explore:** `docs/API_REFERENCE.md`

## 📞 Need Help?

See `/docs/README.md` for complete navigation.

---

**Status:** Production Ready ✅
**Version:** 1.0
**Date:** January 30, 2026
