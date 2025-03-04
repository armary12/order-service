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

    @Transactional
    fun createOrder(order: Order): Order {
        if (order.quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than zero.")
        }

        val maxAllowed = 100
        if (order.quantity > maxAllowed) {
            throw IllegalArgumentException("Cannot order more than $maxAllowed items.")
        }

        // Verify restaurant status
        if (!restaurantClient.isRestaurantOpen(order.restaurantId)) {
            throw RestaurantClosedException("Restaurant is currently closed.")
        }

        // Verify menu availability
        if (!menuClient.isMenuAvailable(order.foodItem, order.restaurantId)) {
            throw IllegalStateException("Menu item is not available.")
        }

        // Fetch actual price
        val actualPrice = pricingClient.getActualPrice(order.foodItem, order.quantity, order.restaurantId)
            ?: throw IllegalStateException("Invalid price received from Pricing Service.")

        if (actualPrice <= 0) {
            throw IllegalStateException("Price must be positive.")
        }

        // Save the order with the correct price
        val newOrder = order.copy(totalPrice = actualPrice)
        val savedOrder = orderRepository.save(newOrder)

        // Publish Kafka event after transaction commits
        orderEventProducer.publishOrderCreatedEvent(newOrder)

        return savedOrder
    }

    fun getOrderById(id: Long): Optional<Order> {
        return orderRepository.findById(id)
    }

    @Transactional
    fun updateOrderStatus(id: Long, newStatus: OrderStatus): Order? {
        val order = orderRepository.findWithLockById(id).orElse(null) ?: return null

        // Validate allowed status transitions
        val allowedTransitions = validStatusTransitions[order.status] ?: emptySet()
        if (!allowedTransitions.contains(newStatus)) {
            throw InvalidOrderStatusException("Invalid status transition: ${order.status} → $newStatus")
        }

        // Check payment if going from PENDING to CONFIRMED
        if (order.status == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            if (!paymentClient.isPaymentComplete(order.id)) {
                throw PaymentNotCompletedException("Payment not yet completed for Order ID: ${order.id}")
            }
        }

        order.updateStatus(newStatus)
        val updatedOrder = orderRepository.save(order)

        // Publish status update event after commit
        orderEventProducer.publishOrderStatusUpdatedEvent(updatedOrder, newStatus)

        return updatedOrder
    }

    private val validStatusTransitions: Map<OrderStatus, Set<OrderStatus>> = mapOf(
        OrderStatus.PENDING to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELED),
        OrderStatus.CONFIRMED to setOf(OrderStatus.COOKING, OrderStatus.CANCELED),
        OrderStatus.COOKING to setOf(OrderStatus.DELIVERING, OrderStatus.COMPLETED, OrderStatus.CANCELED),
        OrderStatus.DELIVERING to setOf(OrderStatus.COMPLETED, OrderStatus.CANCELED),
        OrderStatus.CANCELED to emptySet()
    )

    fun searchOrdersInRestaurant(
        restaurantId: Long,
        status: OrderStatus?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        page: Int,
        size: Int
    ): Page<Order> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())

        return orderRepository.searchOrders(
            restaurantId = restaurantId,
            status = status,
            fromDate = fromDate,
            toDate = toDate,
            pageable = pageable
        )
    }
}
