package com.ttbspark.order.repository

import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @Query("""
        SELECT o 
        FROM Order o
        WHERE o.restaurantId = :restaurantId
          AND (:status IS NULL OR o.status = :status)
          AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
          AND (:toDate IS NULL OR o.createdAt <= :toDate)
    """)
    fun searchOrders(
        @Param("restaurantId") restaurantId: Long,
        @Param("status") status: OrderStatus?,
        @Param("fromDate") fromDate: LocalDateTime?,
        @Param("toDate") toDate: LocalDateTime?,
        pageable: Pageable
    ): Page<Order>
}
