package com.ttbspark.order.client

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PaymentClient(private val webClient: WebClient) {

    private val paymentServiceUrl = "http://payment-service/api/order"

    fun isPaymentComplete(orderId: Long): Boolean {
        return webClient.get()
            .uri("$paymentServiceUrl/$orderId/status")
            .retrieve()
            .bodyToMono(Boolean::class.java)
            .block() ?: false
    }
}
