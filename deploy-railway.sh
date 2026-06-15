#!/bin/bash

# Railway CLI Deployment Script
# Automates Railway deployment process

echo "🚀 Railway Deployment Script"
echo "=============================="
echo ""

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null
then
    echo "❌ Railway CLI not found. Installing..."
    npm install -g @railway/cli
    echo "✅ Railway CLI installed"
fi

# Login to Railway
echo ""
echo "📝 Step 1: Login to Railway"
echo "This will open your browser..."
railway login

# Create or link project
echo ""
echo "📝 Step 2: Initialize Railway Project"
read -p "Do you want to create a new project? (y/n): " create_new

if [ "$create_new" = "y" ]; then
    railway init
else
    railway link
fi

# Deploy infrastructure first
echo ""
echo "📝 Step 3: Adding Infrastructure"
echo "Please add these via Railway Dashboard:"
echo "  1. PostgreSQL (New → Database → Add PostgreSQL)"
echo "  2. Redis (New → Database → Add Redis)"
echo ""
read -p "Press Enter when infrastructure is ready..."

# Get Upstash Kafka credentials
echo ""
echo "📝 Step 4: Upstash Kafka Setup"
echo "1. Go to https://console.upstash.com"
echo "2. Create a Kafka cluster (Free tier)"
echo "3. Create topic: 'order-events'"
echo ""
read -p "Enter Upstash Kafka Bootstrap Server: " KAFKA_SERVERS
read -p "Enter Upstash Username: " KAFKA_USERNAME
read -p "Enter Upstash Password: " KAFKA_PASSWORD

# Get JWT Secret
echo ""
read -p "Enter JWT Secret (min 32 chars): " JWT_SECRET

# Deploy API Gateway
echo ""
echo "📝 Step 5: Deploying API Gateway"
echo "Creating service..."
railway up --service api-gateway --dockerfile api-gateway/Dockerfile

echo "Setting environment variables..."
railway variables --service api-gateway set \
  SPRING_PROFILES_ACTIVE=production \
  SERVER_PORT=8080 \
  JWT_SECRET="$JWT_SECRET"

# Deploy User Service
echo ""
echo "📝 Step 6: Deploying User Service"
railway up --service user-service --dockerfile user-service/Dockerfile

railway variables --service user-service set \
  SPRING_PROFILES_ACTIVE=production \
  SERVER_PORT=8081 \
  JWT_SECRET="$JWT_SECRET"

# Deploy Product Service
echo ""
echo "📝 Step 7: Deploying Product Service"
railway up --service product-service --dockerfile product-service/Dockerfile

railway variables --service product-service set \
  SPRING_PROFILES_ACTIVE=production \
  SERVER_PORT=8082

# Deploy Order Service
echo ""
echo "📝 Step 8: Deploying Order Service"
railway up --service order-service --dockerfile order-service/Dockerfile

railway variables --service order-service set \
  SPRING_PROFILES_ACTIVE=production \
  SERVER_PORT=8083 \
  KAFKA_SERVERS="$KAFKA_SERVERS" \
  KAFKA_USERNAME="$KAFKA_USERNAME" \
  KAFKA_PASSWORD="$KAFKA_PASSWORD"

# Deploy Notification Service
echo ""
echo "📝 Step 9: Deploying Notification Service"
railway up --service notification-service --dockerfile notification-service/Dockerfile

railway variables --service notification-service set \
  SPRING_PROFILES_ACTIVE=production \
  SERVER_PORT=8084 \
  KAFKA_SERVERS="$KAFKA_SERVERS" \
  KAFKA_USERNAME="$KAFKA_USERNAME" \
  KAFKA_PASSWORD="$KAFKA_PASSWORD"

# Summary
echo ""
echo "=============================="
echo "✅ Deployment Complete!"
echo "=============================="
echo ""
echo "Next Steps:"
echo "1. Go to Railway Dashboard"
echo "2. Check deployment logs for each service"
echo "3. Generate public domain for API Gateway"
echo "4. Test your deployment"
echo ""
echo "View your project: railway open"
echo ""
