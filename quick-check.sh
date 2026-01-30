#!/bin/bash

# Quick system check for TrailEquip prerequisites

# Set Java configuration for Java 21
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH=$JAVA_HOME/bin:$PATH

echo "╔════════════════════════════════════════════════════════╗"
echo "║          TrailEquip Quick System Check                 ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

echo "Checking Prerequisites..."
echo ""

# Java
echo -n "Java 21+: "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep -oE 'version "[^"]+' | cut -d' ' -f2 | tr -d '"')
    echo "✓ Found (v$JAVA_VERSION)"
else
    echo "✗ NOT FOUND"
fi

# PostgreSQL
echo -n "PostgreSQL: "
if command -v psql &> /dev/null; then
    PG_VERSION=$(psql --version)
    echo "✓ $PG_VERSION"
else
    echo "✗ NOT FOUND"
fi

# Node.js
echo -n "Node.js: "
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo "✓ $NODE_VERSION"
else
    echo "✗ NOT FOUND"
fi

# npm
echo -n "npm: "
if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm --version)
    echo "✓ v$NPM_VERSION"
else
    echo "✗ NOT FOUND"
fi

echo ""
echo "Checking Services..."
echo ""

# PostgreSQL running
echo -n "PostgreSQL Running: "
if pg_isready -h localhost -p 5432 &> /dev/null; then
    echo "✓ Running on port 5432"
else
    echo "✗ NOT running (start with: brew services start postgresql@15)"
fi

# Backend API
echo -n "Trail Service (8081): "
if curl -s http://localhost:8081/actuator/health &> /dev/null; then
    echo "✓ Running"
else
    echo "✗ Not running"
fi

# Frontend dev server
echo -n "Frontend Dev Server (5173): "
if curl -s http://localhost:5173 &> /dev/null; then
    echo "✓ Running"
else
    echo "✗ Not running"
fi

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║          Setup Instructions                            ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "Terminal 1 - Start PostgreSQL (if not running):"
echo "  brew services start postgresql@15"
echo ""
echo "Terminal 2 - Build and run Backend:"
echo "  cd /Users/viionascu/Projects/TrailEquip"
echo "  ./gradlew clean build -x test"
echo "  ./gradlew :trail-service:bootRun"
echo ""
echo "Terminal 3 - Start Frontend:"
echo "  cd /Users/viionascu/Projects/TrailEquip/ui"
echo "  npm install  # if needed"
echo "  npm run dev"
echo ""
echo "Access the application at: http://localhost:5173"
echo ""
