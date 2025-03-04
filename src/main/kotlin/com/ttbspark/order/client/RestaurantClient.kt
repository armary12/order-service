package com.ttbspark.order.client

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

data class RestaurantStatusResponse(
    val result: Boolean
)

@Service
class RestaurantClient(private val webClient: WebClient) {

    private val restaurantServiceUrl = "http://restaurant-service/api/restaurants"

    fun isRestaurantOpen(restaurantId: Long): Boolean {
        return webClient.get()
            .uri("$restaurantServiceUrl/$restaurantId/status")
            .retrieve()
            .bodyToMono(RestaurantStatusResponse::class.java)
            .map { it.result }
            .block() ?: false
    }
}
