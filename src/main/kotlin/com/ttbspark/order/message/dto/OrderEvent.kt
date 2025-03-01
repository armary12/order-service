package com.ttbspark.order.message.dto

import com.ttbspark.order.model.OrderStatus

data class OrderEvent(
    val orderId: Long,
    val restaurantId: Long,
    val foodItem: String,
    val quantity: Int,
    val totalPrice: Double,
    val status: OrderStatus,
    val eventType: String // "ORDER_CREATED" or "STATUS_UPDATED"
)
