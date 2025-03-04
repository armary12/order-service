package com.ttbspark.order.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class RestaurantClientTest {

    @Test
    fun `isRestaurantOpen returns true when response result is true`() {
        // Given a simulated response with {"result": true}
        val responseBody = """{"result": true}"""
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
        val restaurantClient = RestaurantClient(webClient)

        // When
        val isOpen = restaurantClient.isRestaurantOpen(123)

        // Then
        assertTrue(isOpen)
    }

    @Test
    fun `isRestaurantOpen returns false when response result is false`() {
        // Given a simulated response with {"result": false}
        val responseBody = """{"result": false}"""
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
        val restaurantClient = RestaurantClient(webClient)

        // When
        val isOpen = restaurantClient.isRestaurantOpen(123)

        // Then
        assertFalse(isOpen)
    }
}
