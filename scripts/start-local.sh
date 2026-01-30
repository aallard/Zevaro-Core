#!/bin/bash
set -e

echo "🚀 Starting Zevaro local development environment..."

# Start infrastructure
docker-compose up -d postgres redis kafka

# Wait for services
echo "⏳ Waiting for services to be healthy..."
sleep 15

docker-compose ps

echo "✅ Infrastructure ready!"
echo ""
echo "📋 Services:"
echo "  - PostgreSQL: localhost:5432 (zevaro/zevaro)"
echo "  - Redis:      localhost:6379"
echo "  - Kafka:      localhost:9094"
echo ""
echo "🏃 Run the application with:"
echo "  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
