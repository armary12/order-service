# Food Delivery Platform - Order Microservice

## Docker Image
```bash

docker pull armary12/order-microservice
```

# Microservice Design

---

## Customer Service
Manages customer profiles, registrations, authentication, addresses, and preferences.

---

## Merchant Service
Handles merchant registration, profile management, and basic restaurant configurations.

---

## Menu Service
Manages food menus for each merchant, including items, categories, descriptions, and images. This service allows merchants to update their menus independently of other services.

---

## Order Service
Processes order creation and management. It tracks the order lifecycle (e.g., waiting for confirmation, cooking, delivering, completed, cancelled) and communicates with other microservices as needed.

---

## Payment Service
Handles cashless transactions by integrating with both banking transfers and credit card gateways. It ensures secure payment processing and reconciliation.

---

## Delivery Service
Manages dispatch, tracking, and assignment of orders to delivery personnel or third-party logistics.

---

## Notification Service
Sends real-time notifications (via email, SMS, or push notifications) to customers, merchants, and delivery partners about order updates and promotions.

---

## Pricing Service
Manages pricing strategies and dynamic pricing rules for food items, including regular prices and potential surge or discount pricing. This service may integrate with both the Menu and Promotion services.

---

## Promotion Service
Manages promotional campaigns, discount codes, and coupon management. It allows the platform to offer special deals and track promotional effectiveness.

---

## Landing Page Service
Handles the public-facing marketing website and landing pages. This service is responsible for presenting merchant highlights, promotional content, and general information about the platform to potential customers.

---

## Project Overview
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
