# 📄 SOFTWARE REQUIREMENT SPECIFICATION (SRS)

## 📌 Project: Real-time E-commerce Analytics System

---

# 1. INTRODUCTION

## 1.1 Purpose

Tài liệu này mô tả chi tiết yêu cầu cho hệ thống **Real-time E-commerce Analytics**, nhằm:

- Thu thập và xử lý hành vi người dùng theo thời gian thực
- Phân tích dữ liệu phục vụ business (trending, conversion rate)
- Hỗ trợ recommendation và dashboard

---

## 1.2 Scope

Hệ thống cung cấp:

- Thu thập event user (view, click, add-to-cart, purchase)
- Xử lý event real-time bằng Kafka
- Phân tích và lưu trữ dữ liệu
- Cung cấp API cho dashboard & recommendation

**Không bao gồm:**

- Frontend phức tạp (chỉ cần demo)
- Payment gateway thực tế

---

## 1.3 Definitions

| Term     | Meaning                       |
|----------|-------------------------------|
| Event    | Hành vi user (view, click...) |
| Producer | Service gửi event vào Kafka   |
| Consumer | Service xử lý event           |
| Topic    | Queue trong Kafka             |
| Cache    | Redis lưu dữ liệu nhanh       |

---

## 1.4 Overview

Tài liệu gồm:

- Functional requirements
- Non-functional requirements
- System architecture
- Data design
- API design

---

# 2. OVERALL DESCRIPTION

## 2.1 Product Perspective

Hệ thống là **distributed microservices system** gồm:

- API Gateway
- Config Server
- Discovery Server
- Auth Service
- Event Service
- Analytics Service
- Product Service
- Recommendation Service

---

## 2.2 Product Functions

**Core functions:**
- Ghi nhận event user
- Streaming dữ liệu
- Phân tích real-time
- Caching
- Authentication

---

## 2.3 User Classes

| User     | Description                    |
|----------|--------------------------------|
| End User | Người dùng hệ thống e-commerce |
| Admin    | Xem dashboard analytics        |
| System   | Internal services              |

---

## 2.4 Constraints

- Phải dùng:
   - Kafka
   - Redis
   - MongoDB
- Phải scale horizontal
- Latency thấp (<200ms API)

---

## 2.5 Assumptions

- Traffic lớn (1000+ events/sec)
- Dữ liệu có thể eventual consistency
- Không yêu cầu ACID strict

---

# 3. SYSTEM ARCHITECTURE

## 3.1 High-level Architecture

```
Client → API Gateway → Services  
Kafka → Event Streaming  
Redis → Cache  
MongoDB → Storage  
```

---

## 3.2 Microservices

| Service                | Responsibility            |
|------------------------|---------------------------|
| Auth Service           | OAuth2, JWT               |
| Event Service          | Receive & publish events  |
| Analytics Service      | Process event             |
| Product Service        | Product data              |
| Recommendation Service | Suggest products          |

---

# 4. FUNCTIONAL REQUIREMENTS

## 4.1 Authentication (Auth Service)

FR-1: User Login
- Login bằng Google OAuth2
- Output: JWT token

FR-2: Token Validation
- Validate JWT cho mọi request

---

## 4.2 Event Tracking (Event Service)

FR-3: Send Event
- API nhận event từ client
```json
{
  "userId": "string",
  "eventType": "VIEW_PRODUCT",
  "productId": "string",
  "timestamp": "long"
}
```

FR-4: Public Event
- Event phải được gửi vào Kafka topic `user-events`

---

## 4.3 Event Processing (Analytics Service)

FR-5: Consume Event
- Đọc từ Kafka

FR-6: Update Metrics
- Update:
   - View count
   - Add-to-cart count
   - Purchase count

---

## 4.4 Product Service

FR-7: CRUD Product
- Create/Update/Delete/Get product

FR-8: Cache Product
- Redis cache product detail

---

## 4.5 Analytics API

FR-9: Get Trending Products
- Return top N products theo view/purchase

FR-10: Get Conversion Rate
- conversion = purchase / view

---

## 4.6 Recommendation Service

FR-11: Recommend Products
- Input: productId
- Output: list product liên quan

---

# 5. NON-FUNCTIONAL REQUIREMENTS

## 5.1 Performance

- API response < 200ms
- Kafka throughput: > 10k events/sec

---

## 5.2 Scalability

- Horizontal scaling:
   - Kafka partition
   - Stateless services

---

## 5.3 Availability

- 99.9% uptime
- Retry mechanism

---

## 5.4 Reliability

- Không mất dữ liệu:
   - Kafka partition
   - At-lease-once delivery

---

## 5.5 Security

- OAuth2 + JWT
- HTTPS
- Role-based access

---

## 5.6 Maintainability

- Clean code
- Modular service
- Logging đầy đủ

---

## 5.7 Observability

- Log: requestId
- Metric:
   - Kafka lag
   - API latency

---

# 6. DATA DESIGN

## 6.1 MongoDB Collections

**Events**
```json
{
  "_id": "ObjectId",
  "userId": "string",
  "eventType": "string",
  "productId": "string",
  "timestamp": "long"
}
```
👉 Index:
- productId
- timestamp (TTL index)

**Products**
```json
{
  "_id": "string",
  "name": "string",
  "price": "number",
  "category": "string"
}
```

---

## 6.2 Redis

| Key          | Value        |
|--------------|--------------|
| product:{id} | product data |
| trending     | sorted set   |

---

# 7. API DESIGN

## 7.1 Auth

`POST` /auth/login
Response:
```json
{
  "accessToken": "jwt"
}
```

---

## 7.2 Event

`POST` /events
- Push event

---

## 7.3 Product

`GET` /products/{id}

---

## 7.4 Analytics

`GET` /analytics/trending
Response:
```json
[
  { "productId": "p1", "score": 100 }
]
```

---

## 7.5 Recommendation

`GET` /recommend/{productId}

---

# 8. SEQUENCE FLOW

## 8.1 Event Flow

`Client → Event Service → Kafka → Analytics Service → Redis + MongoDB`

---

## 8.2 Read Flow

`Client → API → Redis → (fallback MongoDB)`

---

# 9. ERROR HANDLING

## 9.1 Kafka Failure

- Retry
- Dead Letter Queue

---

## 9.2 Redis Down

- Fallback MongoDB

---

## 9.3 Invalid Token

- `401` Unauthorized

---

# 10. DEPLOYMENT

## 10.1 Docker

- Mỗi service 1 container

---

## 10.2 Docker Compose

- Kafka
- Zookeeper
- Redis
- MongoDB

---

## 10.3 Scaling
- Scale service bằng replica

---

# 11. TESTING

## 11.1 Unit Test

- Service logic

---

## 11.2 Integration Test

- Kafka flow

---

## 11.3 Load Test

- 1000 events/sec

---

# 12. FUTURE IMPROVEMENTS

- Machine Learning recommendation
- Real-time dashboard (WebSocket)
- Kubernetes deployment
- Multi-region scaling