#!/bin/bash

# Railway Kafka Deployment Script
# Deploys self-hosted Kafka on Railway

echo "🚀 Railway Kafka Deployment Script"
echo "==================================="
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

# Link to existing project or create new
echo ""
echo "📝 Step 2: Link to Your Railway Project"
railway link

# Add Kafka service
echo ""
echo "📝 Step 3: Adding Kafka Service"
echo ""
echo "Creating Kafka service..."

# Create new service for Kafka
railway service create kafka

# Deploy Kafka using bitnami image
echo ""
echo "Deploying Kafka container (bitnami/kafka:3.6)..."
railway up --service kafka --detach

# Set Kafka environment variables
echo ""
echo "📝 Step 4: Configuring Kafka Environment Variables"
railway variables --service kafka set \
  KAFKA_CFG_NODE_ID=0 \
  KAFKA_CFG_PROCESS_ROLES=controller,broker \
  KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka.railway.internal:9092 \
  KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@localhost:9093 \
  KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  ALLOW_PLAINTEXT_LISTENER=yes \
  KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true

echo ""
echo "⏳ Waiting for Kafka to start (this takes ~2-3 minutes)..."
sleep 120

# Update order-service with Kafka URL
echo ""
echo "📝 Step 5: Updating Service Configurations"
echo "Setting KAFKA_SERVERS for order-service..."
railway variables --service order-service set \
  KAFKA_SERVERS=kafka.railway.internal:9092

echo "Setting KAFKA_SERVERS for notification-service..."
railway variables --service notification-service set \
  KAFKA_SERVERS=kafka.railway.internal:9092

# Summary
echo ""
echo "======================================"
echo "✅ Kafka Deployment Complete!"
echo "======================================"
echo ""
echo "Kafka Details:"
echo "  Service: kafka"
echo "  Internal URL: kafka.railway.internal:9092"
echo "  Auto-create topics: Enabled"
echo ""
echo "Updated Services:"
echo "  ✅ order-service"
echo "  ✅ notification-service"
echo ""
echo "Next Steps:"
echo "1. Check Kafka logs: railway logs --service kafka"
echo "2. Verify Kafka is running (look for 'Kafka Server started')"
echo "3. Test by placing an order through API Gateway"
echo "4. Check notification-service logs for Kafka events"
echo ""
echo "View services: railway status"
echo "View logs: railway logs"
echo ""
