package com.ttbspark.order.message

import com.ttbspark.order.message.dto.OrderEvent
import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class OrderEventProducer(private val kafkaTemplate: KafkaTemplate<String, OrderEvent>) {

    fun publishOrderCreatedEvent(order: Order) {
        val event = OrderEvent(
            orderId = order.id,
            restaurantId = order.restaurantId,
            foodItem = order.foodItem,
            quantity = order.quantity,
            totalPrice = order.totalPrice,
            status = order.status,
            eventType = "ORDER_CREATED"
        )
        kafkaTemplate.send("order-events", event.orderId.toString(), event)
    }

    fun publishOrderStatusUpdatedEvent(order: Order, newStatus: OrderStatus) {
        val event = OrderEvent(
            orderId = order.id,
            restaurantId = order.restaurantId,
            foodItem = order.foodItem,
            quantity = order.quantity,
            totalPrice = order.totalPrice,
            status = newStatus,
            eventType = "STATUS_UPDATED"
        )
        kafkaTemplate.send("order-events", event.orderId.toString(), event)
    }
}
