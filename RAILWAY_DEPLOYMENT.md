# 🚀 Railway.app Deployment Guide

Complete step-by-step guide to deploy your E-Commerce Order System on Railway.app (FREE).

## 📋 Prerequisites

1. GitHub account
2. Railway.app account (sign up at https://railway.app)
3. Upstash account for Kafka (sign up at https://upstash.com)

---

## 🎯 PART 1: Setup Upstash Kafka (Free Tier)

### Step 1: Create Upstash Kafka Cluster

```bash
1. Go to https://console.upstash.com
2. Click "Create Cluster"
3. Select "Free" tier
4. Choose region (closest to you)
5. Name: ecom-kafka
6. Click "Create"
```

### Step 2: Create Kafka Topics

```bash
# In Upstash Console → Topics
1. Create topic: "order-events"
   - Partitions: 1
   - Retention: 7 days
```

### Step 3: Get Kafka Credentials

```bash
# In Upstash Console → Your Cluster → Details
Copy these values:
- Bootstrap Servers (e.g., gorgeous-shad-12345-us1-kafka.upstash.io:9092)
- Username
- Password
```

---

## 🎯 PART 2: Setup Railway Project

### Step 1: Create Railway Account

```bash
1. Go to https://railway.app
2. Click "Login" → Sign in with GitHub
3. Authorize Railway
```

### Step 2: Create New Project

```bash
1. Dashboard → "New Project"
2. Choose "Deploy from GitHub repo"
3. Select: Ashish2409/ecom-order-system
4. Click "Deploy"
```

### Step 3: Add PostgreSQL Database

```bash
1. In your project → Click "New"
2. Select "Database" → "Add PostgreSQL"
3. Railway will create a PostgreSQL instance
4. Note: Railway auto-generates DATABASE_URL
```

### Step 4: Add Redis

```bash
1. In your project → Click "New"
2. Select "Database" → "Add Redis"
3. Railway will create a Redis instance
4. Note: Railway auto-generates REDIS_URL
```

---

## 🎯 PART 3: Deploy Services (One by One)

### Service 1: API Gateway

```bash
1. Project → "New" → "GitHub Repo"
2. Select: ecom-order-system
3. Service Name: api-gateway
4. Root Directory: /
5. Railway auto-detects Dockerfile
```

**Set Environment Variables:**
```bash
# In api-gateway service → Variables
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
JWT_SECRET=yourProductionSecretMustBeAtLeast32CharactersLongForHS256!

# Database (use Railway's references)
DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

# Redis (use Railway's references)
REDIS_HOST=${{Redis.REDIS_HOST}}
REDIS_PORT=${{Redis.REDIS_PORT}}
REDIS_PASSWORD=${{Redis.REDIS_PASSWORD}}

# Service URLs (we'll update these after deploying other services)
USER_SERVICE_URL=http://user-service.railway.internal:8081
PRODUCT_SERVICE_URL=http://product-service.railway.internal:8082
ORDER_SERVICE_URL=http://order-service.railway.internal:8083
NOTIFICATION_SERVICE_URL=http://notification-service.railway.internal:8084
```

**Deploy:**
```bash
Click "Deploy" button
Wait for build to complete (~5 minutes first time)
```

---

### Service 2: User Service

```bash
1. Project → "New" → "GitHub Repo"
2. Select: ecom-order-system
3. Service Name: user-service
4. Root Directory: /
```

**Set Environment Variables:**
```bash
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8081
JWT_SECRET=yourProductionSecretMustBeAtLeast32CharactersLongForHS256!

DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

**Deploy:**
```bash
Click "Deploy"
```

---

### Service 3: Product Service

```bash
1. Project → "New" → "GitHub Repo"
2. Select: ecom-order-system
3. Service Name: product-service
```

**Set Environment Variables:**
```bash
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8082

DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

**Deploy:**
```bash
Click "Deploy"
```

---

### Service 4: Order Service

```bash
1. Project → "New" → "GitHub Repo"
2. Select: ecom-order-system
3. Service Name: order-service
```

**Set Environment Variables:**
```bash
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8083

DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

# Kafka (from Upstash)
KAFKA_SERVERS=gorgeous-shad-12345-us1-kafka.upstash.io:9092
KAFKA_USERNAME=your-upstash-username
KAFKA_PASSWORD=your-upstash-password

# Product Service URL
PRODUCT_SERVICE_URL=http://product-service.railway.internal:8082
```

**Deploy:**
```bash
Click "Deploy"
```

---

### Service 5: Notification Service

```bash
1. Project → "New" → "GitHub Repo"
2. Select: ecom-order-system
3. Service Name: notification-service
```

**Set Environment Variables:**
```bash
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8084

DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

# Kafka (from Upstash)
KAFKA_SERVERS=gorgeous-shad-12345-us1-kafka.upstash.io:9092
KAFKA_USERNAME=your-upstash-username
KAFKA_PASSWORD=your-upstash-password
```

**Deploy:**
```bash
Click "Deploy"
```

---

## 🎯 PART 4: Configure Public Access

### Step 1: Generate Public Domain for API Gateway

```bash
1. Click on "api-gateway" service
2. Settings → Networking → "Generate Domain"
3. Railway creates: api-gateway-production-xxxx.up.railway.app
4. Copy this URL
```

### Step 2: Update CORS in API Gateway

Add the Railway URL to allowed origins in `api-gateway/src/main/resources/application-production.yml`:
```yaml
allowedOrigins: "https://api-gateway-production-xxxx.up.railway.app"
```

---

## 🎯 PART 5: Database Initialization

### Option A: Automatic (Recommended)

Railway will run `init.sql` automatically if you configure it:

```bash
1. In PostgreSQL service → Settings → Variables
2. Add: RAILWAY_RUN_SQL=true
3. Upload init.sql via Railway Dashboard
```

### Option B: Manual

```bash
1. Railway Dashboard → Postgres → Data
2. Click "Query"
3. Copy contents of init.sql
4. Execute
```

---

## 🎯 PART 6: Test Your Deployment

### Test API Gateway

```bash
# Health check
curl https://api-gateway-production-xxxx.up.railway.app/actuator/health

# Register user
curl -X POST https://api-gateway-production-xxxx.up.railway.app/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "phone": "1234567890"
  }'

# Login
curl -X POST https://api-gateway-production-xxxx.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Get products (public)
curl https://api-gateway-production-xxxx.up.railway.app/api/products
```

---

## 🎯 PART 7: Setup Auto-Deployment

Railway automatically deploys on every push to main branch.

**To configure:**
```bash
1. Each service → Settings → Deployment
2. Enable "Auto Deploy" (default: ON)
3. Branch: main
```

---

## 💰 Cost Estimation (Free Tier)

### Railway Free Plan:
- $5/month credit
- 500 hours/month per service
- Sleeps after 30 minutes of inactivity

### What Runs 24/7:
- ✅ API Gateway (public-facing)
- ✅ PostgreSQL
- ✅ Redis

### What Sleeps:
- 💤 User Service (wakes on request)
- 💤 Product Service (wakes on request)
- 💤 Order Service (wakes on request)
- 💤 Notification Service (wakes on request)

### Upstash Kafka (Free):
- ✅ 10,000 messages/day
- ✅ 100 MB storage
- ✅ 1 day retention

**Total Monthly Cost: $0** (within free tiers)

---

## 🔧 Troubleshooting

### Service Won't Start

```bash
# Check logs
Railway Dashboard → Service → Deployments → Logs

# Common issues:
1. Wrong environment variables
2. Database connection failed
3. Port already in use
```

### Database Connection Failed

```bash
# Verify DATABASE_URL format
jdbc:postgresql://host:port/database

# Check Railway variables
Postgres service → Variables → Copy connection strings
```

### Kafka Connection Failed

```bash
# Verify Upstash credentials
1. Upstash Console → Your Cluster → Details
2. Copy exact values
3. Update Railway environment variables
```

### Service Timeout

```bash
# First deployment takes ~5-10 minutes
# Be patient, Railway is building Docker images

# If it times out:
1. Settings → Deployment → Redeploy
```

---

## 🎉 Success Checklist

- [ ] Upstash Kafka cluster created
- [ ] Railway project created
- [ ] PostgreSQL added
- [ ] Redis added
- [ ] All 5 services deployed
- [ ] Environment variables configured
- [ ] Database initialized
- [ ] API Gateway public domain generated
- [ ] Health checks passing
- [ ] Test API calls successful

---

## 📊 Monitoring

### Railway Dashboard

```bash
# View metrics for each service:
- CPU usage
- Memory usage
- Request count
- Response time
```

### Logs

```bash
# View real-time logs
Service → Deployments → Logs
```

### Health Checks

```bash
# API Gateway
https://your-gateway.up.railway.app/actuator/health

# Each service (internal)
http://service-name.railway.internal:port/actuator/health
```

---

## 🔄 Updating Your App

```bash
# Push to GitHub
git add .
git commit -m "Update feature"
git push origin main

# Railway auto-deploys (takes ~2-5 minutes)
```

---

## 📝 Important Notes

1. **First deployment takes 5-10 minutes** per service
2. **Services sleep after 30 minutes** of inactivity (free tier)
3. **Cold start takes ~10-20 seconds** when service wakes up
4. **Use internal URLs** for service-to-service communication (faster)
5. **Only API Gateway needs public URL**
6. **Database and Redis always stay awake**

---

## 🎯 Next Steps

1. **Custom Domain** (Optional): Settings → Domains → Add Custom Domain
2. **Upgrade to Hobby Plan** ($5/month): No sleep, faster, more resources
3. **Enable Monitoring**: Add application performance monitoring
4. **Setup Backups**: Railway auto-backs up databases

---

**🎉 Congratulations! Your E-Commerce system is now live on Railway.app!**

**Public API URL:** `https://api-gateway-production-xxxx.up.railway.app`

Share this URL to test your deployed application!
