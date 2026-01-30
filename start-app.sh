#!/bin/bash

# TrailEquip Application Startup Script
# This script starts PostgreSQL, builds and runs the backend services, and starts the frontend

set -e

PROJECT_DIR="/Users/viionascu/Projects/TrailEquip"
FRONTEND_DIR="$PROJECT_DIR/ui"
BACKEND_PORT=8081
POSTGRES_PORT=5432

# Set Java configuration for Java 21
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH=$JAVA_HOME/bin:$PATH

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║      TrailEquip Application Startup Script             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}\n"

# Step 1: Check prerequisites
echo -e "${YELLOW}[1/5] Checking prerequisites...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java not found. Please install Java 21 or higher${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java found: $(java -version 2>&1 | head -1)${NC}"

# Use PostgreSQL 17
if ! command -v /opt/homebrew/opt/postgresql@17/bin/psql &> /dev/null; then
    echo -e "${RED}✗ PostgreSQL 17 not found. Please install PostgreSQL 17 with PostGIS support${NC}"
    exit 1
fi
echo -e "${GREEN}✓ PostgreSQL 17 client found${NC}"

if ! command -v node &> /dev/null; then
    echo -e "${RED}✗ Node.js not found. Please install Node.js${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Node.js found: $(node --version)${NC}"

if ! command -v npm &> /dev/null; then
    echo -e "${RED}✗ npm not found. Please install npm${NC}"
    exit 1
fi
echo -e "${GREEN}✓ npm found: $(npm --version)${NC}\n"

# Step 2: Start PostgreSQL 17
echo -e "${YELLOW}[2/5] Starting PostgreSQL 17 with PostGIS...${NC}"

# Check if PostgreSQL is already running
if /opt/homebrew/opt/postgresql@17/bin/pg_isready -h localhost -p $POSTGRES_PORT &> /dev/null; then
    echo -e "${GREEN}✓ PostgreSQL 17 is already running${NC}"
else
    echo -e "${BLUE}  Starting PostgreSQL 17 service...${NC}"
    if brew services start postgresql@17 2>/dev/null; then
        echo -e "${GREEN}✓ PostgreSQL 17 started successfully${NC}"
        sleep 2
    else
        echo -e "${RED}✗ Failed to start PostgreSQL 17${NC}"
        exit 1
    fi
fi

# Verify PostgreSQL is accessible
if /opt/homebrew/opt/postgresql@17/bin/pg_isready -h localhost -p $POSTGRES_PORT &> /dev/null; then
    echo -e "${GREEN}✓ PostgreSQL 17 is accessible with PostGIS support${NC}\n"
else
    echo -e "${RED}✗ PostgreSQL 17 is not responding${NC}"
    exit 1
fi

# Step 3: Build backend
echo -e "${YELLOW}[3/5] Building backend services...${NC}"
cd "$PROJECT_DIR"

if ./gradlew clean build -x test --quiet; then
    echo -e "${GREEN}✓ Backend build completed successfully${NC}\n"
else
    echo -e "${RED}✗ Backend build failed${NC}"
    exit 1
fi

# Step 4: Start backend service in background
echo -e "${YELLOW}[4/5] Starting Trail Service (port $BACKEND_PORT)...${NC}"

# Start trail-service in background
./gradlew :trail-service:bootRun > /tmp/trail-service.log 2>&1 &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

# Wait for backend to start
echo -e "${BLUE}  Waiting for backend to start...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:$BACKEND_PORT/actuator/health &> /dev/null; then
        echo -e "${GREEN}✓ Trail Service is running on port $BACKEND_PORT${NC}\n"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}✗ Backend failed to start within 30 seconds${NC}"
        echo -e "${BLUE}  Check logs with: tail -f /tmp/trail-service.log${NC}"
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Step 5: Start frontend
echo -e "${YELLOW}[5/5] Starting Frontend development server...${NC}"
cd "$FRONTEND_DIR"

if [ ! -d "node_modules" ]; then
    echo -e "${BLUE}  Installing npm dependencies...${NC}"
    npm install --quiet
fi

echo -e "${GREEN}✓ Starting frontend on port 5173${NC}\n"
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║          TrailEquip is starting up!                   ║${NC}"
echo -e "${BLUE}║                                                        ║${NC}"
echo -e "${BLUE}║  Frontend:  http://localhost:5173                      ║${NC}"
echo -e "${BLUE}║  Backend:   http://localhost:8081                      ║${NC}"
echo -e "${BLUE}║  API Docs:  http://localhost:8081/swagger-ui.html      ║${NC}"
echo -e "${BLUE}║                                                        ║${NC}"
echo -e "${BLUE}║  Backend Log: tail -f /tmp/trail-service.log           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}\n"

# Start frontend (this will be in foreground)
npm run dev

# If frontend exits, kill the backend too
kill $BACKEND_PID 2>/dev/null || true
echo -e "${YELLOW}Application stopped${NC}"
