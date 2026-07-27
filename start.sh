#!/bin/bash
# CourseHub Dev Environment Orchestrator for macOS / Linux
unset JAVA_HOME

echo "============================================================"
echo "     CourseHub - Automated Development Environment Launcher"
echo "============================================================"
echo ""

# 1. Check if Docker is running
echo "[1/6] Checking Docker status..."
if ! docker info >/dev/null 2>&1; then
    echo "[ERROR] Docker is not running!"
    echo "Please start Docker Desktop/Daemon first and try again."
    echo ""
    exit 1
fi
echo "[INFO] Docker is running."

# 2. Boot Docker Compose
echo ""
echo "[2/6] Starting database and cache services (MySQL, Redis)..."
docker compose up -d
if [ $? -ne 0 ]; then
    echo "[ERROR] Failed to start Docker Compose services!"
    exit 1
fi

# 3. Wait for MySQL to become healthy
echo ""
echo "[3/6] Waiting for MySQL container (coursehub-mysql) to become healthy..."
until [ "$(docker inspect --format='{{.State.Health.Status}}' coursehub-mysql 2>/dev/null)" == "healthy" ]; do
    sleep 2
done
echo "[INFO] MySQL is healthy!"

# 4. Wait for Redis to become healthy
echo ""
echo "[4/6] Waiting for Redis container (coursehub-redis) to become healthy..."
until [ "$(docker inspect --format='{{.State.Health.Status}}' coursehub-redis 2>/dev/null)" == "healthy" ]; do
    sleep 2
done
echo "[INFO] Redis is healthy!"

# 5. Check and install frontend dependencies
echo ""
echo "[5/6] Checking frontend dependencies..."
if [ ! -d "coursehub-frontend/node_modules" ]; then
    echo "[INFO] node_modules not found in coursehub-frontend. Installing dependencies..."
    cd coursehub-frontend && npm install && cd ..
    echo "[INFO] Dependencies installed successfully."
else
    echo "[INFO] Frontend dependencies are already installed."
fi

# 6. Launch Backend and Frontend in background
echo ""
echo "[6/6] Launching backend and frontend applications..."

# Create log files if not exists
touch backend.log frontend.log

# Start Backend
cd coursehub
./gradlew bootRun > ../backend.log 2>&1 &
BACKEND_PID=$!
cd ..
echo "[INFO] Backend running in background (PID: $BACKEND_PID, logging to backend.log)"

# Start Frontend
cd coursehub-frontend
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..
echo "[INFO] Frontend running in background (PID: $FRONTEND_PID, logging to frontend.log)"

# Setup cleanup trap on script exit
cleanup() {
    echo ""
    echo "============================================================"
    echo " Shutting down development servers..."
    echo "============================================================"
    if kill -0 $BACKEND_PID 2>/dev/null; then
        echo "Killing Backend (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null
    fi
    if kill -0 $FRONTEND_PID 2>/dev/null; then
        echo "Killing Frontend (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID 2>/dev/null
    fi
    echo "[INFO] Cleaned up background processes."
    exit 0
}
trap cleanup INT TERM EXIT

echo ""
echo "============================================================"
echo " All services are launching. Opening your browser shortly..."
echo "============================================================"
sleep 5

# Open browser depending on OS
if command -v open >/dev/null 2>&1; then
    open http://localhost:5173
elif command -v xdg-open >/dev/null 2>&1; then
    xdg-open http://localhost:5173
else
    echo "[INFO] Please open http://localhost:5173 in your browser."
fi

echo ""
echo "Tailing backend logs (Press Ctrl+C to terminate all servers)..."
echo "------------------------------------------------------------"
tail -f backend.log
