package com.ttbspark.order.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class PricingClientTest {

    @Test
    fun `getActualPrice returns expected price when response is valid`() {
        // Given a simulated response with {"actualPrice": 15.0}
        val responseBody = """{"actualPrice": 15.0}"""
        val exchangeFunction = ExchangeFunction { _: ClientRequest ->
            Mono.just(
                ClientResponse
                    .create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build()
            )
        }
        val webClient = WebClient.builder()
            .exchangeFunction(exchangeFunction)
            .build()
        val pricingClient = PricingClient(webClient)

        // When
        val actualPrice = pricingClient.getActualPrice("Burger", 2, 123)

        // Then
        assertEquals(15.0, actualPrice)
    }
}
