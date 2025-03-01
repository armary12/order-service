package com.ttbspark.order.client

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PricingClient(private val webClient: WebClient) {

    private val pricingServiceUrl = "http://pricing-service/api/pricing/calculate"

    fun getActualPrice(foodItem: String, quantity: Int, restaurantId: Long): Double? {
        val requestBody = mapOf(
            "foodItem" to foodItem,
            "quantity" to quantity,
            "restaurantId" to restaurantId
        )

        return webClient.post()
            .uri(pricingServiceUrl)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Double::class.java)
            .block()
    }
}
