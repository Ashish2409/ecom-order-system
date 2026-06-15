# 🚀 Railway Deployment - Quick Reference

## 📋 Files Created

1. ✅ `railway.json` - Railway project configuration
2. ✅ `api-gateway/railway.toml` - API Gateway Railway config
3. ✅ `user-service/railway.toml` - User Service Railway config
4. ✅ `product-service/railway.toml` - Product Service Railway config
5. ✅ `order-service/railway.toml` - Order Service Railway config
6. ✅ `notification-service/railway.toml` - Notification Service Railway config
7. ✅ `.env.railway` - Environment variables template
8. ✅ `api-gateway/src/main/resources/application-production.yml` - Production config
9. ✅ `RAILWAY_DEPLOYMENT.md` - Complete deployment guide
10. ✅ `deploy-railway.sh` - Automated deployment script

---

## 🎯 Quick Start (3 Options)

### Option 1: Manual Deployment (Recommended for First Time)

**Follow:** `RAILWAY_DEPLOYMENT.md`

**Steps:**
1. Sign up at https://railway.app
2. Sign up at https://upstash.com (for Kafka)
3. Follow the detailed guide step-by-step

**Time:** ~30 minutes

---

### Option 2: Railway CLI (Automated)

```bash
# 1. Make script executable
chmod +x deploy-railway.sh

# 2. Run deployment script
./deploy-railway.sh

# 3. Follow prompts
```

**Time:** ~15 minutes

---

### Option 3: GitHub Integration (Easiest)

```bash
# 1. Push code to GitHub
git add .
git commit -m "Add Railway deployment configs"
git push origin main

# 2. Go to Railway.app
# 3. New Project → Deploy from GitHub
# 4. Select: ecom-order-system
# 5. Railway auto-detects all services
# 6. Configure environment variables (use .env.railway as reference)
```

**Time:** ~20 minutes

---

## 🔑 Required Accounts

### 1. Railway.app (Free)
- URL: https://railway.app
- Plan: Free ($5/month credit)
- What: Hosting platform

### 2. Upstash (Free)
- URL: https://upstash.com
- Plan: Free tier
- What: Managed Kafka service

---

## 💰 Cost Breakdown

### Railway Free Tier:
- ✅ $5/month credit
- ✅ 500 hours/month per service
- ✅ Services sleep after 30 min inactivity
- ✅ PostgreSQL included
- ✅ Redis included

### Upstash Free Tier:
- ✅ 10,000 messages/day
- ✅ 100 MB storage
- ✅ 1 day retention

**Total Cost: $0/month** (within free tiers)

---

## 📊 What You Get After Deployment

### Public URLs:
```
API Gateway: https://api-gateway-production-xxxx.up.railway.app
```

### Internal URLs (service-to-service):
```
User Service: http://user-service.railway.internal:8081
Product Service: http://product-service.railway.internal:8082
Order Service: http://order-service.railway.internal:8083
Notification Service: http://notification-service.railway.internal:8084
```

### Databases:
```
PostgreSQL: Provided by Railway
Redis: Provided by Railway
Kafka: Provided by Upstash
```

---

## 🧪 Testing Your Deployment

```bash
# Replace with your actual Railway URL
export API_URL="https://api-gateway-production-xxxx.up.railway.app"

# Health check
curl $API_URL/actuator/health

# Register user
curl -X POST $API_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "phone": "1234567890"
  }'

# Login
curl -X POST $API_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Get products
curl $API_URL/api/products
```

---

## 🔧 Environment Variables Required

### All Services:
```bash
SPRING_PROFILES_ACTIVE=production
JWT_SECRET=yourSecretKey32CharsMin
```

### Database (Railway auto-provides):
```bash
DB_URL=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}
DB_USER=${PGUSER}
DB_PASSWORD=${PGPASSWORD}
```

### Redis (Railway auto-provides):
```bash
REDIS_HOST=${REDIS_HOST}
REDIS_PORT=${REDIS_PORT}
```

### Kafka (from Upstash):
```bash
KAFKA_SERVERS=your-kafka-url:9092
KAFKA_USERNAME=your-username
KAFKA_PASSWORD=your-password
```

**Complete list in:** `.env.railway`

---

## 📝 Deployment Checklist

### Before Deployment:
- [ ] Commit all changes to GitHub
- [ ] Create Railway account
- [ ] Create Upstash account
- [ ] Review `.env.railway` file

### During Deployment:
- [ ] Create Railway project
- [ ] Add PostgreSQL database
- [ ] Add Redis
- [ ] Create Upstash Kafka cluster
- [ ] Deploy all 5 services
- [ ] Set environment variables
- [ ] Generate public domain for API Gateway

### After Deployment:
- [ ] Check all service logs
- [ ] Run health checks
- [ ] Test API endpoints
- [ ] Initialize database (run init.sql)
- [ ] Test order creation (Kafka flow)

---

## 🆘 Common Issues

### Build Fails
```bash
# Check Dockerfile path in railway.toml
dockerfilePath = "service-name/Dockerfile"

# Verify context is project root
context = "."
```

### Database Connection Failed
```bash
# Use Railway's variable references
DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
```

### Service Timeout
```bash
# First build takes 5-10 minutes
# Be patient

# If stuck, redeploy:
Settings → Deployment → Redeploy
```

### Kafka Connection Failed
```bash
# Double-check Upstash credentials
# Copy exact values from Upstash console
```

---

## 🎓 Learning Resources

### Railway Documentation:
- https://docs.railway.app

### Upstash Documentation:
- https://docs.upstash.com/kafka

### Railway CLI:
- https://docs.railway.app/develop/cli

---

## 🔄 Auto-Deployment

After initial setup, Railway auto-deploys on every push to main:

```bash
git add .
git commit -m "Update feature"
git push origin main

# Railway automatically:
# 1. Detects changes
# 2. Builds Docker images
# 3. Deploys new version
# 4. Zero downtime (rolling update)
```

---

## 📊 Monitoring

### Railway Dashboard:
- View logs
- Check metrics (CPU, Memory)
- Monitor deployments
- Track costs

### Health Endpoints:
```bash
# API Gateway
https://your-gateway.up.railway.app/actuator/health

# Metrics
https://your-gateway.up.railway.app/actuator/metrics
```

---

## 🎉 Success!

After following this guide, you'll have:

✅ All 5 microservices deployed
✅ PostgreSQL database running
✅ Redis cache running
✅ Kafka messaging running
✅ API Gateway publicly accessible
✅ Auto-deployment enabled
✅ Monitoring setup
✅ $0 monthly cost (free tier)

---

## 📞 Support

- Railway Discord: https://discord.gg/railway
- Upstash Discord: https://discord.gg/upstash
- GitHub Issues: https://github.com/Ashish2409/ecom-order-system/issues

---

**Ready to deploy? Start with:** `RAILWAY_DEPLOYMENT.md`

**Questions? Check the full guide or reach out for help!**
