package com.ttbspark.order.repository

import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    // Find orders by status
    fun findByStatus(status: OrderStatus): List<Order>

    // Find orders by restaurantId
    fun findByRestaurantId(restaurantId: Long): List<Order>

    // Find all orders created after a specific time
    fun findByCreatedAtAfter(createdAt: LocalDateTime): List<Order>

    // Custom query to get order history
    @Query("""
        SELECT h.timestamp FROM Order o 
        JOIN o.statusHistory h 
        WHERE o.id = :orderId AND h.status = :status
    """)
    fun getOrderStatusTimestamp(@Param("orderId") orderId: Long, @Param("status") status: OrderStatus): LocalDateTime?
}
