package cz.matej.parizek.studyCase.eligibility.client

import cz.matej.parizek.eligibility.model.GetAccountsRequest
import cz.matej.parizek.eligibility.model.GetAccountsResponse
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class AccountWebClient(
    @Qualifier("accountsWebClient") private val webClient: WebClient
)  {
  suspend fun listGet(
        clientId: String,
        correlationId: String?,
        getAccountsRequest: GetAccountsRequest?
    ): ResponseEntity<GetAccountsResponse> =
        webClient.get()
            .uri("/list")
            .header("clientId", clientId)
            .header("correlation-id", correlationId ?: "")
            .retrieve()
            .toEntity(GetAccountsResponse::class.java)
            .awaitSingle()
}
