# TrailEquip Folder Structure & Documentation Organization

## 📁 Project Organization (CONSOLIDATED)

```
TrailEquip/
├── docs/                          ← ALL DOCUMENTATION HERE
│   ├── README.md                 (Start here!)
│   ├── STARTUP.md                (ONE-COMMAND startup)
│   ├── CONFIGURATION.md          (Environment variables)
│   ├── ARCHITECTURE.md           (System design)
│   ├── API_REFERENCE.md          (REST endpoints)
│   ├── TESTING_STRATEGY.md       (Quality gates)
│   ├── GETTING_STARTED.md        (Legacy)
│   └── CRUD_TESTS.md             (Legacy)
│
├── services/
│   ├── trail-service/
│   │   ├── src/main/java/        (Implementation)
│   │   ├── src/test/java/        (Unit & integration tests)
│   │   └── build.gradle.kts
│   ├── weather-service/
│   │   ├── src/main/java/
│   │   ├── src/test/java/
│   │   └── build.gradle.kts
│   └── recommendation-service/
│       ├── src/main/java/
│       ├── src/test/java/
│       └── build.gradle.kts
│
├── infra/
│   ├── docker-compose.yml        (Services orchestration)
│   └── db/
│       ├── init.sql              (Database schema + seed data)
│       └── migrations/
│
├── ui/                            (React frontend)
│   ├── src/
│   ├── tests/
│   └── package.json
│
├── README.md                      (Project overview)
├── docker-compose.yml            (Root level)
├── gradlew                        (Build tool)
├── build.gradle.kts              (Root build config)
└── .gitlab-ci.yml               (CI/CD pipeline)
```

## 🗂️ Documentation Organization (Single Source of Truth)

### One Place for Everything

**Location:** `/docs/` folder

**Why?** No confusion between:
- ~~`/docs/`~~ ❌
- ~~`/documentation/`~~ ❌
- ~~`/Documents/`~~ ❌

### Document Index

| Document | Purpose | Audience |
|----------|---------|----------|
| **README.md** | Start here! Full index | Everyone |
| **STARTUP.md** | Run the app (1 command) | Developers, DevOps |
| **CONFIGURATION.md** | Environment setup | DevOps, Developers |
| **ARCHITECTURE.md** | System design & layers | Architects, Senior Devs |
| **API_REFERENCE.md** | REST endpoints | Frontend, API consumers |
| **TESTING_STRATEGY.md** | Quality requirements | QA, Developers |

## 🚀 Getting Started

### Step 1: Read Documentation

```bash
cd /Users/viionascu/Projects/TrailEquip
open docs/README.md    # Start here
```

### Step 2: Start Application

```bash
docker-compose up     # ONE command (as documented in docs/STARTUP.md)
```

### Step 3: Access Services

```
API Gateway:    http://localhost:8080
Trail Service:  http://localhost:8081
React UI:       http://localhost:3000
Health Check:   http://localhost:8080/api/v1/osm/trails/health
```

## 🔄 Git-Safe Structure

### What Gets Committed

✅ `/docs/` - All documentation
✅ `/services/` - Source code
✅ `/infra/` - Infrastructure configuration
✅ `/ui/` - Frontend code
✅ Root config files (README, docker-compose.yml, etc.)

### What Gets Ignored

❌ `.env` - Environment secrets
❌ `target/`, `build/` - Compiled code
❌ `node_modules/` - Dependencies
❌ `.log` files - Runtime logs

### .gitignore

```
# Environment
.env
.env.local

# Build artifacts
target/
build/
dist/

# Dependencies
node_modules/
vendor/

# IDE
.idea/
.vscode/
*.swp

# Runtime
*.log
.DS_Store
```

## 📊 Folder Responsibilities

### `/services/`

Contains all microservices and their tests.

```
services/trail-service/src/
├── main/java/com/trailequip/trail/
│   ├── adapter/      (REST, DTOs, external services)
│   ├── application/  (Use cases, orchestration)
│   ├── domain/       (Business logic, entities)
│   └── infrastructure/ (Database, OSM API)
└── test/java/       (Unit & integration tests)
```

