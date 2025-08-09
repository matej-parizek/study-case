package cz.matej.parizek.studyCase.eligibility.client

import cz.matej.parizek.eligibility.api.ClientsServerApi
import cz.matej.parizek.eligibility.model.GetClientDetailResponse
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ClientWebClient(
    @Qualifier("clientsWebClient") private val webClient: WebClient
) {
    suspend fun getClientDetails(
        clientId: String,
        correlationId: String?
    ): ResponseEntity<GetClientDetailResponse> =
        webClient.get()
            .uri("/$clientId")
            .header("correlation-id", correlationId ?: "")
            .header("clientId", clientId)
            .retrieve()
            .toEntity(GetClientDetailResponse::class.java)
            .awaitSingle()
}
