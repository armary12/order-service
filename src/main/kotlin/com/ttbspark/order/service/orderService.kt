package com.ttbspark.order.service

import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.repository.OrderRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class OrderService(private val orderRepository: OrderRepository) {

    fun createOrder(order: Order): Order {
        return orderRepository.save(order)
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
