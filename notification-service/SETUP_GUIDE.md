# Notification Service - Setup & Testing Guide

## 🎉 What We Just Built

The **Notification Service** is now complete! It's a Kafka consumer that:
- ✅ Listens to `order-events` topic
- ✅ Sends multi-channel notifications (Email, SMS, Push - mocked)
- ✅ Implements idempotency to prevent duplicates
- ✅ Uses Strategy Pattern for pluggable notification channels
- ✅ Stores notification history in database
- ✅ Provides REST API for querying notifications

---

## 🏗️ Architecture

```
Order Service (Producer)
    │
    │ publishes OrderEvent
    ▼
┌─────────────────────┐
│  Kafka Topic        │
│  "order-events"     │
└──────────┬──────────┘
           │ consumes
           ▼
┌─────────────────────────┐
│ Notification Service    │
│  • OrderEventConsumer   │
│  • NotificationService  │
│  • Strategy Pattern     │
└──────────┬──────────────┘
           │
     ┌─────┼─────┐
     ▼     ▼     ▼
  Email  SMS  Push
 (mock) (mock) (mock)
     │     │     │
     └─────┼─────┘
           ▼
  notifications.notifications
        (DB table)
```

---

## 🚀 Quick Start

### Step 1: Ensure Infrastructure is Running

```bash
# Start Postgres, Kafka, Redis
docker-compose up -d postgres kafka zookeeper redis

# Verify containers
docker ps

# Check Kafka is ready
docker logs kafka | grep "started (kafka.server.KafkaServer)"
```

### Step 2: Build the Notification Service

```bash
# Build without tests (faster)
./gradlew :notification-service:build -x test

# Or build with tests
./gradlew :notification-service:build
```

### Step 3: Run the Services

**You need 4 terminal windows:**

#### Terminal 1: User Service
```bash
./gradlew :user-service:bootRun
```

#### Terminal 2: Product Service
```bash
./gradlew :product-service:bootRun
```

#### Terminal 3: Order Service
```bash
./gradlew :order-service:bootRun
```

#### Terminal 4: Notification Service (NEW!)
```bash
./gradlew :notification-service:bootRun
```

**Wait for logs:**
```
NotificationServiceApplication : Started NotificationServiceApplication in X seconds
OrderEventConsumer            : partition assigned: [order-events-0]
```

---

## 🧪 Testing the Complete Flow

### Test 1: End-to-End Order Placement with Notifications

#### 1. Register a User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "role": "USER"
  }'
```

#### 2. Login and Save JWT
```bash
JWT=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}' \
  | jq -r '.data.token')

echo "JWT Token: $JWT"
```

#### 3. Create Admin User (SQL)
```bash
docker exec -it postgres psql -U ashish -d ecomdb

INSERT INTO users.users (username, email, password, role, created_at, updated_at)
VALUES (
  'admin',
  'admin@example.com',
  '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
  'ADMIN',
  NOW(),
  NOW()
);

\q
```

#### 4. Login as Admin
```bash
ADMIN_JWT=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin123!"}' \
  | jq -r '.data.token')

echo "Admin JWT: $ADMIN_JWT"
```

#### 5. Create a Product
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "stock": 100,
    "active": true
  }' | jq
```

#### 6. Place an Order (This triggers notifications!)
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2,
    "idempotencyKey": "test-order-001"
  }' | jq
```

#### 7. Check Notification Service Logs

You should see in Terminal 4 (notification-service):

```
📩 Received order event: orderId=1 status=CONFIRMED userEmail=test@example.com
Processing order event: orderId=1 status=CONFIRMED
Sending ORDER_CONFIRMED notifications via channels: [EMAIL, SMS]
📧 Sending email to test@example.com: Your Order #1 is Confirmed!
✅ Email sent successfully to test@example.com
✅ Notification sent: id=1 channel=EMAIL type=ORDER_CONFIRMED
📱 Sending SMS to +1234567890
✅ SMS sent successfully to +1234567890
✅ Notification sent: id=2 channel=SMS type=ORDER_CONFIRMED
✅ Order event processed successfully: orderId=1
```

#### 8. Query Notifications via API

**Get notifications for order:**
```bash
curl "http://localhost:8084/api/notifications/order/1" | jq
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "orderId": 1,
      "userId": 1,
      "channel": "EMAIL",
      "type": "ORDER_CONFIRMED",
      "recipient": "test@example.com",
      "subject": "Your Order #1 is Confirmed!",
      "message": "Great news! Your order #1 has been confirmed...",
      "status": "SENT",
      "sentAt": "2025-01-29T12:30:00Z",
      "retryCount": 0
    },
    {
      "id": 2,
      "orderId": 1,
      "userId": 1,
      "channel": "SMS",
      "type": "ORDER_CONFIRMED",
      "recipient": "+1234567890",
      "status": "SENT",
      "sentAt": "2025-01-29T12:30:01Z"
    }
  ]
}
```

**Get user's notifications (paginated):**
```bash
curl "http://localhost:8084/api/notifications/user/1?page=0&size=10" | jq
```

**Get notification statistics:**
```bash
curl "http://localhost:8084/api/notifications/stats" | jq
```

**Response:**
```json
{
  "success": true,
  "data": {
    "total": 2,
    "sent": 2,
    "failed": 0,
    "pending": 0,
    "successRate": 100.0
  }
}
```

---

## 📊 Verify in Database

```bash
docker exec -it postgres psql -U ashish -d ecomdb
```

```sql
-- View notifications
SELECT 
    id, 
    order_id, 
    channel, 
    type, 
    recipient, 
    status, 
    sent_at,
    created_at
FROM notifications.notifications
ORDER BY created_at DESC;

-- Count by status
SELECT status, COUNT(*) 
FROM notifications.notifications 
GROUP BY status;

