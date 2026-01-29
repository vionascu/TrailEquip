# TrailEquip Architecture

A complete guide to understanding how TrailEquip works—for everyone from beginners to architects.

---

## 🎯 What is TrailEquip?

**In Simple Terms:** TrailEquip is a hiking app that helps you find trails in the Bucegi Mountains, checks the weather, and tells you what gear to bring.

**In Technical Terms:** A microservices-based web platform that combines trail discovery, real-time weather forecasting, and intelligent equipment recommendations through independent, scalable services.

---

## 🏗️ System Architecture

### High-Level View (For Everyone)

```
┌─────────────────────────────────────────────────────────────┐
│                     🖥️  YOUR BROWSER                         │
│                    (React Web App)                          │
│                    ↓ Display Trails                         │
│                    ↑ Get Information                         │
└────────────────────────────┬────────────────────────────────┘
                             │
                        HTTP/REST
                             │
           ┌─────────────────┴─────────────────┐
           │                                   │
      ┌────▼──────────────────┐    ┌──────────▼──────────┐
      │ 🚪 API GATEWAY        │    │                     │
      │ (Front Door)          │    │                     │
      │ Port 8080             │    │                     │
      │                       │    │                     │
      │ • Routes requests     │    │                     │
      │ • Handles users       │    │                     │
      │ • Manages sessions    │    │                     │
      └────┬────────┬─────────┘    │                     │
           │        │              │                     │
  ┌────────┘  ┌─────┘              │                     │
  │           │                    │                     │
  │    ┌──────▼─────────┐   ┌──────▼─────────┐   ┌──────▼──────┐
  │    │ 🥾 TRAIL       │   │ ⛈️  WEATHER    │   │ 🎒 EQUIP    │
  │    │ SERVICE        │   │ SERVICE        │   │ SERVICE     │
  │    │ (8081)         │   │ (8082)         │   │ (8083)      │
  │    │                │   │                │   │             │
  │    │ • Find trails  │   │ • Get forecast │   │ • Suggest   │
  │    │ • Details      │   │ • Cache temps  │   │   gear      │
  │    │ • Map          │   │ • Wind info    │   │ • Ratings   │
  │    └────┬───────────┘   └────┬───────────┘   └─────┬───────┘
  │         │                    │                     │
  │         └────────────────────┴─────────────────────┘
  │                              │
  │                    ┌─────────▼──────────┐
  │                    │ 💾 DATABASE        │
  │                    │ PostgreSQL + Maps  │
  │                    │ (Port 5432)        │
  │                    │                    │
  │                    │ • Stores trails    │
  │                    │ • Geographic data  │
  │                    │ • Equipment info   │
  │                    └────────────────────┘
  │
  └──→ All services run in containers (Docker)
```

### What Each Part Does

#### 👨‍💻 **React UI** (Port 3000)
- What you see in your browser
- Shows trails on a map
- Displays weather and equipment recommendations
- Lets you search and filter trails

#### 🚪 **API Gateway** (Port 8080)
- Acts like a receptionist for your requests
- Directs your request to the right service
- Manages who you are (authentication)
- Prevents abuse (rate limiting)

#### 🥾 **Trail Service** (Port 8081)
- Knows everything about trails
- Stores trail names, difficulty, location
- Answers: "Show me all trails near here"
- Uses geographic data for location queries

#### ⛈️ **Weather Service** (Port 8082)
- Fetches weather forecasts
- Caches data so it doesn't call external APIs too often
- Answers: "What's the weather for this location?"
- Provides temperature, wind, rain predictions

#### 🎒 **Equipment Service** (Port 8083)
- Recommends what gear you need
- Considers trail difficulty + weather
- Answers: "For this trail + weather, bring..."
- Suggests boots, jackets, backpacks, etc.

#### 💾 **PostgreSQL Database**
- Stores all data permanently
- Includes PostGIS for geographic queries
- Single source of truth for all services
- Used by all three services

---

## 🔄 How a Request Flows (Example)

**Scenario:** You search for "Easy trails near Omu Peak with good weather"

```
1. 🖱️ YOU CLICK SEARCH
   └─→ Browser sends request to API Gateway

2. 🚪 API GATEWAY RECEIVES IT
   └─→ "This is a trail search request"
       "Route it to Trail Service"

3. 🥾 TRAIL SERVICE SEARCHES
   └─→ Queries database: "Find easy trails near Omu Peak"
       Returns: ["Omu Peak Loop", "Sphinx Ridge", ...]

4. ⛈️ GATEWAY ALSO ASKS WEATHER SERVICE
   └─→ "What's the weather at Omu Peak?"
       Returns: Temperature, wind, rain chance

5. 🎒 GATEWAY ASKS EQUIPMENT SERVICE
   └─→ "For these trails + this weather, what should I bring?"
       Returns: ["Hiking boots", "Rain jacket", "Backpack"]

6. 📦 GATEWAY COMBINES EVERYTHING
   └─→ Packages all info together

7. 🖥️ BROWSER SHOWS YOU
   └─→ "Omu Peak Loop - Easy - 15°C - Bring: boots, jacket, pack"
```

