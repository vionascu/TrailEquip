#!/bin/bash

# TrailEquip - Run All Services in One Terminal
# This script starts all 5 services in the background

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/.logs"

# Set Java environment for macOS
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export PATH="$JAVA_HOME/bin:$PATH"

# Create logs directory
mkdir -p "$LOG_DIR"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}       TrailEquip - Starting All Services${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""

# Function to check if a port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1  # Port is in use
    else
        return 0  # Port is available
    fi
}

# Function to stop all services
cleanup() {
    echo ""
    echo -e "${YELLOW}Stopping all services...${NC}"
    kill $TRAIL_PID $WEATHER_PID $RECOMMENDATION_PID $GATEWAY_PID $UI_PID 2>/dev/null || true
    echo -e "${GREEN}All services stopped.${NC}"
    exit 0
}

# Set trap to cleanup on exit (Ctrl+C)
trap cleanup SIGINT SIGTERM

# Check prerequisites
echo -e "${BLUE}✓ Checking prerequisites...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java not found. Please install Java 21+${NC}"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo -e "${RED}✗ npm not found. Please install Node.js 20+${NC}"
    exit 1
fi

if ! psql -lqt 2>/dev/null | grep -q trailequip; then
    echo -e "${RED}✗ Database 'trailequip' not found.${NC}"
    echo -e "${YELLOW}  Run this first:${NC}"
    echo -e "    createdb trailequip"
    echo -e "    psql trailequip < $SCRIPT_DIR/infra/db/init.sql"
    exit 1
fi

echo -e "${GREEN}✓ Prerequisites OK${NC}"
echo ""

# Check if ports are available
echo -e "${BLUE}✓ Checking ports...${NC}"
for port in 8081 8082 8083 8080 3000; do
    if ! check_port $port; then
        echo -e "${RED}✗ Port $port is already in use${NC}"
        exit 1
    fi
done
echo -e "${GREEN}✓ All ports available${NC}"
echo ""

# Start Trail Service
echo -e "${BLUE}Starting Trail Service (8081)...${NC}"
cd "$SCRIPT_DIR"
./gradlew :services:trail-service:bootRun > "$LOG_DIR/trail-service.log" 2>&1 &
TRAIL_PID=$!
echo -e "${GREEN}✓ Trail Service started (PID: $TRAIL_PID)${NC}"
echo "  Log: $LOG_DIR/trail-service.log"

# Start Weather Service
echo -e "${BLUE}Starting Weather Service (8082)...${NC}"
./gradlew :services:weather-service:bootRun > "$LOG_DIR/weather-service.log" 2>&1 &
WEATHER_PID=$!
echo -e "${GREEN}✓ Weather Service started (PID: $WEATHER_PID)${NC}"
echo "  Log: $LOG_DIR/weather-service.log"

# Start Recommendation Service
echo -e "${BLUE}Starting Recommendation Service (8083)...${NC}"
./gradlew :services:recommendation-service:bootRun > "$LOG_DIR/recommendation-service.log" 2>&1 &
RECOMMENDATION_PID=$!
echo -e "${GREEN}✓ Recommendation Service started (PID: $RECOMMENDATION_PID)${NC}"
echo "  Log: $LOG_DIR/recommendation-service.log"

# Start API Gateway
echo -e "${BLUE}Starting API Gateway (8080)...${NC}"
./gradlew :services:api-gateway:bootRun > "$LOG_DIR/api-gateway.log" 2>&1 &
GATEWAY_PID=$!
echo -e "${GREEN}✓ API Gateway started (PID: $GATEWAY_PID)${NC}"
echo "  Log: $LOG_DIR/api-gateway.log"

# Start UI
echo -e "${BLUE}Starting React UI (3000)...${NC}"
cd "$SCRIPT_DIR/ui"
npm install > "$LOG_DIR/ui-install.log" 2>&1
npm run dev > "$LOG_DIR/ui.log" 2>&1 &
UI_PID=$!
echo -e "${GREEN}✓ React UI started (PID: $UI_PID)${NC}"
echo "  Log: $LOG_DIR/ui.log"

echo ""
echo -e "${GREEN}════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✓ All services started!${NC}"
echo -e "${GREEN}════════════════════════════════════════════════════════════${NC}"
echo ""

echo -e "${BLUE}📍 Services Available:${NC}"
echo -e "  ${GREEN}UI:${NC}                    http://localhost:3000"
echo -e "  ${GREEN}API Gateway:${NC}          http://localhost:8080"
echo -e "  ${GREEN}API Docs (Swagger):${NC}   http://localhost:8080/swagger-ui.html"
echo -e "  ${GREEN}Health Check:${NC}         http://localhost:8080/api/v1/health"
echo ""

echo -e "${BLUE}📋 Service Logs:${NC}"
echo -e "  Trail Service:       tail -f $LOG_DIR/trail-service.log"
echo -e "  Weather Service:     tail -f $LOG_DIR/weather-service.log"
echo -e "  Recommendation Svc:  tail -f $LOG_DIR/recommendation-service.log"
echo -e "  API Gateway:         tail -f $LOG_DIR/api-gateway.log"
echo -e "  React UI:            tail -f $LOG_DIR/ui.log"
echo ""

echo -e "${YELLOW}⏹️  To stop all services: Press Ctrl+C${NC}"
echo ""

# Wait for all processes
wait
