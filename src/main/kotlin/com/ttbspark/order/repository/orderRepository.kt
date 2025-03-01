package com.ttbspark.order.repository

import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    // Find orders by status
    fun findByStatus(status: OrderStatus): List<Order>

    // Find orders by restaurantId
    fun findByRestaurantId(restaurantId: Long): List<Order>

    // Find all orders created after a specific time
    fun findByCreatedAtAfter(createdAt: LocalDateTime): List<Order>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): Optional<Order>
}
