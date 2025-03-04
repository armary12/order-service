package com.ttbspark.order.message.listener

import com.ttbspark.order.exception.InvalidOrderStatusException
import com.ttbspark.order.message.dto.PaymentCompletedEvent
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@Profile("dev","sit","prod")
class PaymentEventListener(
    private val orderService: OrderService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["payment_completed_topic"], groupId = "order-service-group")
    fun handlePaymentCompleted(event: PaymentCompletedEvent) {
        logger.info("Received PaymentCompletedEvent for orderId=${event.orderId}")
        try {
            orderService.updateOrderStatus(event.orderId, OrderStatus.CONFIRMED)
        } catch (e: InvalidOrderStatusException) {
            logger.error("Cannot update order status to CONFIRMED: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error updating order for orderId=${event.orderId}", e)
        }
    }
}
