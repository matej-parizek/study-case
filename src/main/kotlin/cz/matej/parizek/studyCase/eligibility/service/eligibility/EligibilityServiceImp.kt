package cz.matej.parizek.studyCase.eligibility.service.eligibility

import arrow.core.Either
import cz.matej.parizek.eligibility.api.AccountsServerApi
import cz.matej.parizek.eligibility.api.ClientsServerApi
import cz.matej.parizek.eligibility.model.GetAccountsResponse
import cz.matej.parizek.eligibility.model.GetClientDetailResponse
import cz.matej.parizek.eligibility.model.GetEligibilityResponse
import cz.matej.parizek.studyCase.eligibility.client.AccountWebClient
import cz.matej.parizek.studyCase.eligibility.client.ClientWebClient
import cz.matej.parizek.studyCase.eligibility.entity.Eligibility
import cz.matej.parizek.studyCase.eligibility.repository.EligibilityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class EligibilityServiceImp(
    private val accountClient: AccountWebClient,
    private val clientClient: ClientWebClient,
    private val repository: EligibilityRepository
) : IRetrieveEligibility {
    override suspend fun retrieveEligibility(correlationId: String?, clientId: String)
        : Either<EligibilityFailure, GetEligibilityResponse> = coroutineScope {

        val clientResponse = asyncApiCall {
            clientClient.getClientDetails(
                clientId = clientId,
                correlationId = correlationId
            )
        }

        val accountsResponse = asyncApiCall {
            accountClient.listGet(
                clientId = clientId,
                correlationId = correlationId,
                getAccountsRequest = null
            )
        }

        evaluateResponses(
            clientEither = clientResponse.await(),
            accountEither = accountsResponse.await()
        )
    }


    private fun <T> CoroutineScope.asyncApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        call: suspend () -> ResponseEntity<T>
    ) = async(dispatcher) {
        val response = call()
        when (response.statusCode) {
            HttpStatus.OK ->
                Either.Right(response.body)

            HttpStatus.UNAUTHORIZED ->
                Either.Left(EligibilityFailure.Unauthorized)

            HttpStatus.BAD_REQUEST ->
                Either.Left(EligibilityFailure.BadRequest)

            else ->
                Either.Left(EligibilityFailure.UnexpectedError(response))
        }
    }

    private suspend fun evaluateResponses(
        clientEither: Either<EligibilityFailure, GetClientDetailResponse?>,
        accountEither: Either<EligibilityFailure, GetAccountsResponse?>
    ): Either<EligibilityFailure, GetEligibilityResponse> {
        val client = when (clientEither) {
            is Either.Left -> return Either.Left(clientEither.value)
            is Either.Right -> clientEither.value ?: return Either.Left(EligibilityFailure.BadRequest)
        }
        val accounts = when (accountEither) {
            is Either.Left -> return Either.Left(accountEither.value)
            is Either.Right -> accountEither.value ?: return Either.Left(EligibilityFailure.BadRequest)
        }
        val dateBirth = LocalDate.parse(client.birthDate);

        val accountValidation = !(accounts.accounts.isNullOrEmpty())
        val clientAgeValidation = dateBirth.oldEnough()

        if (client.clientId != accounts.client.clientId) {
            return Either.Left(EligibilityFailure.BadRequest)
        }

        repository.save(
            Eligibility(
                clientId = UUID.fromString(client.clientId),
                eligible = accountValidation && clientAgeValidation,
            )
        )

        val reasons = when {
            !accountValidation && !clientAgeValidation ->
                listOf(GetEligibilityResponse.Reasons.ACCOUNT, GetEligibilityResponse.Reasons.ADULT)
            !accountValidation ->
                listOf(GetEligibilityResponse.Reasons.ACCOUNT)
            !clientAgeValidation ->
                listOf(GetEligibilityResponse.Reasons.ADULT)
            else -> null
        }

        return Either.Right(
            GetEligibilityResponse(
                eligible = accountValidation && clientAgeValidation,
                reasons = reasons?.ifEmpty { null }
            )
        )
    }


}

private fun LocalDate.oldEnough(): Boolean {
    val today = LocalDate.now()
    return this.plusYears(18).isBefore(today)
}