package com.ttbspark.order.client

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class RestaurantClient(private val webClient: WebClient) {

    private val restaurantServiceUrl = "http://restaurant-service/api/restaurants"

    fun isRestaurantOpen(restaurantId: Long): Boolean {
        return webClient.get()
            .uri("$restaurantServiceUrl/$restaurantId/status")
            .retrieve()
            .bodyToMono(Boolean::class.java)
            .block() ?: false
    }
}
