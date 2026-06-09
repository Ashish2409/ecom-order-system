# E-Commerce Order Management System

## 🏗️ Microservices Architecture

A production-grade e-commerce system built with Spring Boot microservices, featuring API Gateway, Circuit Breaker, Rate Limiting, and Event-Driven Architecture.

## 🎯 System Architecture

```
                    ┌─────────────────┐
                    │  API Gateway    │ :8080
                    │  (Entry Point)  │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
   ┌────▼─────┐        ┌────▼─────┐        ┌────▼─────┐
   │   User   │        │ Product  │        │  Order   │
   │ Service  │        │ Service  │        │ Service  │
   │  :8081   │        │  :8082   │        │  :8083   │
   └──────────┘        └──────────┘        └────┬─────┘
                                                 │
                                                 │ Kafka
                                                 │
                                           ┌─────▼──────┐
                                           │Notification│
                                           │  Service   │
                                           │   :8084    │
                                           └────────────┘
```

## 📦 Services

### 1. **API Gateway** (:8080)
- Single entry point for all services
- JWT authentication & authorization
- Circuit breaker (Resilience4j)
- Rate limiting (Redis-based)
- Request tracking (Correlation ID)
- CORS configuration

### 2. **User Service** (:8081)
- User registration & authentication
- JWT token generation
- Password encryption (BCrypt)
- Role-based access control

### 3. **Product Service** (:8082)
- Product catalog management
- Stock management
- Category management
- Search & pagination

### 4. **Order Service** (:8083)
- Order creation & management
- Order status tracking
- Product validation
- Stock reservation
- Event publishing to Kafka

### 5. **Notification Service** (:8084)
- Event-driven notifications
- Kafka consumer
- Multi-channel support (Email, SMS, Push)
- Notification history

## 🛠️ Technology Stack

| Category | Technologies |
|----------|-------------|
| **Backend** | Spring Boot 3.2.0, Java 17 |
| **API Gateway** | Spring Cloud Gateway |
| **Database** | PostgreSQL 15 |
| **Caching** | Redis 7 |
| **Message Broker** | Apache Kafka |
| **Security** | Spring Security, JWT (JJWT 0.12.5) |
| **Resilience** | Resilience4j (Circuit Breaker) |
| **Documentation** | OpenAPI 3.0 (Swagger) |
| **Build** | Gradle 8.14 |
| **Containerization** | Docker, Docker Compose |
| **Monitoring** | Spring Actuator, Prometheus |

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker Desktop
- PowerShell (Windows)

### 1. Clone Repository
```bash
git clone https://github.com/Ashish2409/ecom-order-system.git
cd ecom-order-system
```

### 2. Start Complete System
```powershell
.\start-all.ps1
```

This will:
- Start infrastructure (Postgres, Redis, Kafka)
- Start all 4 microservices
- Start API Gateway
- Verify all services are UP

### 3. Check System Status
```powershell
.\check-services.ps1
```

### 4. Test API Gateway
```powershell
.\test-gateway-complete.ps1
```

### 5. Stop System
```powershell
.\stop-all.ps1
```

## 📚 API Documentation

### Via API Gateway (Recommended)
- **API Gateway Swagger**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Routes Info**: http://localhost:8080/actuator/gateway/routes

### Direct Service Access
| Service | Swagger UI |
|---------|------------|
| User Service | http://localhost:8081/swagger-ui.html |
| Product Service | http://localhost:8082/swagger-ui.html |
| Order Service | http://localhost:8083/swagger-ui.html |
| Notification Service | http://localhost:8084/swagger-ui.html |

## 🔐 Authentication

