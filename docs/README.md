# TrailEquip Documentation

**📍 SINGLE SOURCE OF TRUTH**

This `/docs/` folder contains everything needed to understand, develop, deploy, and maintain TrailEquip.

> **🎯 Important:** All documentation has been consolidated into this single folder.
> - ✅ `/docs/` ← Start here
> - ❌ `/documentation/` ← Deprecated
> - ❌ `/Documents/` ← Deprecated
>
> To avoid confusion, use ONLY the `/docs/` folder for all documentation.

## 🎨 Architecture Diagrams (Start Here for Visual Learners!)

**Want to understand the system quickly?** These are the easiest way to get started:

0. **[ARCHITECTURE_SIMPLE.md](ARCHITECTURE_SIMPLE.md)** - 👶 Simple explanation for beginners (even 15-year-olds!)
   - How each service works with real-world analogies
   - Step-by-step examples
   - "Restaurant" analogy to understand microservices

1. **[ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md)** - 📊 Visual diagrams and flowcharts
   - Complete system diagram
   - Data flow diagrams
   - Request journey maps
   - Port reference table
   - Service responsibilities

---

## 📚 Complete Documentation Index

### Getting Started

Start here if you're new to the project:

1. **[STARTUP.md](STARTUP.md)** - ONE-COMMAND startup guide
   - How to start the application
   - Verify all services are running
   - Restart procedures
   - Troubleshooting

2. **[CONFIGURATION.md](CONFIGURATION.md)** - Environment setup
   - Required environment variables
   - Configuration precedence
   - Creating .env file
   - Profile selection (dev/prod)

3. **[GETTING_STARTED.md](GETTING_STARTED.md)** - Quick start for new developers
   - Project setup
   - Development environment
   - First build and run
   - IDE configuration

### Development

For implementing features and understanding the codebase:

4. **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design
   - Clean Architecture layers
   - Component descriptions
   - Data flow diagrams
   - Technology stack
   - Service interactions

5. **[API_REFERENCE.md](API_REFERENCE.md)** - REST API documentation
   - All endpoints (ingestion, export, search)
   - Request/response formats
   - Usage examples
   - Data models

6. **[CRUD_TESTS.md](CRUD_TESTS.md)** - REST API test documentation
   - Testing all endpoints
   - Request/response examples
   - Common test scenarios

7. **[TESTING_STRATEGY.md](TESTING_STRATEGY.md)** - Quality assurance
   - Test coverage requirements (80% minimum)
   - Test organization
   - Running tests
   - CI/CD integration

### Deployment & Infrastructure

For deployment and infrastructure setup:

8. **[GITLAB_SETUP.md](GITLAB_SETUP.md)** - GitLab CI/CD configuration
   - 6 deployment options
   - GitLab Runner setup
   - Cloud CI/CD pipeline
   - GitLab Pages deployment
   - Troubleshooting

9. **[POSTGRES_LOCAL_SETUP.md](POSTGRES_LOCAL_SETUP.md)** - Local PostgreSQL setup
   - Installation via Homebrew
   - Database initialization
   - Spring Boot configuration
   - Useful commands and troubleshooting

10. **[DEPLOYMENT_SUMMARY.md](DEPLOYMENT_SUMMARY.md)** - Deployment status
    - Build and infrastructure updates
    - CI/CD pipeline status
    - Next steps after deployment
    - Quality checklist

11. **[FOLDER_STRUCTURE.md](FOLDER_STRUCTURE.md)** - Project directory layout
    - Source code organization
    - Build and infrastructure structure
    - Documentation location
    - Navigation guide

### Frontend & UI

12. **[MAP_STYLING_GUIDE.md](MAP_STYLING_GUIDE.md)** - UI map styling
    - OpenTopoMap integration
    - Trail rendering and styling
    - CSS enhancements
    - Performance optimization
    - Troubleshooting

### Source Code

The actual implementation:

```
services/trail-service/src/
├── main/java/com/trailequip/trail/
│   ├── adapter/
│   │   ├── rest/          (Controllers, APIs)
│   │   └── dto/           (Data Transfer Objects)
│   ├── application/
│   │   └── service/       (Use cases, orchestration)
│   ├── domain/
│   │   ├── model/         (Entities, business logic)
│   │   └── repository/    (Data access)
│   └── infrastructure/
│       ├── config/        (Spring configuration)
│       └── overpass/      (OSM API client)
└── test/java/             (Comprehensive test suite)
```

