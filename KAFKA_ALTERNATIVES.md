# 🔄 Kafka Hosting Alternatives (FREE Options)

Since Upstash no longer supports Kafka, here are the best FREE alternatives for your project.

---

## ⭐ OPTION 1: CloudKarafka (RECOMMENDED)

### Why CloudKarafka?
- ✅ **FREE Forever** (Developer Duck plan)
- ✅ **5 MB storage** + **10 MB/day** throughput
- ✅ **Production-ready**
- ✅ **High availability** (3 brokers)
- ✅ **No credit card required**
- ✅ **Perfect for microservices**

### Setup Steps:

#### 1. Create Account
```
1. Go to: https://www.cloudkarafka.com
2. Click "Sign Up" (free)
3. Verify email
```

#### 2. Create Instance
```
1. Dashboard → "Create New Instance"
2. Name: ecom-kafka
3. Plan: "Developer Duck" (FREE)
4. Region: Choose closest to you
5. Click "Create Instance"
```

#### 3. Get Connection Details
```
Dashboard → Your Instance → Details

Copy these:
- Brokers: ark-01.srvs.cloudkarafka.com:9094,ark-02.srvs.cloudkarafka.com:9094
- Username: xyz12345
- Password: (your password)
- Topic Prefix: xyz12345-
```

#### 4. Create Topic
```
Dashboard → Topics → Create Topic

Topic Name: xyz12345-order-events
(Must include your prefix!)

Partitions: 1
Retention: 7 days
```

### Configuration for Your Project:

**Update `application.yml` for order-service and notification-service:**

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:ark-01.srvs.cloudkarafka.com:9094,ark-02.srvs.cloudkarafka.com:9094,ark-03.srvs.cloudkarafka.com:9094}
    properties:
      security.protocol: SASL_SSL
      sasl.mechanism: SCRAM-SHA-256
      sasl.jaas.config: org.apache.kafka.common.security.scram.ScramLoginModule required username="${KAFKA_USERNAME}" password="${KAFKA_PASSWORD}";
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
      group-id: ${KAFKA_TOPIC_PREFIX:}ecom-notification-group

# Topic name with prefix
kafka:
  topic:
    order-events: ${KAFKA_TOPIC_PREFIX:}order-events
```

**Environment Variables for Railway:**
```bash
KAFKA_SERVERS=ark-01.srvs.cloudkarafka.com:9094,ark-02.srvs.cloudkarafka.com:9094,ark-03.srvs.cloudkarafka.com:9094
KAFKA_USERNAME=xyz12345
KAFKA_PASSWORD=your-password
KAFKA_TOPIC_PREFIX=xyz12345-
```

---

## OPTION 2: Aiven (Free Trial - 30 Days)

### Why Aiven?
- ✅ **Great features**
- ✅ **Easy to use**
- ⚠️ **Trial expires in 30 days** (then $10/month)
- ⚠️ **Credit card required**

### Setup Steps:

#### 1. Create Account
```
1. Go to: https://aiven.io
2. Sign up (Credit card required)
3. Choose "Free trial"
```

#### 2. Create Kafka Service
```
1. Dashboard → "Create Service"
2. Select: Apache Kafka
3. Plan: "Startup-2" (Free trial)
4. Region: Choose closest
5. Name: ecom-kafka
6. Create
```

#### 3. Get Connection Details
```
Service → Overview

Copy:
- Service URI
- Access Key
- Access Certificate
- CA Certificate
```

#### 4. Create Topic
```
Service → Topics → Create Topic

Name: order-events
Partitions: 1
Replication: 1
```

**Note:** After 30 days, you'll need to upgrade or migrate to another service.

---

## OPTION 3: Confluent Cloud (Free Tier)

### Why Confluent?
- ✅ **$400 free credit** (trial)
- ✅ **Professional features**
- ⚠️ **Credit card required**
- ⚠️ **Credits expire in 3 months**

### Setup Steps:

```
1. Go to: https://confluent.cloud
2. Sign up (Credit card required)
3. Create cluster (Basic plan)
4. Get API keys
5. Create topic: order-events
```

**Note:** After credits expire, minimal usage costs ~$1-5/month.

---

## OPTION 4: Run Kafka on Railway

### Why Self-Host on Railway?
- ✅ **Full control**
- ✅ **No external dependencies**
- ⚠️ **Uses your Railway $5 credit**
- ⚠️ **~200MB RAM usage**

### Setup Steps:

#### 1. Deploy Kafka on Railway

```bash
1. Railway Dashboard → New → Empty Service
2. Service Name: kafka
3. Deploy from Docker image
4. Image: bitnami/kafka:latest
```

#### 2. Set Environment Variables

```bash
# KRaft mode (no Zookeeper needed)
KAFKA_CFG_NODE_ID=0
KAFKA_CFG_PROCESS_ROLES=controller,broker
KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093
KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka.railway.internal:9092
KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@localhost:9093
KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
ALLOW_PLAINTEXT_LISTENER=yes
```

#### 3. Configure Your Services

**Environment Variables for Order/Notification Services:**
```bash
KAFKA_SERVERS=kafka.railway.internal:9092
# No username/password needed for internal Railway network
```

**Update `application.yml`:**
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:kafka.railway.internal:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      group-id: ecom-notification-group

kafka:
  topic:
    order-events: order-events
```

### Cost Estimate:
- Kafka service: ~$1.50/month (from $5 credit)
- Leaves ~$3.50 for other services

---

## OPTION 5: Redpanda Cloud (Free Tier)

### Why Redpanda?
- ✅ **Kafka-compatible**
- ✅ **Better performance**
- ✅ **Free tier available**
- ⚠️ **Beta/New product**