### Register User
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "1234567890"
}
```

### Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Use JWT Token
```bash
GET http://localhost:8080/api/orders/me
Authorization: Bearer <your-jwt-token>
```

## 🧪 Testing

### Manual Testing
Use the provided PowerShell script:
```powershell
.\test-gateway-complete.ps1
```

### API Testing
- Import Swagger collections into Postman
- Use Swagger UI for interactive testing

## 📊 Key Features

### API Gateway Features
- ✅ **Authentication**: JWT validation at gateway level
- ✅ **Circuit Breaker**: Automatic failure handling
- ✅ **Rate Limiting**: 20-100 requests/min based on endpoint
- ✅ **Request Tracking**: Correlation IDs for distributed tracing
- ✅ **Logging**: Request/response logging with timing
- ✅ **CORS**: Cross-origin support for frontends
- ✅ **Fallback**: User-friendly error responses

### Microservices Features
- ✅ **Event-Driven**: Kafka integration for async communication
- ✅ **Database Per Service**: Independent databases
- ✅ **API Documentation**: OpenAPI 3.0 / Swagger
- ✅ **Health Checks**: Spring Actuator endpoints
- ✅ **Docker Ready**: Multi-stage Dockerfiles
- ✅ **Security**: JWT-based authentication
- ✅ **Validation**: Request/Response validation
- ✅ **Exception Handling**: Global error handling

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Products Table
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Notifications Table
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔧 Configuration

### Environment Variables

Create `.env` file in project root:
```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/ecomdb
DB_USER=ashish
DB_PASSWORD=password123

# JWT
JWT_SECRET=yourSecretKeyMustBeAtLeast32CharactersLong!

# Kafka
KAFKA_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Service URLs (for Gateway)
USER_SERVICE_URL=http://localhost:8081
PRODUCT_SERVICE_URL=http://localhost:8082
ORDER_SERVICE_URL=http://localhost:8083
NOTIFICATION_SERVICE_URL=http://localhost:8084
```

## 📁 Project Structure

```
ecom-order-system/
├── api-gateway/              # API Gateway service
├── user-service/             # User & Auth service
├── product-service/          # Product catalog service
├── order-service/            # Order management service
├── notification-service/     # Notification service
├── .github/workflows/        # CI/CD pipelines
├── docker-compose.yml        # Docker Compose configuration
├── init.sql                  # Database initialization
├── start-all.ps1            # Start all services script
├── stop-all.ps1             # Stop all services script
├── check-services.ps1       # Check service status script
├── test-gateway-complete.ps1 # Test API Gateway script
└── README.md
```

## 🐳 Docker Deployment

### Build All Services
```bash
docker-compose build
```

### Build Without Cache (After Git Merge)
```bash
# If you get "not found" errors after merging branches
docker-compose build --no-cache
```

### Run All Services
```bash
docker-compose up -d
```

### Build and Run
```bash
docker-compose up --build -d
```

### View Logs
```bash
docker-compose logs -f api-gateway
```

### Stop All Services
```bash
docker-compose down
```

## 🔍 Monitoring

### Health Endpoints
- Gateway: http://localhost:8080/actuator/health
- User Service: http://localhost:8081/actuator/health
- Product Service: http://localhost:8082/actuator/health
- Order Service: http://localhost:8083/actuator/health
- Notification Service: http://localhost:8084/actuator/health

### Metrics
- Prometheus: http://localhost:8080/actuator/prometheus
- Metrics: http://localhost:8080/actuator/metrics

## ⚠️ Troubleshooting

### Port Already in Use
```powershell
# Check what's using the port
Get-NetTCPConnection -LocalPort 8080

# Stop all services
.\stop-all.ps1
```

### Services Won't Start
1. Ensure Docker is running
2. Check logs: `docker-compose logs`
3. Verify database is up: `docker ps`
4. Check application.yml configurations

### Gateway Can't Reach Services
- Ensure all services are UP: `.\check-services.ps1`
- Verify service URLs in gateway configuration
- Check if services are running on correct ports

## 📝 Development

### Running Services Locally

#### Option 1: Using Scripts (Recommended)
```powershell
.\start-all.ps1
```

#### Option 2: Individual Service
```bash
cd user-service
../gradlew bootRun
```

#### Option 3: IntelliJ IDEA
1. Open project in IntelliJ
2. Right-click service module
3. Run 'ServiceApplication'

### Building Services
```bash
# Build all
./gradlew build

# Build specific service
./gradlew :user-service:build
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👤 Author

**Ashish Biradar**
- GitHub: [@Ashish2409](https://github.com/Ashish2409)

## 🙏 Acknowledgments

- Spring Boot Team
- Spring Cloud Team
- Open Source Community

---

**Built with ❤️ using Spring Boot & Microservices Architecture**
