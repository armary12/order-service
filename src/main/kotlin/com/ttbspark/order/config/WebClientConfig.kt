package com.ttbspark.order.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .filter(mockResponseInterceptor())
            .build()
    }

    private fun mockResponseInterceptor(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
            val requestUri = clientResponse.headers().asHttpHeaders()["Request-URI"]?.firstOrNull()

            if (requestUri?.contains("mock-endpoint") == true) {
                val mockBody = "This is a mocked response"
                return@ofResponseProcessor Mono.just(
                    org.springframework.web.reactive.function.client.ClientResponse
                        .create(clientResponse.statusCode())
                        .header("Content-Type", "application/json")
                        .body(mockBody)
                        .build()
                )
            }
            Mono.just(clientResponse)
        }
    }
}
