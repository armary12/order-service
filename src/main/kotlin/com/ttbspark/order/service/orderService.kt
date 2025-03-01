package com.ttbspark.order.service

import com.ttbspark.order.client.MenuClient
import com.ttbspark.order.client.PricingClient
import com.ttbspark.order.client.RestaurantClient
import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.repository.OrderRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val restaurantClient: RestaurantClient,
    private val menuClient: MenuClient,
    private val pricingClient: PricingClient
) {

    fun createOrder(order: Order): Order {
        // Verify restaurant status
        if (!restaurantClient.isRestaurantOpen(order.restaurantId)) {
            throw IllegalStateException("Restaurant is currently closed.")
        }

        // Verify menu availability
        if (!menuClient.isMenuAvailable(order.foodItem, order.restaurantId)) {
            throw IllegalStateException("Menu item is not available.")
        }

        // Fetch actual price
        val actualPrice = pricingClient.getActualPrice(order.foodItem, order.quantity, order.restaurantId)
            ?: throw IllegalStateException("Invalid price received from Pricing Service.")

        // Save the order with the correct price
        val newOrder = order.copy(totalPrice = actualPrice)
        return orderRepository.save(newOrder)
    }

    fun getOrderById(id: Long): Optional<Order> {
        return orderRepository.findById(id)
    }

    fun updateOrderStatus(id: Long, newStatus: OrderStatus): Order? {
        val order = orderRepository.findById(id).orElse(null) ?: return null
        order.updateStatus(newStatus)
        return orderRepository.save(order)
    }

    fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return orderRepository.findByStatus(status)
    }

    fun getOrdersByRestaurant(restaurantId: Long): List<Order> {
        return orderRepository.findByRestaurantId(restaurantId)
    }
}