### Setup Steps:

```
1. Go to: https://redpanda.com/try-redpanda
2. Sign up for free tier
3. Create cluster
4. Get connection details
5. Use same Kafka client configs
```

---

## 📊 COMPARISON TABLE

| Service | Cost | Setup Time | Reliability | Recommendation |
|---------|------|------------|-------------|----------------|
| **CloudKarafka** | FREE Forever | 5 min | ⭐⭐⭐⭐⭐ | ✅ **BEST CHOICE** |
| Aiven | FREE 30 days | 10 min | ⭐⭐⭐⭐⭐ | Good for testing |
| Confluent Cloud | $400 credit | 15 min | ⭐⭐⭐⭐⭐ | Professional projects |
| Railway Self-Host | ~$1.50/month | 20 min | ⭐⭐⭐⭐ | If you want control |
| Redpanda | FREE | 10 min | ⭐⭐⭐ | Experimental |

---

## ⭐ RECOMMENDED SETUP

### For Your Project: Use CloudKarafka

**Reasons:**
1. ✅ **FREE forever** (no expiration)
2. ✅ **No credit card required**
3. ✅ **Perfect for microservices** (5MB storage is enough for order events)
4. ✅ **Production-ready** (3 Kafka brokers)
5. ✅ **Easy setup** (5 minutes)
6. ✅ **High availability**

### Quick Start with CloudKarafka:

```bash
1. Sign up: https://www.cloudkarafka.com
2. Create instance (Developer Duck - FREE)
3. Get credentials
4. Update your Railway environment variables:
   - KAFKA_SERVERS=ark-01.srvs.cloudkarafka.com:9094,...
   - KAFKA_USERNAME=xyz12345
   - KAFKA_PASSWORD=your-password
   - KAFKA_TOPIC_PREFIX=xyz12345-
5. Redeploy order-service and notification-service
```

**Done! Your Kafka messaging is now running on CloudKarafka!**

---

## 🔧 Code Changes Required

### Update Order Service

**File:** `order-service/src/main/resources/application.yml`

Add topic prefix support:
```yaml
kafka:
  topic:
    order-events: ${KAFKA_TOPIC_PREFIX:}order-events
```

### Update Notification Service

**File:** `notification-service/src/main/resources/application.yml`

Add topic prefix and consumer group:
```yaml
spring:
  kafka:
    consumer:
      group-id: ${KAFKA_TOPIC_PREFIX:}ecom-notification-group

kafka:
  topic:
    order-events: ${KAFKA_TOPIC_PREFIX:}order-events
```

### Update `.env.railway`

Replace Upstash config with CloudKarafka:
```bash
# Kafka (CloudKarafka)
KAFKA_SERVERS=ark-01.srvs.cloudkarafka.com:9094,ark-02.srvs.cloudkarafka.com:9094,ark-03.srvs.cloudkarafka.com:9094
KAFKA_USERNAME=xyz12345
KAFKA_PASSWORD=your-cloudkarafka-password
KAFKA_TOPIC_PREFIX=xyz12345-
```

---

## 🧪 Testing Kafka

### Test Locally:

```bash
# 1. Update docker-compose.yml to use CloudKarafka
# 2. Set environment variables
export KAFKA_SERVERS=ark-01.srvs.cloudkarafka.com:9094,...
export KAFKA_USERNAME=xyz12345
export KAFKA_PASSWORD=your-password
export KAFKA_TOPIC_PREFIX=xyz12345-

# 3. Start services
./gradlew :order-service:bootRun
./gradlew :notification-service:bootRun

# 4. Place an order (order-service will produce event)
curl -X POST http://localhost:8083/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'

# 5. Check notification-service logs
# Should see: "Received order event: ..."
```

### Test on Railway:

```bash
# After deploying to Railway with CloudKarafka env vars:

# 1. Check CloudKarafka dashboard
# Messages tab should show producer/consumer activity

# 2. Check Railway logs
Railway Dashboard → notification-service → Logs
# Should see Kafka consumer messages
```

---

## 💡 Pro Tips

1. **Topic Names**: Always use prefix (e.g., `xyz12345-order-events`)
2. **Consumer Groups**: Use prefix for groups too (e.g., `xyz12345-ecom-group`)
3. **Connection Pooling**: CloudKarafka free tier limits connections to 5
4. **Monitoring**: Check CloudKarafka dashboard for message throughput
5. **Retention**: Free tier has 7-day retention (enough for most apps)

---

## 🆘 Troubleshooting

### Connection Refused
```
Error: Connection refused to ark-01.srvs.cloudkarafka.com:9094

Solution:
- Check if SASL_SSL is configured
- Verify username/password are correct
- Ensure security.protocol is set
```

### Topic Not Found
```
Error: Topic 'order-events' not found

Solution:
- Topic must include prefix: 'xyz12345-order-events'
- Set KAFKA_TOPIC_PREFIX environment variable
- Pre-create topic in CloudKarafka dashboard
```

### Authentication Failed
```
Error: Authentication failed

Solution:
- Copy exact username from CloudKarafka dashboard
- Copy exact password
- Username format: xyz12345 (your instance ID)
```

---

## 📝 Summary

**Best Choice:** ⭐ **CloudKarafka (Free Forever)**

**Setup Time:** 5-10 minutes

**Steps:**
1. Sign up at CloudKarafka
2. Create instance (Developer Duck)
3. Get credentials
4. Update environment variables
5. Deploy!

**Cost:** $0/month forever ✅

---

**Ready to switch? Follow the CloudKarafka setup above!**
