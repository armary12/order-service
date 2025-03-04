package com.ttbspark.order.service

import com.ttbspark.order.client.MenuClient
import com.ttbspark.order.client.PaymentClient
import com.ttbspark.order.client.PricingClient
import com.ttbspark.order.client.RestaurantClient
import com.ttbspark.order.exception.PaymentNotCompletedException
import com.ttbspark.order.message.OrderEventProducer
import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.repository.OrderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.util.*

internal class OrderServiceTest {

    @Mock
    private lateinit var orderRepository: OrderRepository

    @Mock
    private lateinit var restaurantClient: RestaurantClient

    @Mock
    private lateinit var menuClient: MenuClient

    @Mock
    private lateinit var pricingClient: PricingClient

    @Mock
    private lateinit var orderEventProducer: OrderEventProducer

    @Mock
    private lateinit var paymentClient: PaymentClient

    @InjectMocks
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `createOrder should succeed when validations pass`() {
        // Given
        val order = Order(
            customerName = "John Doe",
            foodItem = "Pizza",
            quantity = 2,
            unitPrice = 10.0,
            totalPrice = 0.0, // initial totalPrice will be updated
            address = "123 Main St",
            restaurantId = 100,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        // Mocks for external service validations:
        `when`(restaurantClient.isRestaurantOpen(order.restaurantId)).thenReturn(true)
        `when`(menuClient.isMenuAvailable(order.foodItem, order.restaurantId)).thenReturn(true)
        `when`(pricingClient.getActualPrice(order.foodItem, order.quantity, order.restaurantId)).thenReturn(20.0)
        // Simulate saving by returning a copy with a generated id and updated totalPrice.
        val savedOrder = order.copy(id = 1, totalPrice = 20.0)
        `when`(orderRepository.save(any(Order::class.java))).thenReturn(savedOrder)

        // When
        val result = orderService.createOrder(order)

        // Then
        assertEquals(20.0, result.totalPrice)
        assertEquals(1, result.id)
//        verify(orderEventProducer).publishOrderCreatedEvent(any())
    }

    @Test
    fun `createOrder should throw IllegalArgumentException for zero quantity`() {
        // Given
        val order = Order(
            customerName = "John Doe",
            foodItem = "Pizza",
            quantity = 0,
            unitPrice = 10.0,
            totalPrice = 0.0,
            address = "123 Main St",
            restaurantId = 100,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            orderService.createOrder(order)
        }
        assertEquals("Quantity must be greater than zero.", exception.message)
    }

    @Test
    fun `updateOrderStatus should update status when valid transition and payment complete`() {
        // Given
        val order = Order(
            id = 1,
            customerName = "Jane Doe",
            foodItem = "Burger",
            quantity = 1,
            unitPrice = 5.0,
            totalPrice = 5.0,
            address = "456 Oak St",
            restaurantId = 200,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        `when`(orderRepository.findWithLockById(1)).thenReturn(Optional.of(order))
        `when`(paymentClient.isPaymentComplete(order.id)).thenReturn(true)

        // Create an updated order copy with the new status
        val orderWithConfirmedStatus = order.copy(status = OrderStatus.CONFIRMED)
        // Stub the save method to return the updated order
        `when`(orderRepository.save(any(Order::class.java))).thenReturn(orderWithConfirmedStatus)

        // When
        val updatedOrder = orderService.updateOrderStatus(1, OrderStatus.CONFIRMED)

        // Then
        assertNotNull(updatedOrder)
        assertEquals(OrderStatus.CONFIRMED, updatedOrder?.status)
//        verify(orderEventProducer).publishOrderStatusUpdatedEvent(any(), argThat { status: OrderStatus -> status == OrderStatus.CONFIRMED })
    }

    @Test
    fun `updateOrderStatus should throw PaymentNotCompletedException when payment incomplete`() {
        // Given
        val order = Order(
            id = 1,
            customerName = "Jane Doe",
            foodItem = "Burger",
            quantity = 1,
            unitPrice = 5.0,
            totalPrice = 5.0,
            address = "456 Oak St",
            restaurantId = 200,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        `when`(orderRepository.findWithLockById(1)).thenReturn(Optional.of(order))
        `when`(paymentClient.isPaymentComplete(order.id)).thenReturn(false)
        // When & Then
        assertThrows(PaymentNotCompletedException::class.java) {
            orderService.updateOrderStatus(1, OrderStatus.CONFIRMED)
        }
    }

    @Test
    fun `getOrderById should return order when it exists`() {
        // Given
        val order = Order(
            id = 1,
            customerName = "Alice",
            foodItem = "Salad",
            quantity = 1,
            unitPrice = 7.0,
            totalPrice = 7.0,
            address = "789 Maple Ave",
            restaurantId = 300,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        `when`(orderRepository.findById(1)).thenReturn(Optional.of(order))
        // When
        val result = orderService.getOrderById(1)
        // Then
        assertTrue(result.isPresent)
        assertEquals(order, result.get())
    }

    @Test
    fun `searchOrdersInRestaurant should return paged orders`() {
        // Given
        val ordersList = listOf(
            Order(
                id = 1,
                customerName = "Alice",
                foodItem = "Salad",
                quantity = 1,
                unitPrice = 7.0,
                totalPrice = 7.0,
                address = "789 Maple Ave",
                restaurantId = 300,
                status = OrderStatus.PENDING,
                createdAt = LocalDateTime.now()
            ),
            Order(
                id = 2,
                customerName = "Bob",
                foodItem = "Soup",
                quantity = 2,
                unitPrice = 5.0,
                totalPrice = 10.0,
                address = "101 Pine St",
                restaurantId = 300,
                status = OrderStatus.CONFIRMED,
                createdAt = LocalDateTime.now()
            )
        )
        val pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending())
        val page = PageImpl(ordersList, pageable, ordersList.size.toLong())
        `when`(orderRepository.searchOrders(300, null, null, null, pageable)).thenReturn(page)

        // When
        val result = orderService.searchOrdersInRestaurant(300, null, null, null, 0, 10)

        // Then
        assertEquals(2, result.totalElements)
        assertEquals(ordersList, result.content)
    }
}
