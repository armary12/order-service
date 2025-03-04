package com.ttbspark.order.controller

import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.service.OrderService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun createOrder(@RequestBody order: Order): ResponseEntity<Order> {
        return ResponseEntity.ok(orderService.createOrder(order))
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<Order> {
        val order = orderService.getOrderById(id)
        return if (order.isPresent) ResponseEntity.ok(order.get()) else ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}/status")
    fun updateOrderStatus(@PathVariable id: Long, @RequestParam status: OrderStatus): ResponseEntity<Order> {
        val updatedOrder = orderService.updateOrderStatus(id, status)
        return if (updatedOrder != null) ResponseEntity.ok(updatedOrder) else ResponseEntity.notFound().build()
    }

    @GetMapping("/restaurants/{restaurantId}/orders/search")
    fun searchOrders(
        @PathVariable restaurantId: Long,
        @RequestParam(required = false) status: OrderStatus?,
        @RequestParam(required = false) fromDate: LocalDateTime?,
        @RequestParam(required = false) toDate: LocalDateTime?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<Order> {
        return orderService.searchOrdersInRestaurant(
            restaurantId = restaurantId,
            status = status,
            fromDate = fromDate,
            toDate = toDate,
            page = page,
            size = size
        )
    }
}
