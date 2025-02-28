package com.ttbspark.order.controller

import com.ttbspark.order.model.Order
import com.ttbspark.order.model.OrderStatus
import com.ttbspark.order.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<Order>> {
        return ResponseEntity.ok(orderService.getAllOrders())
    }

    @GetMapping("/status/{status}")
    fun getOrdersByStatus(@PathVariable status: OrderStatus): ResponseEntity<List<Order>> {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status))
    }

    @GetMapping("/restaurant/{restaurantId}")
    fun getOrdersByRestaurant(@PathVariable restaurantId: Long): ResponseEntity<List<Order>> {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId))
    }

    @GetMapping("/{id}/status/{status}/timestamp")
    fun getOrderStatusTimestamp(
        @PathVariable id: Long,
        @PathVariable status: OrderStatus
    ): ResponseEntity<String> {
        val timestamp = orderService.getOrderStatusTimestamp(id, status)
        return if (timestamp != null) ResponseEntity.ok(timestamp.toString()) else ResponseEntity.notFound().build()
    }
}