**Principles:**
- Each service independent
- Separate database per service
- Clear layer separation within each service
- Full test coverage (80%+)

### `/infra/`

Infrastructure configuration.

```
infra/
├── docker-compose.yml    (Local development)
├── db/
│   ├── init.sql          (Database schema + seed)
│   └── migrations/       (Future: versioned migrations)
└── ci-cd/               (Future: pipeline configs)
```

**Principles:**
- Single source of truth for infrastructure
- Reproducible local development
- Version controlled configurations

### `/ui/`

React frontend application.

```
ui/
├── src/
│   ├── components/   (React components)
│   ├── pages/        (Page layouts)
│   ├── services/     (API client)
│   └── styles/       (CSS/Tailwind)
└── tests/            (Frontend tests)
```

**Principles:**
- Component-based architecture
- API client abstraction
- Comprehensive testing

### `/docs/`

All documentation (single source of truth).

```
docs/
├── README.md              (Index)
├── STARTUP.md            (How to run)
├── CONFIGURATION.md      (Environment)
├── ARCHITECTURE.md       (Design)
├── API_REFERENCE.md      (Endpoints)
└── TESTING_STRATEGY.md   (Quality)
```

**Principles:**
- One place for all docs
- Linked references
- Executable examples
- Troubleshooting guides

## 🚫 What NOT to Do

### Don't Create Multiple Doc Folders

❌ **Bad:**
```
/docs/          ← OLD
/documentation/ ← OLD
/Documents/     ← OLD (Just created)
```

✅ **Good:**
```
/docs/          ← ONLY DOCS GO HERE
```

### Don't Scatter Configuration

❌ **Bad:**
```
.env                 (Project root)
config/app.yaml     (Duplicate config)
settings.properties (Another copy)
```

✅ **Good:**
```
.env                           (Local only, not committed)
docker-compose.yml            (Shared configuration)
application.yml              (Application defaults)
docs/CONFIGURATION.md        (Documentation)
```

### Don't Duplicate Documentation

❌ **Bad:**
```
docs/API.md
ui/API_DOCS.md
services/trail-service/API.md
```

✅ **Good:**
```
docs/API_REFERENCE.md  (Single source of truth)
```

## 🔗 Linking Documentation

### How to Reference

**In README or any markdown:**

```markdown
See [Startup Guide](./docs/STARTUP.md)
See [Configuration](./docs/CONFIGURATION.md#database-configuration)
See [API Reference](./docs/API_REFERENCE.md#export-trail-as-geojson)
```

**In code comments:**

```java
/**
 * For startup instructions, see docs/STARTUP.md
 * For configuration options, see docs/CONFIGURATION.md
 */
```

## 📋 Folder Checklist

Before committing, verify:

- [ ] All docs in `/docs/` folder
- [ ] No `/documentation/` or `/Documents/` folders
- [ ] Root README.md points to `/docs/`
- [ ] All links in docs are relative (`./docs/FILE.md`)
- [ ] No duplicate documentation
- [ ] .env file not committed
- [ ] Source code organized in `/services/`
- [ ] Tests in `/services/{service}/src/test/`

## 🎯 Navigation Guide

**If you need to...**

| Task | Go To |
|------|-------|
| Start the application | `docs/STARTUP.md` |
| Configure environment | `docs/CONFIGURATION.md` |
| Understand architecture | `docs/ARCHITECTURE.md` |
| Use the API | `docs/API_REFERENCE.md` |
| Write tests | `docs/TESTING_STRATEGY.md` |
| Find code | `services/` folder |
| Deploy | `infra/` folder |

## 📞 Questions?

**Where is documentation?** → `/docs/`
**Where is code?** → `/services/`
**Where is config?** → `/infra/` and `docs/CONFIGURATION.md`
**How do I start?** → Run `docker-compose up` (see `docs/STARTUP.md`)

---

**This is the ONLY folder structure guide you need.**
**Everything is consolidated. One place for everything. Clean. Simple. Effective.**

Last updated: January 30, 2026
Status: ✓ Consolidated & Clean
