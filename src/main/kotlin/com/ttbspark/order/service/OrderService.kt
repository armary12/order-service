package com.ttbspark.order.service

import com.ttbspark.order.client.MenuClient
import com.ttbspark.order.client.PaymentClient
import com.ttbspark.order.client.PricingClient
import com.ttbspark.order.client.RestaurantClient
import com.ttbspark.order.exception.InvalidOrderStatusException
import com.ttbspark.order.exception.PaymentNotCompletedException
import com.ttbspark.order.exception.RestaurantClosedException
import com.ttbspark.order.message.OrderEventProducer
import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val restaurantClient: RestaurantClient,
    private val menuClient: MenuClient,
    private val pricingClient: PricingClient,
    private val orderEventProducer: OrderEventProducer,
    private val paymentClient: PaymentClient
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional
    fun createOrder(order: Order): Order {
        logger.info("Starting creation of order for customer: ${order.customerName}, food item: ${order.foodItem}")

        if (order.quantity <= 0) {
            logger.error("Invalid order quantity: ${order.quantity} for order: $order")
            throw IllegalArgumentException("Quantity must be greater than zero.")
        }

        val maxAllowed = 100
        if (order.quantity > maxAllowed) {
            logger.error("Order quantity ${order.quantity} exceeds max allowed: $maxAllowed")
            throw IllegalArgumentException("Cannot order more than $maxAllowed items.")
        }

        // Verify restaurant status
        logger.info("Verifying restaurant status for restaurant id: ${order.restaurantId}")
        if (!restaurantClient.isRestaurantOpen(order.restaurantId)) {
            logger.error("Restaurant with id ${order.restaurantId} is closed.")
            throw RestaurantClosedException("Restaurant is currently closed.")
        }

        // Verify menu availability
        logger.info("Verifying menu availability for food item: ${order.foodItem} in restaurant id: ${order.restaurantId}")
        if (!menuClient.isMenuAvailable(order.foodItem, order.restaurantId)) {
            logger.error("Menu item ${order.foodItem} is not available for restaurant id: ${order.restaurantId}")
            throw IllegalStateException("Menu item is not available.")
        }

        // Fetch actual price
        logger.info("Fetching price for food item: ${order.foodItem}, quantity: ${order.quantity}, restaurant id: ${order.restaurantId}")
        val actualPrice = pricingClient.getActualPrice(order.foodItem, order.quantity, order.restaurantId)
            ?: throw IllegalStateException("Invalid price received from Pricing Service.")

        if (actualPrice <= 0) {
            logger.error("Fetched price is invalid: $actualPrice")
            throw IllegalStateException("Price must be positive.")
        }

        // Save the order with the correct price
        val newOrder = order.copy(totalPrice = actualPrice)
        logger.info("Saving order with total price: $actualPrice")
        val savedOrder = orderRepository.save(newOrder)
        logger.info("Order saved with id: ${savedOrder.id}")

        // Publish Kafka event after transaction commits
        logger.info("Publishing order created event for order id: ${savedOrder.id}")
        orderEventProducer.publishOrderCreatedEvent(newOrder)

        return savedOrder
    }

    fun getOrderById(id: Long): Optional<Order> {
        logger.info("Fetching order with id: $id")
        val orderOptional = orderRepository.findById(id)
        if (orderOptional.isEmpty) {
            logger.warn("Order with id: $id not found.")
        } else {
            logger.info("Order with id: $id found.")
        }
        return orderOptional
    }

    @Transactional
    fun updateOrderStatus(id: Long, newStatus: OrderStatus): Order? {
        logger.info("Updating status for order id: $id to new status: $newStatus")
        val order = orderRepository.findWithLockById(id).orElse(null) ?: run {
            logger.warn("Order with id: $id not found for status update.")
            return null
        }

        // Validate allowed status transitions
        val allowedTransitions = validStatusTransitions[order.status] ?: emptySet()
        if (!allowedTransitions.contains(newStatus)) {
            logger.error("Invalid status transition: ${order.status} → $newStatus for order id: $id")
            throw InvalidOrderStatusException("Invalid status transition: ${order.status} → $newStatus")
        }

        // Check payment if going from PENDING to CONFIRMED
        if (order.status == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            logger.info("Verifying payment for order id: $id before status change to CONFIRMED")
            if (!paymentClient.isPaymentComplete(order.id)) {
                logger.error("Payment not completed for order id: $id")
                throw PaymentNotCompletedException("Payment not yet completed for Order ID: ${order.id}")
            }
        }

        order.updateStatus(newStatus)
        logger.info("Saving order update for order id: $id with new status: $newStatus")
        val updatedOrder = orderRepository.save(order)
        logger.info("Order id: $id updated to status: $newStatus, publishing update event.")
        orderEventProducer.publishOrderStatusUpdatedEvent(updatedOrder, newStatus)

        return updatedOrder
    }

    fun searchOrdersInRestaurant(
        restaurantId: Long,
        status: OrderStatus?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        page: Int,
        size: Int
    ): Page<Order> {
        logger.info("Searching orders for restaurant id: $restaurantId with status: $status, from: $fromDate, to: $toDate, page: $page, size: $size")
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val orders = orderRepository.searchOrders(
            restaurantId = restaurantId,
            status = status,
            fromDate = fromDate,
            toDate = toDate,
            pageable = pageable
        )
        logger.info("Found ${orders.totalElements} orders for restaurant id: $restaurantId")
        return orders
    }

    private val validStatusTransitions: Map<OrderStatus, Set<OrderStatus>> = mapOf(
        OrderStatus.PENDING to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELED),
        OrderStatus.CONFIRMED to setOf(OrderStatus.COOKING, OrderStatus.CANCELED),
        OrderStatus.COOKING to setOf(OrderStatus.DELIVERING, OrderStatus.COMPLETED, OrderStatus.CANCELED),
        OrderStatus.DELIVERING to setOf(OrderStatus.COMPLETED, OrderStatus.CANCELED),
        OrderStatus.CANCELED to emptySet()
    )
}
