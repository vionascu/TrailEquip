# TrailEquip Architecture - Simple Explanation

## 🏗️ The Big Picture

TrailEquip is like a **hiking assistant app** that:
1. 📍 Finds hiking trails
2. 🌤️ Shows you the weather
3. 🎒 Tells you what to pack

It's built with **4 services** that work together through a central coordinator.

---

## 🎯 How It Works (Simple Version)

```
┌─────────────────────────────────────────────────────────────┐
│                        YOUR PHONE/COMPUTER                   │
│                     (Runs the App/Website)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ You click buttons
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     API GATEWAY (Port 8080)                  │
│               "The Front Desk of the Hotel"                  │
│     Directs requests to the right service/department         │
└──┬──────────────────────┬──────────────────────┬────────────┘
   │                      │                      │
   │ Send to:             │ Send to:             │ Send to:
   │                      │                      │
   ▼                      ▼                      ▼
┌────────────────┐  ┌──────────────┐  ┌──────────────────┐
│ TRAIL SERVICE  │  │WEATHER SVCICE│  │RECOMMENDATION    │
│  (Port 8081)   │  │ (Port 8082)   │  │SERVICE           │
│                │  │               │  │ (Port 8083)      │
│ "Trail Expert" │  │"Weather Man"  │  │ "Equipment Guy"  │
└────────┬───────┘  └────────┬──────┘  └────────┬─────────┘
         │                   │                  │
         │ Talks to:         │ Talks to:       │ Asks both:
         │                   │                 │
         ▼                   ▼                 ▼
    ┌─────────┐         ┌─────────┐     ┌─────────────┐
    │PostgreSQL           │ Weather  │     │ Trail Data +
    │+ PostGIS            │ Website  │     │ Weather Info
    │(Database)           │(Free API)│     │
    │                     │          │     │ = Recommendations
    │Stores ALL           │Gets       │     │
    │Trail Info           │Weather    │     │Example:
    └─────────┘           └─────────┘     │"It's cold & wet
                                          │ → bring jacket!"
                                          └─────────────────┘
```

---

## 🏘️ Each Service Explained

### 1️⃣ API Gateway (Port 8080) - "The Receptionist"

**What it does:**
- You talk to this service
- It listens on port 8080
- It figures out which service you need
- Directs your request to the right place

**Like a hotel:**
```
You:  "Hi, I need trail info!"
Receptionist: "Oh, go to TRAIL SERVICE (port 8081)"

You:  "What's the weather?"
Receptionist: "Go to WEATHER SERVICE (port 8082)"

You:  "What should I pack?"
Receptionist: "Go to RECOMMENDATION SERVICE (port 8083)"
```

**You never talk directly to the other services - you always talk to this one.**

---

### 2️⃣ Trail Service (Port 8081) - "The Trail Expert"

**What it does:**
- Finds and stores hiking trails
- Gets data from OpenStreetMap (a free map database)
- Tells you trail difficulty (EASY, MEDIUM, HARD, etc.)
- Exports trails for GPS devices

**Example:**
```
You ask: "Show me all HARD trails near Bucegi Mountains"

Trail Service does:
1. Asks OpenStreetMap: "What trails are there?"
2. Grades them: "This one is HARD, this one is MEDIUM"
3. Stores in database
4. Sends you: "Here are 5 HARD trails 🥾"
```

**What it stores:**
- Trail name
- Trail length (km)
- How high you climb (elevation gain)
- Difficulty level
- Terrain type (rocky, forest, etc.)
- Hazards (exposed cliffs, bears, etc.)

---

### 3️⃣ Weather Service (Port 8082) - "The Weather Person"

**What it does:**
- Gets weather forecasts from Open-Meteo (free website)
- Saves the data so it doesn't ask the same question twice
- Tells you temperature, rain, wind for any location

**Example:**
```
You ask: "What's the weather at Bucegi for Jan 31?"

Weather Service does:
1. Checks if it already knows (cached)
2. If not, asks Open-Meteo API
3. Gets: Temp: 5°C, Rain: 50%, Wind: 25 km/h
4. Saves for 6 hours (so next person gets instant answer!)
5. Sends you the forecast
```

**Smart caching:**
```
First person: "Weather forecast?" → Asks website (10 seconds)
Next 100 people: "Weather forecast?" → Gets instant answer! (0.1 seconds)
```

---

### 4️⃣ Recommendation Service (Port 8083) - "The Packing Expert"

**What it does:**
- Takes trail info + weather
- Uses smart logic to say what to pack
- Gives you warnings

**Example:**
```
Trail: HARD, 2000m elevation, exposed ridge
Weather: 5°C, 60% rain, 35 km/h wind

Recommendation Service says:
✅ Pack: Thermal base layer, rain jacket, microspikes
⚠️ Warning: "High wind on ridges - be careful!"
```

