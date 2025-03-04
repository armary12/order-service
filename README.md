## Docker Image
```bash
https://hub.docker.com/r/armary12/order-microservice
```
or
```bash
docker pull armary12/order-microservice
```

# Microservice Architecture Overview


### Customer Service
Manages customer profiles, registrations, authentication, addresses, and preferences.

### Merchant Service
Handles merchant registration, profile management, and restaurant configurations.

### Menu Service
Manages food menus for each merchant (items, categories, descriptions, images) with independent updates.

### Order Service
Processes orders, tracks lifecycle stages (waiting, cooking, delivering, completed, cancelled), and communicates with other services.

### Payment Service
Handles cashless transactions via banking transfers and credit card gateways with secure processing and reconciliation.

### Delivery Service
Manages dispatch, tracking, and assignment of orders to delivery personnel or third-party logistics.

### Notification Service
Sends real-time notifications (email, SMS, push) for order updates and promotions.

### Pricing Service
Manages pricing strategies and dynamic rules (regular, surge, discount) with integration to Menu and Promotion services.

### Promotion Service
Manages promotional campaigns, discount codes, and coupon management for special deals and effectiveness tracking.

### Landing Page Service
Handles the public-facing marketing website and landing pages for merchant highlights, promotions, and general platform info.

# Useful Public cloud services

### Message Queue:
Use a service like AWS SQS to let different parts of your system talk to each other without waiting.

### Serverless Functions:
Use services like AWS Lambda to run small tasks automatically, such as sending notifications or processing data.

### IDP & Session Management:
Use AWS Cognito for easy and secure user logins, and a managed Redis service for keeping track of user sessions.

### Logging & API Gateway:
Use centralized logging tools (like AWS CloudWatch) to watch your system, and an API gateway to manage and secure incoming requests.

---

## Order service Overview
This is an Order Microservice for a Food Delivery Platform developed using Kotlin and Spring Boot. The microservice handles order creation, status management, and provides REST endpoints for order-related operations.

## Technology Stack
- Language: Kotlin
- Framework: Spring Boot
- Database: JPA/Hibernate
- Logging: SLF4J
- Clients: Microservice communication through REST clients

## Features
- Create new food orders
- Manage order statuses
- Search and filter orders
- Validate order creation with multiple checks
- Publish order events via Kafka

## Order Status Flow
The system supports the following order status transitions:
1. PENDING → CONFIRMED or CANCELED
2. CONFIRMED → COOKING or CANCELED
3. COOKING → DELIVERING, COMPLETED, or CANCELED
4. DELIVERING → COMPLETED or CANCELED
5. CANCELED → No further transitions

## API Endpoints

### Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "foodItem": "Pizza",
    "quantity": 2,
    "unitPrice": 10.00,
    "address": "123 Main St",
    "restaurantId": 1
  }'
```

### Get Order by ID
```bash
curl -X GET http://localhost:8080/api/orders/1
```

### Update Order Status
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?status=CONFIRMED"
```

### Search Orders for a Restaurant
```bash
curl -X GET "http://localhost:8080/api/orders/restaurants/1/orders/search?status=PENDING&page=0&size=10"
```