-- Count by channel
SELECT channel, COUNT(*) 
FROM notifications.notifications 
GROUP BY channel;
```

---

## 🔄 Test Different Order Statuses

### Update Order Status to SHIPPED
```bash
curl -X PUT http://localhost:8083/api/orders/1/status \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}' | jq
```

**Expected Notifications:** EMAIL + PUSH

**Check logs:**
```
📩 Received order event: orderId=1 status=SHIPPED
Sending ORDER_SHIPPED notifications via channels: [EMAIL, PUSH]
📧 Sending email to test@example.com: Your Order #1 has been Shipped
✅ Email sent successfully
🔔 Sending push notification to user 1
✅ Push notification sent successfully
```

### Update Order Status to DELIVERED
```bash
curl -X PUT http://localhost:8083/api/orders/1/status \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}' | jq
```

**Expected Notifications:** EMAIL only

---

## 🐛 Troubleshooting

### Issue: Notification service not receiving events

**Check 1: Kafka is running**
```bash
docker ps | grep kafka
```

**Check 2: Topic exists**
```bash
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
# Should show: order-events
```

**Check 3: Consumer group is registered**
```bash
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
# Should show: notification-service
```

**Check 4: View consumer group details**
```bash
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service \
  --describe
```

**Check 5: Manually consume from topic**
```bash
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

### Issue: "userEmail" is null in events

**Fix:** Rebuild order-service (we updated OrderEvent.java)

```bash
# Stop order-service (Ctrl+C in Terminal 3)
./gradlew :order-service:clean build -x test
./gradlew :order-service:bootRun
```

### Issue: Database connection failed

**Check:** Postgres is running
```bash
docker-compose logs postgres
```

**Restart if needed:**
```bash
docker-compose restart postgres
```

---

## 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications/order/{orderId}` | Get notifications for specific order |
| GET | `/api/notifications/user/{userId}` | Get user's notifications (paginated) |
| GET | `/api/notifications/failed` | Get all failed notifications (admin) |
| GET | `/api/notifications/stats` | Get notification statistics |
| GET | `/actuator/health` | Health check |
| GET | `/swagger-ui.html` | Interactive API docs |

---

## 🎯 Key Features Implemented

### 1. Kafka Consumer with Manual Acknowledgment
- Prevents message loss
- Only acks after successful processing
- Failed messages will be redelivered

### 2. Idempotency
- Prevents duplicate notifications
- Uses composite key: `order-{orderId}-{status}-{channel}`
- Safe to replay Kafka messages

### 3. Strategy Pattern
- Pluggable notification channels
- Easy to add new channels (e.g., Slack, WhatsApp)
- Each strategy encapsulates channel-specific logic

### 4. Notification History
- All notifications stored in DB
- Audit trail for compliance
- Retry failed notifications

### 5. Multi-Channel Support
- Different channels for different events
- Example: ORDER_CONFIRMED → Email + SMS
- Configurable in `getChannelsForType()` method

---

## 🔧 Customization

### Add New Notification Channel

1. **Create Strategy:**
```java
@Component
public class WhatsAppNotificationStrategy implements NotificationStrategy {
    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WHATSAPP; // Add to enum
    }
    
    @Override
    public void send(Notification notification) {
        // Integrate with WhatsApp Business API
    }
}
```

2. **Update Enum:**
```java
public enum NotificationChannel {
    EMAIL, SMS, PUSH, WHATSAPP
}
```

3. **Add to Channel Mapping:**
```java
case ORDER_PLACED -> List.of(
    NotificationChannel.EMAIL, 
    NotificationChannel.WHATSAPP
);
```

### Customize Notification Templates

Edit `NotificationService.generateMessage()` method to:
- Use HTML templates for emails
- Add personalization (user name, order details)
- Support multiple languages

### Add Retry Logic for Failed Notifications

Create a scheduled job:
```java
@Scheduled(fixedDelay = 300000) // Every 5 minutes
public void retryFailedNotifications() {
    List<Notification> failed = repository.findByStatus(FAILED);
    failed.forEach(this::retryNotification);
}
```

---

## 📊 Monitoring

### View Consumer Lag
```bash
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service \
  --describe
```

### Check Notification Success Rate
```bash
curl "http://localhost:8084/api/notifications/stats" | jq '.data.successRate'
```

### View Failed Notifications
```bash
curl "http://localhost:8084/api/notifications/failed" | jq
```

---

## 🎉 Success Criteria

- [x] Notification service starts successfully
- [x] Consumes events from Kafka
- [x] Sends notifications (mocked)
- [x] Stores notification history
- [x] REST API returns data
- [x] Idempotency works (duplicate events ignored)
- [x] Multi-channel notifications sent

---

## 🚀 Next Steps

1. ✅ **Complete:** Notification Service
2. 🚧 **Next:** Cart Service (Redis-backed shopping cart)
3. 📅 **Future:** Payment Service
4. 📅 **Future:** API Gateway

---

## 📞 Quick Commands Cheat Sheet

```bash
# Start infrastructure
docker-compose up -d postgres kafka zookeeper redis

# Build notification service
./gradlew :notification-service:build -x test

# Run notification service
./gradlew :notification-service:bootRun

# Check health
curl http://localhost:8084/actuator/health

# View Swagger UI
open http://localhost:8084/swagger-ui.html

# Consume Kafka events (manual check)
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning

# Database
docker exec -it postgres psql -U ashish -d ecomdb
\dt notifications.*
SELECT * FROM notifications.notifications ORDER BY created_at DESC;
```

---

**Congratulations! 🎉** You've successfully implemented a production-grade notification service with event-driven architecture, idempotency, and the Strategy pattern!

**Created:** 2025-01-29  
**Status:** ✅ COMPLETE
