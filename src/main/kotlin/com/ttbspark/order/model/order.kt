package com.ttbspark.order.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val customerName: String,

    val foodItem: String,

    val quantity: Int,

    val unitPrice: Double,

    val totalPrice: Double,

    val address: String,

    val restaurantId: Long,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ElementCollection
    @CollectionTable(name = "order_status_history", joinColumns = [JoinColumn(name = "order_id")])
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "status")
    @Column(name = "timestamp")
    var statusHistory: MutableMap<OrderStatus, LocalDateTime> = mutableMapOf()
) {
    fun updateStatus(newStatus: OrderStatus) {
        this.status = newStatus
        this.statusHistory[newStatus] = LocalDateTime.now()
    }
}

enum class OrderStatus {
    PENDING, CONFIRMED, COOKING, DELIVERING, COMPLETED, CANCELED
}
