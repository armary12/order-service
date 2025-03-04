package com.ttbspark.order.client

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

data class PaymentCheckResponse(
    val result: Boolean
)

@Service
class PaymentClient(private val webClient: WebClient) {

    private val paymentServiceUrl = "http://payment-service/api/order"

    fun isPaymentComplete(orderId: Long): Boolean {
        return webClient.get()
            .uri("$paymentServiceUrl/$orderId/status")
            .retrieve()
            .bodyToMono(PaymentCheckResponse::class.java)
            .map { it.result }
            .block() ?: false
    }
}