---

## 🏛️ Architecture Style

### Microservices Architecture

**What does "microservices" mean?**

Instead of one giant program doing everything, we have small independent programs:

```
❌ OLD WAY (Monolith):
┌──────────────────────────┐
│ Everything in one app    │
│ • Trails                 │
│ • Weather                │
│ • Equipment              │
│ • Users                  │
│ • Maps                   │
│ (One problem = all down) │
└──────────────────────────┘

✅ NEW WAY (Microservices):
┌─────────┐  ┌─────────┐  ┌─────────┐
│ Trails  │  │ Weather │  │ Equip   │
│Service  │  │Service  │  │Service  │
└─────────┘  └─────────┘  └─────────┘
   (If Weather goes down, Trails still work)
```

**Benefits:**
- 🚀 Each service scales independently
- 👥 Different teams can work on different services
- 🔧 Update one without affecting others
- 💪 More resilient to failures

---

## 📊 Technology Stack

### Backend Services
- **Language:** Java 21
- **Framework:** Spring Boot 3.2
- **Build:** Gradle
- **Testing:** JUnit 5, Mockito, TestContainers

### Frontend
- **Framework:** React 18
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **Build:** Vite

### Database
- **Primary DB:** PostgreSQL 14
- **Geographic Extension:** PostGIS (for map queries)
- **Connection Pooling:** HikariCP

### DevOps
- **Containerization:** Docker
- **Orchestration:** Docker Compose (local) / Kubernetes (production)
- **CI/CD:** GitLab Pipelines
- **Build Artifact:** Docker images pushed to registry

---

## 🗂️ File Structure

```
TrailEquip/
│
├── services/                    # All backend services
│   ├── api-gateway/
│   │   ├── src/main/java/      # Gateway code
│   │   └── src/test/           # Gateway tests
│   │
│   ├── trail-service/          # Trail management
│   ├── weather-service/        # Weather integration
│   └── recommendation-service/ # Equipment recommendations
│
├── ui/                         # React frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── pages/              # Page layouts
│   │   └── services/           # API calls
│   └── package.json
│
├── infra/                      # Infrastructure
│   ├── docker-compose.yml      # All containers + networking
│   └── db/
│       └── init.sql            # Database schema + seed data
│
├── docs/                       # Documentation
│   ├── ARCHITECTURE.md         # This file
│   ├── QUICKSTART.md           # Setup guide
│   └── ADRs/                   # Architecture decisions
│
└── build.gradle.kts           # Root build configuration
```

---

## 🔌 Communication Between Services

### How Services Talk to Each Other

```
┌─────────────────────┐
│  Trail Service      │
│  (Knows about       │
│   trails in DB)     │
└─────────────────────┘
           │
           │ (HTTP REST)
           │ POST /api/v1/recommendations/equipment
           │ {trailId, weather, userLevel}
           ▼
┌─────────────────────┐
│ Equipment Service   │
│ (Looks up trail     │
│  in Trail Service)  │
└─────────────────────┘
           │
           │ Uses trail data to recommend
           │ boots, jacket, backpack, etc.
           ▼
       Response
```

**Note:** Services communicate via HTTP REST APIs. No direct database access between services—each owns their own data.

---

## 📈 Data Flow Example: Complete Journey

### User Search Flow

```
USER INTERFACE
     │
     │ 1. User searches: "Easy trails near Bulea Lake"
     ▼
API GATEWAY
     │
     ├─→ 2a. Route to Trail Service
     │       ↓
     │   TRAIL SERVICE
     │       │
     │       └─→ 3a. Query DB: SELECT * FROM trails WHERE difficulty='easy'
     │           ↓
     │       DB RETURNS: [Trail1, Trail2, Trail3]
     │
     ├─→ 2b. Route to Weather Service (for Bulea Lake location)
     │       ↓
     │   WEATHER SERVICE
     │       │
     │       └─→ 3b. Fetch forecast for coordinates (45.4°N, 25.5°E)
     │           ↓
     │       EXTERNAL API (weather provider)
     │           Returns: Temp 15°C, Wind 10km/h, Rain 20%
     │
     ├─→ 2c. Route to Equipment Service
     │       ↓
     │   EQUIPMENT SERVICE
     │       │
     │       ├─→ 3c. Analyze: Easy trail + 15°C + 20% rain
     │       │       ↓
     │       └─→ Recommend: Hiking boots, Rain jacket, Backpack
     │
     └─→ 4. Combine all responses
         ↓
API RESPONSE TO BROWSER:
{
  "trails": ["Bulea Lake Loop"],
  "weather": {"temp": 15, "rain": 20},
  "equipment": ["boots", "jacket", "backpack"]
}
         ↓
     USER SEES:
  "Bulea Lake Loop - Easy - 15°C
   Bring: Hiking boots, Rain jacket, Backpack"
```

