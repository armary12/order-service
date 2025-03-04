package com.ttbspark.order.exception

import org.apache.coyote.BadRequestException

/**
 * Thrown when a requested status transition is invalid or disallowed.
 */
class InvalidOrderStatusException(message: String) : BadRequestException(message)

/**
 * Thrown when an order is attempted while the restaurant is not open for service.
 */
class RestaurantClosedException(message: String) : IllegalStateException(message)

/**
 * Thrown when an status change is attempted from PENDING to CONFIRMED
 *
 */
class PaymentNotCompletedException(message: String) : IllegalStateException(message)
