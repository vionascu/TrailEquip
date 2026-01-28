#!/bin/bash

# View logs from all services in one view
# Usage: ./view-logs.sh [service-name]

LOG_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/.logs"

if [ ! -d "$LOG_DIR" ]; then
    echo "❌ No logs directory found. Have you started services with ./run-all.sh?"
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "📋 Available Services:"
    echo ""
    echo "  View specific service logs:"
    echo "    ./view-logs.sh trail          - Trail Service"
    echo "    ./view-logs.sh weather        - Weather Service"
    echo "    ./view-logs.sh recommendation - Recommendation Service"
    echo "    ./view-logs.sh gateway        - API Gateway"
    echo "    ./view-logs.sh ui             - React UI"
    echo ""
    echo "  Or view all logs at once:"
    echo "    tail -f $LOG_DIR/*.log"
    echo ""
else
    case $1 in
        trail)
            tail -f "$LOG_DIR/trail-service.log"
            ;;
        weather)
            tail -f "$LOG_DIR/weather-service.log"
            ;;
        recommendation)
            tail -f "$LOG_DIR/recommendation-service.log"
            ;;
        gateway)
            tail -f "$LOG_DIR/api-gateway.log"
            ;;
        ui)
            tail -f "$LOG_DIR/ui.log"
            ;;
        *)
            echo "❌ Unknown service: $1"
            echo "Valid options: trail, weather, recommendation, gateway, ui"
            exit 1
            ;;
    esac
fi