---

## 🔐 Security Layers

```
┌─────────────────────────────────┐
│ 1. HTTPS/TLS Encryption         │ (Data in transit)
├─────────────────────────────────┤
│ 2. API Gateway Authentication   │ (JWT tokens)
├─────────────────────────────────┤
│ 3. Service-to-Service Auth      │ (Internal tokens)
├─────────────────────────────────┤
│ 4. Database User Permissions    │ (Row-level access)
├─────────────────────────────────┤
│ 5. Rate Limiting                │ (Prevent abuse)
├─────────────────────────────────┤
│ 6. Input Validation             │ (Prevent injection)
└─────────────────────────────────┘
```

---

## 🚀 Deployment Architecture

### Local Development (Your Laptop)

```
Your Machine
├── Docker Container 1: API Gateway
├── Docker Container 2: Trail Service
├── Docker Container 3: Weather Service
├── Docker Container 4: Equipment Service
├── Docker Container 5: React UI
└── Docker Container 6: PostgreSQL

All connected via Docker Network
```

### Production (Cloud)

```
Load Balancer
     │
     ├─→ Kubernetes Cluster
     │   ├─→ Pod: API Gateway (multiple replicas)
     │   ├─→ Pod: Trail Service (scales up/down)
     │   ├─→ Pod: Weather Service (cached responses)
     │   ├─→ Pod: Equipment Service
     │   └─→ Pod: React UI (CDN)
     │
     └─→ Managed Database
         └─→ PostgreSQL (automated backups)
```

---

## 📊 Decision Matrix

| Decision | Choice | Why |
|----------|--------|-----|
| **Architecture** | Microservices | Scale services independently |
| **Language** | Java | Enterprise-grade, type-safe, mature |
| **Framework** | Spring Boot | Largest ecosystem, production-ready |
| **Database** | PostgreSQL | Reliable, PostGIS support for maps |
| **Frontend** | React | Component-based, large community |
| **Containers** | Docker | Consistency across environments |
| **Orchestration** | Kubernetes (prod) | Auto-scaling, self-healing |

---

## 🔄 Key Architectural Principles

### 1. **Separation of Concerns**
Each service handles one domain:
- Trail Service = Trails only
- Weather Service = Weather only
- Equipment Service = Recommendations only

### 2. **Independent Deployment**
Update one service without touching others.

### 3. **Scalability**
Need more trails? Scale Trail Service. Need faster weather? Scale Weather Service.

### 4. **Resilience**
If Weather Service is slow, users still see trails and equipment recommendations.

### 5. **Testability**
Each service can be tested independently with mocked dependencies.

---

## 🎓 For Architects: Technical Details

### Service Contracts

Each service exposes a REST API with documented endpoints:

**Trail Service API**
```
GET    /api/v1/trails              → List all trails
GET    /api/v1/trails/:id          → Trail details
GET    /api/v1/trails/near/:lat/:lng → Geo-query
POST   /api/v1/trails              → Create trail (admin)
```

**Weather Service API**
```
GET    /api/v1/weather/forecast    → Get forecast
POST   /api/v1/weather/cache/clear → Clear cache (admin)
```

**Equipment Service API**
```
POST   /api/v1/recommendations/equipment → Get recommendations
```

### Data Model

```
┌─────────────────────┐
│ TRAILS              │
├─────────────────────┤
│ id (UUID)           │
│ name (String)       │
│ difficulty (Enum)   │
│ latitude, longitude │
│ elevation (Integer) │
│ distance (Float)    │
│ description (Text)  │
│ created_at          │
└─────────────────────┘

┌──────────────────────┐
│ EQUIPMENT            │
├──────────────────────┤
│ id (UUID)            │
│ name (String)        │
│ type (Enum)          │
│ weather_threshold    │
│ difficulty_level     │
│ price (Decimal)      │
└──────────────────────┘

┌──────────────────────┐
│ RECOMMENDATIONS      │
├──────────────────────┤
│ id (UUID)            │
│ trail_id (FK)        │
│ equipment_id (FK)    │
│ reason (String)      │
│ created_at           │
└──────────────────────┘
```

### Failure Handling

```
Service A calls Service B
    ↓
    ├─ If B responds: Use response
    ├─ If B times out: Retry with exponential backoff
    ├─ If B fails: Use cached response or default
    └─ If B is down: Circuit breaker opens, return graceful error
```

---

## 📚 References

- [Quick Start Guide](./QUICKSTART.md)
- [Architecture Decision Records](./ADRs/)
- [API Documentation](../services/api-gateway/README.md)
- [Database Schema](../infra/db/init.sql)

---

**Last Updated:** January 28, 2025 | **Status:** MVP Ready
