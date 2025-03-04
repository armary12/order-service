package com.ttbspark.order.controller

import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.service.OrderService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OrderControllerTest {

    private val orderService: OrderService = mock()
    private val orderController = OrderController(orderService)

    // Helper function to create a sample Order instance
    private fun createSampleOrder(id: Long = 1L): Order {
        val fixedDateTime = LocalDateTime.of(2025, 3, 4, 12, 0)
        return Order(
            id = id,
            customerName = "John Doe",
            foodItem = "Pizza",
            quantity = 2,
            unitPrice = 10.0,
            totalPrice = 20.0,
            address = "123 Main St",
            restaurantId = 1L,
            status = OrderStatus.PENDING,
            createdAt = fixedDateTime,
            statusHistory = mutableMapOf(OrderStatus.PENDING to fixedDateTime)
        )
    }

    @Test
    fun `createOrder should return created order`() {
        // given
        val order = createSampleOrder()
        whenever(orderService.createOrder(order)).thenReturn(order)

        // when
        val response: ResponseEntity<Order> = orderController.createOrder(order)

        // then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(order, response.body)
        verify(orderService, times(1)).createOrder(order)
    }

    @Test
    fun `getOrderById should return order when found`() {
        // given
        val order = createSampleOrder()
        whenever(orderService.getOrderById(1L)).thenReturn(Optional.of(order))

        // when
        val response = orderController.getOrderById(1L)

        // then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(order, response.body)
        verify(orderService, times(1)).getOrderById(1L)
    }

    @Test
    fun `getOrderById should return not found when order does not exist`() {
        // given
        whenever(orderService.getOrderById(1L)).thenReturn(Optional.empty())

        // when
        val response = orderController.getOrderById(1L)

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        verify(orderService, times(1)).getOrderById(1L)
    }

    @Test
    fun `updateOrderStatus should return updated order when found`() {
        // given
        val order = createSampleOrder()
        val newStatus = OrderStatus.CONFIRMED
        whenever(orderService.updateOrderStatus(1L, newStatus)).thenReturn(order)

        // when
        val response = orderController.updateOrderStatus(1L, newStatus)

        // then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(order, response.body)
        verify(orderService, times(1)).updateOrderStatus(1L, newStatus)
    }

    @Test
    fun `updateOrderStatus should return not found when order does not exist`() {
        // given
        val newStatus = OrderStatus.CONFIRMED
        whenever(orderService.updateOrderStatus(1L, newStatus)).thenReturn(null)

        // when
        val response = orderController.updateOrderStatus(1L, newStatus)

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        verify(orderService, times(1)).updateOrderStatus(1L, newStatus)
    }

    @Test
    fun `searchOrders should return a page of orders`() {
        // given
        val restaurantId = 1L
        val status: OrderStatus? = null
        val fromDate: LocalDateTime? = null
        val toDate: LocalDateTime? = null
        val page = 0
        val size = 10
        val order = createSampleOrder()
        val orders = listOf(order)
        val pageResult: Page<Order> = PageImpl(orders)
        whenever(
            orderService.searchOrdersInRestaurant(
                restaurantId = restaurantId,
                status = status,
                fromDate = fromDate,
                toDate = toDate,
                page = page,
                size = size
            )
        ).thenReturn(pageResult)

        // when
        val response = orderController.searchOrders(
            restaurantId = restaurantId,
            status = status,
            fromDate = fromDate,
            toDate = toDate,
            page = page,
            size = size
        )

        // then
        assertEquals(pageResult, response)
        verify(orderService, times(1)).searchOrdersInRestaurant(
            restaurantId = restaurantId,
            status = status,
            fromDate = fromDate,
            toDate = toDate,
            page = page,
            size = size
        )
    }
}
