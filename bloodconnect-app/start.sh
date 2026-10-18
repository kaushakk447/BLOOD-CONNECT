#!/bin/bash

# BloodConnect Quick Start Script
# This script sets up and runs the entire BloodConnect application

set -e

echo "=========================================="
echo "  BloodConnect - Quick Start"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker Desktop."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Desktop."
    exit 1
fi

echo "✅ Docker and Docker Compose detected"
echo ""

# Navigate to docker directory
cd .docker

echo "Starting BloodConnect..."
echo ""
echo "🚀 Building and starting services..."
docker-compose up --build

echo ""
echo "=========================================="
echo "  BloodConnect is running!"
echo "=========================================="
echo ""
echo "📱 Frontend:  http://localhost:3000"
echo "🔌 Backend:   http://localhost:8080"
echo "🗄️  Database:  localhost:5432"
echo ""
echo "Use Ctrl+C to stop"
