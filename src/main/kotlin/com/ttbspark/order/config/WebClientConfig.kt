package com.ttbspark.order.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
@Profile("local")
class LocalWebClientConfiguration {

    @Bean
    fun webClient(): WebClient {
        val mockExchangeFunction = ExchangeFunction { request: ClientRequest ->
            val url = request.url().toString()
            when {
                url.contains("menu-service/api/menus/check") ->{
                    val responseBody = """{"result": true}"""
                    Mono.just(
                        ClientResponse
                            .create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(responseBody)
                            .build()

                    )
                }
                url.contains("restaurant-service/api/restaurants") && url.contains("/status") -> {
                    val responseBody = """{"result": true}"""
                    Mono.just(
                        ClientResponse
                            .create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(responseBody)
                            .build()
                    )
                }
                url.contains("pricing-service/api/pricing/calculate") -> {
                    val responseBody = """{"actualPrice": 100}"""
                    Mono.just(
                        ClientResponse
                            .create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(responseBody)
                            .build()
                    )
                }
                url.contains("payment-service/api/order") -> {
                    val responseBody = """{"result": true}"""
                    Mono.just(
                        ClientResponse
                            .create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(responseBody)
                            .build()
                    )
                }
                else ->
                    Mono.just(
                        ClientResponse
                            .create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body("")
                            .build()
                    )
            }
        }

        return WebClient.builder()
            .exchangeFunction(mockExchangeFunction)
            .build()
    }
}
