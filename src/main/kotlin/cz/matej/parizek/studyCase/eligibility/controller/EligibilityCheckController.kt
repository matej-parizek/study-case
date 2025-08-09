package cz.matej.parizek.studyCase.eligibility.controller

import arrow.core.Either
import cz.matej.parizek.eligibility.api.ApplicationServerApi
import cz.matej.parizek.eligibility.model.GetEligibilityResponse
import cz.matej.parizek.studyCase.eligibility.service.eligibility.IRetrieveEligibility
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EligibilityCheckController(
    private val service: IRetrieveEligibility,
) : BaseController(), ApplicationServerApi {

    override suspend fun apiV1EligibilityGet(
        @RequestParam("clientId") clientId: String,
        @RequestParam("correlationId") correlationId: String?,
    ): ResponseEntity<GetEligibilityResponse> =
        withAuthentication(clientId) {
            when (val r = service.retrieveEligibility(correlationId, clientId)) {
                is Either.Left  -> ResponseEntity.badRequest().build()
                is Either.Right -> ResponseEntity.ok(r.value)
            }
        }
}
