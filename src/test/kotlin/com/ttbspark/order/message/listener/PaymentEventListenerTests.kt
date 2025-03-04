package com.ttbspark.order.message.listener

import com.ttbspark.order.exception.InvalidOrderStatusException
import com.ttbspark.order.message.dto.PaymentCompletedEvent
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.service.OrderService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

internal class PaymentEventListenerTest {

    private lateinit var orderService: OrderService
    private lateinit var paymentEventListener: PaymentEventListener

    @BeforeEach
    fun setUp() {
        orderService = mock()
        paymentEventListener = PaymentEventListener(orderService)
    }

    @Test
    fun `handlePaymentCompleted should update order status to CONFIRMED`() {
        // Given
        val event = PaymentCompletedEvent(orderId = 1, paymentId = 100)
        // When
        assertDoesNotThrow {
            paymentEventListener.handlePaymentCompleted(event)
        }
        // Then
        verify(orderService).updateOrderStatus(1, OrderStatus.CONFIRMED)
    }

    @Test
    fun `handlePaymentCompleted should handle InvalidOrderStatusException gracefully`() {
        // Given
        val event = PaymentCompletedEvent(orderId = 2, paymentId = 200)
        doAnswer { throw InvalidOrderStatusException("Invalid status transition") }
            .whenever(orderService)
            .updateOrderStatus(2, OrderStatus.CONFIRMED)

        // When & Then: ensure no exception is propagated
        assertDoesNotThrow {
            paymentEventListener.handlePaymentCompleted(event)
        }

        verify(orderService).updateOrderStatus(2, OrderStatus.CONFIRMED)
    }

    @Test
    fun `handlePaymentCompleted should handle unexpected exceptions gracefully`() {
        // Given
        val event = PaymentCompletedEvent(orderId = 3, paymentId = 300)
        whenever(orderService.updateOrderStatus(3, OrderStatus.CONFIRMED))
            .thenThrow(RuntimeException("Unexpected error"))
        // When & Then: assert that the exception is handled and not propagated.
        assertDoesNotThrow {
            paymentEventListener.handlePaymentCompleted(event)
        }
        verify(orderService).updateOrderStatus(3, OrderStatus.CONFIRMED)
    }
}
