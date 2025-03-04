package com.ttbspark.order.client

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

data class MenuCheckResponse(
    val result: Boolean
)

@Service
class MenuClient(private val webClient: WebClient) {

    private val menuServiceUrl = "http://menu-service/api/menus/check"

    fun isMenuAvailable(foodItem: String, restaurantId: Long): Boolean {
        val requestBody = mapOf("foodItem" to foodItem, "restaurantId" to restaurantId)

        return webClient.post()
            .uri(menuServiceUrl)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(MenuCheckResponse::class.java)
            .map { it.result }
            .block() ?: false
    }
}