**The Logic:**
```
Temperature checks:
- 0-5°C?   → Need insulation (jacket)
- 5-10°C?  → Need mid-layer (fleece)
- Cold + wet? → Need microspikes!

Precipitation checks:
- No rain?     → Normal clothes OK
- Little rain? → Bring rain jacket
- Lots rain?   → Bring full rain gear

Wind checks:
- Light wind (< 20 km/h)? → Fine
- Medium wind (20-30)? → Be careful on ridges
- Strong wind (> 30)? → Might cancel hike
```

---

## 🗺️ How Requests Flow

### Scenario: "I want to hike tomorrow, what should I pack?"

```
Step 1: You (on phone/computer)
└─→ "I want to hike Omu Peak tomorrow"

Step 2: API Gateway (Port 8080)
└─→ "OK, let me help you. I'll call the other services"

Step 3: Trail Service (Port 8081)
└─→ "Omu Peak is a HARD trail, 12.5 km, 1200m elevation,
    exposed ridge, rocky terrain"

Step 4: Weather Service (Port 8082)
└─→ "Tomorrow: 5°C, 50% rain, 30 km/h wind"

Step 5: Recommendation Service (Port 8083)
└─→ Takes BOTH pieces of information:
    - Trail: HARD, exposed
    - Weather: Cold, rainy, windy
    - Decides: "Bring thermal layers, rain jacket, microspikes"

Step 6: Back to You
└─→ You get: "Recommended gear: ..." + "⚠️ Warnings: ..."
```

---

## 🔗 The Connections Explained

### Backend Services Talk to Each Other

```
Recommendation Service needs info from:
├─ Trail Service: "What's the difficulty & terrain?"
└─ Weather Service: "What's the forecast?"

Then it combines both:
HARD trail + cold wet weather = "Pack microspikes & rain gear!"
```

### External Services (Outside Companies)

```
Trail Service talks to:
└─ OpenStreetMap (OSM) - "Where are the trails?"

Weather Service talks to:
└─ Open-Meteo - "What's the weather?"

Database (PostgreSQL):
└─ Stores all trail information
```

---

## 📊 Complete Architecture Map

```
┌─────────────────────────────────────────────────────────────┐
│                     YOU (Phone/Computer)                     │
│                   Using the App/Website                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────────┐
        │     API GATEWAY (Port 8080)          │
        │  "Main Entrance - Directs Traffic"   │
        └─┬─────────────────────────┬────────┬─┘
          │                         │        │
       Route to         Route to    Route to │
          │                │        │        │
          ▼                ▼        ▼        ▼
    ┌─────────────┐  ┌──────────┐  ┌──────────────┐
    │Trail Svc    │  │Weather   │  │Recommendation│
    │8081         │  │8082      │  │8083          │
    └──────┬──────┘  └────┬─────┘  └──────┬───────┘
           │              │               │
           ▼              ▼               ▼
        ┌─────┐       ┌──────┐      Combines
        │OSM  │       │Open- │      Info ←─┐
        │API  │       │Meteo │          │  │
        └─────┘       └──────┘      Sends  │
           ▲              ▲          Back   │
           │              │              │  │
           │              │          Services
           └──────────────┘          Talk!
                                         │
                                         ▼
                                    ┌──────────┐
                                    │PostgreSQL│
                                    │Database  │
                                    └──────────┘
                                    (Stores All
                                     Trail Info)
```

---

## 🎓 Key Takeaways

| Concept | Explanation |
|---------|-------------|
| **Port** | Like a telephone number - each service has one |
| **API Gateway** | The main entry point - you talk to this |
| **Service** | A specialized worker doing one job |
| **Database** | Where all the trail info is saved |
| **Cache** | Save answers so you don't ask twice |
| **External API** | Services from other companies (OpenStreetMap, Open-Meteo) |

---

## 🚀 Real-World Analogy

**Think of it like a restaurant:**

```
You walk in:
└─ Greeter (API Gateway - 8080): "Welcome! What do you need?"
   ├─ "I want a special steak"
   │  └─ Chef (Trail Service - 8081): "Here's a HARD trail"
   ├─ "What's outside?"
   │  └─ Weather Guy (Weather Service - 8082): "It's cold & rainy"
   └─ "What should I bring?"
      └─ Manager (Recommendation Service - 8083):
         "Combine the steak info + weather =
          Pack a jacket & umbrella!"
```

**Each person (service):**
- Does ONE job well
- Talks to specific people (other services)
- Can be replaced without breaking the restaurant
- Makes the restaurant efficient

---

## 📚 For More Details

- **More architecture details:** See [ARCHITECTURE.md](ARCHITECTURE.md)
- **All endpoints with examples:** See [API_REFERENCE.md](API_REFERENCE.md)
- **How to start the app:** See [STARTUP.md](STARTUP.md)

---

**Remember:** Each service is like a team member. Together they help you find the perfect trail and pack the right gear! 🥾🎒