---

## 🚀 Quick Start

### First Time Setup

1. **Read** [STARTUP.md](STARTUP.md)
2. **Run** `docker-compose up`
3. **Verify** `curl http://localhost:8080/api/v1/osm/trails/health`

### Common Tasks

#### Ingest Bucegi Trails
```bash
curl -X POST http://localhost:8080/api/v1/osm/trails/ingest/bucegi
```
See [API_REFERENCE.md](API_REFERENCE.md#ingest-bucegi-trails)

#### Export Trail as GeoJSON
```bash
curl http://localhost:8080/api/v1/osm/trails/{id}/geojson > trail.geojson
```
See [API_REFERENCE.md](API_REFERENCE.md#export-trail-as-geojson)

#### Run Tests
```bash
mvn test
mvn jacoco:report
```
See [TESTING_STRATEGY.md](TESTING_STRATEGY.md)

#### Check Configuration
```bash
cat .env
echo $DATABASE_URL
```
See [CONFIGURATION.md](CONFIGURATION.md)

---

## 📋 Quality Standards

### Code Quality

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.0
- **Architecture**: Clean Architecture, SOLID principles
- **Testing**: 80% minimum code coverage (mandatory)

### Documentation

- All code changes documented
- Every API endpoint documented with examples
- Configuration explained with defaults
- Troubleshooting guides included

### Database

- PostgreSQL 15+ with PostGIS 3.3
- Automatic schema creation and validation
- Migrations managed via Liquibase/Flyway
- Data validated on insert

---

## 🔍 Key Features

### OSM Integration
- Fetch hiking routes from OpenStreetMap via Overpass API
- Parse OSMC trail marking symbols
- Infer difficulty from terrain metrics
- Automatic deduplication by OSM ID

### Data Export
- **GeoJSON** for web maps (Leaflet, MapBox)
- **GPX 1.1** for GPS devices (Garmin, Strava)
- Collections and single trails

### Trail Information
- Distance, elevation gain/loss
- Duration estimation
- Terrain classification (forest, alpine, rock, etc.)
- Hazard identification (exposure, bears, weather)
- Waypoint extraction (peaks, shelters, junctions)

---

## 🛠️ Technology Stack

### Backend
```
Java 21
Spring Boot 3.2.0
Hibernate with Spatial
JTS (Java Topology Suite)
```

### Database
```
PostgreSQL 15+
PostGIS 3.3+
GIS/Spatial indexes
```

### Infrastructure
```
Docker & Docker Compose
Spring Cloud Gateway
OpenAPI/Swagger
```

### Testing
```
JUnit 5
Mockito
JaCoCo (coverage)
```

---

## 📊 Architecture Layers

```
REST API Layer
    ↓
Application Services (Use cases)
    ↓
Domain Layer (Business logic)
    ↓
Infrastructure (DB, APIs)
```

**Key Principle**: Business logic concentrated in domain models, fully testable without external dependencies.

---

## 🔄 Development Workflow

### 1. Make Changes
```bash
# Edit code, tests, or configuration
vim src/main/java/.../MyService.java
```

### 2. Run Tests
```bash
mvn test
mvn jacoco:report
```

### 3. Verify Coverage
```bash
# Must be >= 80%
open target/site/jacoco/index.html
```

### 4. Restart Application
```bash
docker-compose restart trail-service
```

### 5. Test APIs
```bash
curl http://localhost:8080/api/v1/osm/trails/health
```

---

## 🐛 Troubleshooting

### Application won't start

1. Check logs: `docker-compose logs trail-service`
2. Verify config: [CONFIGURATION.md](CONFIGURATION.md)
3. See [STARTUP.md](STARTUP.md#troubleshooting-startup)

### API returning errors

1. Check endpoint: [API_REFERENCE.md](API_REFERENCE.md)
2. Verify parameters
3. Check health: `curl http://localhost:8080/api/v1/osm/trails/health`

### Database issues

1. Check connection: `docker-compose logs db`
2. Verify PostGIS: `docker-compose exec db psql -U trailequip -d trailequip -c "SELECT PostGIS_version();"`
3. See [CONFIGURATION.md](CONFIGURATION.md#database-connection-strings)

### Test coverage too low

1. Write more tests (80% minimum required)
2. Check coverage report: `target/site/jacoco/index.html`
3. See [TESTING_STRATEGY.md](TESTING_STRATEGY.md)

---

## 📖 Design Principles

### SOLID

- **S**ingle Responsibility: Each class has one job
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes substitutable for base types
- **I**nterface Segregation: Many specific interfaces, not generic ones
- **D**ependency Inversion: Depend on abstractions, not concretions

### Clean Architecture

- Clear layer separation
- Business logic independent of frameworks
- Easy to test, maintain, extend
- Fail-fast validation

### Error Handling

- Never swallow exceptions
- Wrap low-level errors with domain context
- Fail fast with clear messages
- Log with production debugging in mind

---

## 📝 Change Log

### v1.0 - Initial Release (Jan 30, 2026)

**Features:**
- ✅ OSM integration via Overpass API
- ✅ OSMC trail marking support
- ✅ GeoJSON and GPX export
- ✅ Trail difficulty inference
- ✅ Terrain classification
- ✅ Hazard identification
- ✅ Waypoint extraction
- ✅ Comprehensive REST API
- ✅ Full test coverage (80%+)

**Architecture:**
- ✅ Clean Architecture
- ✅ SOLID principles
- ✅ Microservice-ready
- ✅ Docker deployment

**Documentation:**
- ✅ Complete API reference
- ✅ Startup guide
- ✅ Configuration manual
- ✅ Testing strategy
- ✅ Architecture documentation

---

## 🔒 Security

- No hardcoded credentials
- Environment-based configuration
- Input validation at boundaries
- SQL injection prevention via JPA/ORM
- PostGIS spatial query injection prevention
- Secure defaults everywhere

---

## 🤝 Contributing

### Code Style
- Follow existing conventions
- Use meaningful names
- Write self-documenting code
- Keep methods focused

### Testing
- Write tests for new features
- Maintain 80%+ coverage
- Use descriptive test names
- Test edge cases

### Documentation
- Update docs with code changes
- Add API examples
- Document configuration
- Include troubleshooting

---

## 📞 Support

### Getting Help

1. Check relevant documentation (links above)
2. Search for similar issues
3. Review code comments
4. Check application logs

### Reporting Issues

Include:
- What you were trying to do
- Expected behavior
- Actual behavior
- Error message or logs
- Environment (OS, Java version, etc.)

---

## 📜 License

- **Data**: ODbL (OpenStreetMap data license)
- **Code**: Check LICENSE file
- **Attribution**: Always credit OpenStreetMap

---

## 🎯 Next Steps

**Just starting?**
→ Go to [STARTUP.md](STARTUP.md)

**Setting up environment?**
→ Go to [CONFIGURATION.md](CONFIGURATION.md)

**Understanding the system?**
→ Go to [ARCHITECTURE.md](ARCHITECTURE.md)

**Using the API?**
→ Go to [API_REFERENCE.md](API_REFERENCE.md)

**Writing tests?**
→ Go to [TESTING_STRATEGY.md](TESTING_STRATEGY.md)

---

---

## 📦 Documentation Consolidation Completed

As of January 30, 2026:
- ✅ All documentation consolidated into `/docs/` (single source of truth)
- ✅ Deprecated `/documentation/` folder removed
- ✅ Deprecated `/Documents/` folder removed
- ✅ 13 comprehensive markdown files organized by topic
- ✅ All links updated and verified
- ✅ No orphaned references remaining

**Total Documentation:**
- 13 markdown files
- ~14,000+ lines of current, production-ready documentation
- Covers: setup, architecture, API, testing, deployment, CI/CD, and UI

---

**Last Updated**: January 30, 2026
**Status**: Production Ready ✓
**Coverage**: 80%+ ✓
**Deployment**: Docker Ready ✓
**Documentation**: Consolidated & Organized ✓
