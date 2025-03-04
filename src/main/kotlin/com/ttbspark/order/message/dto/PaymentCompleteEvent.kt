package com.ttbspark.order.message.dto

data class PaymentCompletedEvent(
    val orderId: Long,
    val paymentId: Long
)
