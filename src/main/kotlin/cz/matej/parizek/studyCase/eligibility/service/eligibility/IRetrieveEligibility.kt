package cz.matej.parizek.studyCase.eligibility.service.eligibility

import arrow.core.Either
import cz.matej.parizek.eligibility.model.GetEligibilityResponse
import org.springframework.http.ResponseEntity

interface IRetrieveEligibility {

    suspend fun retrieveEligibility(correlationId: String?, clientId: String): Either<EligibilityFailure, GetEligibilityResponse>
}

sealed interface EligibilityFailure {
    data object BadRequest : EligibilityFailure
    data object Unauthorized : EligibilityFailure
    data class UnexpectedError(val responseEntity: ResponseEntity<*>) : EligibilityFailure
}