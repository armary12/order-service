package com.ttbspark.order.exception

/**
 * Thrown when a requested status transition is invalid or disallowed.
 */
class InvalidOrderStatusException(message: String) : RuntimeException(message)

/**
 * Thrown when an order is attempted while the restaurant is not open for service.
 */
class RestaurantClosedException(message: String) : RuntimeException(message)

/**
 * Thrown when an status change is attempted from PENDING to CONFIRMED
 *
 */
class PaymentNotCompletedException(message: String) : RuntimeException(message)
