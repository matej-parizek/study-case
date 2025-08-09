package cz.matej.parizek.studyCase.eligibility.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {

    @Bean("clientsWebClient")
    fun clientsWebClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${external.clients.base-url}") baseUrl: String,
        @Value("\${external.clients.api-key}") apiKey: String
    ): WebClient = webClientBuilder.baseUrl(baseUrl).defaultHeader("api-key",apiKey).build()

    @Bean("accountsWebClient")
    fun accountsWebClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${external.accounts.base-url}") baseUrl: String,
        @Value("\${external.clients.api-key}") apiKey: String
    ): WebClient = webClientBuilder.baseUrl(baseUrl).defaultHeader("api-key",apiKey).build()
}
