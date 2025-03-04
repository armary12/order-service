# Food Delivery Platform - Order Microservice

## Docker Image
```bash

docker pull armary12/order-microservice
```

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

## Microservice Validations
- Restaurant open status check
- Menu item availability verification
- Quantity limits (1-100 items)
- Price validation
- Payment completion verification

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

## Error Handling
The microservice handles various exceptions:
- `RestaurantClosedException`
- `InvalidOrderStatusException`
- `PaymentNotCompletedException`
- Validation exceptions for order creation

## Configuration Recommendations
- Use environment variables for configuration
- Implement circuit breakers for external service calls
- Configure robust logging
- Implement comprehensive error tracking

