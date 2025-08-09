package cz.matej.parizek.studyCase.eligibility.service.eligibility

import arrow.core.Either
import cz.matej.parizek.eligibility.model.GetAccountsResponse
import cz.matej.parizek.eligibility.model.GetClientDetailResponse
import cz.matej.parizek.eligibility.model.GetEligibilityResponse
import cz.matej.parizek.studyCase.eligibility.TestDataFactory
import cz.matej.parizek.studyCase.eligibility.client.AccountWebClient
import cz.matej.parizek.studyCase.eligibility.client.ClientWebClient
import cz.matej.parizek.studyCase.eligibility.entity.Eligibility
import cz.matej.parizek.studyCase.eligibility.repository.EligibilityRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID

class EligibilityServiceImpUnitTest {

    private val accountsClient: AccountWebClient = mock()
    private val clientsClient: ClientWebClient = mock()
    private val repository: EligibilityRepository = mock()
    private lateinit var service: EligibilityServiceImp


    @BeforeEach
    fun setup() {
        service = EligibilityServiceImp(accountsClient, clientsClient, repository)
    }

    @Test
    fun `success - eligibility=true`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        val client = TestDataFactory.clientDetail(clientId = clientId, birthDateIso = TestDataFactory.birthDateAdult())
        val accounts = TestDataFactory.accountsResponse(clientId = clientId, accountsCount = 2)

        whenever(clientsClient.getClientDetails(eq(clientId),eq(correlationId)))
            .thenReturn(ResponseEntity.ok(client))
        whenever(accountsClient.listGet(eq(clientId), eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.ok(accounts))

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isRight())
        val body = (res as Either.Right<GetEligibilityResponse>).value
        Assertions.assertTrue(body.eligible)
        Assertions.assertNull(body.reasons)

        val captor = argumentCaptor<Eligibility>()
        verify(repository).save(captor.capture())
        Assertions.assertEquals(UUID.fromString(clientId), captor.firstValue.clientId)
        Assertions.assertTrue(captor.firstValue.eligible)
    }

    @Test
    fun `success - underage eligibility=false`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        val client = TestDataFactory.clientDetail(clientId = clientId, birthDateIso = TestDataFactory.birthDateMinor())
        val accounts = TestDataFactory.accountsResponse(clientId = clientId, accountsCount = 1)

        whenever(clientsClient.getClientDetails(eq(clientId),  eq(correlationId)))
            .thenReturn(ResponseEntity.ok(client))
        whenever(accountsClient.listGet(eq(clientId), eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.ok(accounts))

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isRight())
        val body = (res as Either.Right<GetEligibilityResponse>).value
        Assertions.assertFalse(body.eligible)
        Assertions.assertEquals(listOf(GetEligibilityResponse.Reasons.ADULT), body.reasons)
        verify(repository).save(any())
    }

    @Test
    fun `success - no account eligibility=false`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        val client = TestDataFactory.clientDetail(clientId = clientId, birthDateIso = TestDataFactory.birthDateAdult())
        val accounts = TestDataFactory.accountsResponse(clientId = clientId, accountsCount = 0)

        whenever(clientsClient.getClientDetails(eq(clientId),  eq(correlationId)))
            .thenReturn(ResponseEntity.ok(client))
        whenever(accountsClient.listGet(eq(clientId), eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.ok(accounts))

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isRight())
        val body = (res as Either.Right<GetEligibilityResponse>).value
        Assertions.assertFalse(body.eligible)
        Assertions.assertEquals(listOf(GetEligibilityResponse.Reasons.ACCOUNT), body.reasons)
        verify(repository).save(any())
    }

    @Test
    fun `success - no account & underage eligibility=false`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        val client = TestDataFactory.clientDetail(clientId = clientId, birthDateIso = TestDataFactory.birthDateMinor())
        val accounts = TestDataFactory.accountsResponse(clientId = clientId, accountsCount = 0)

        whenever(clientsClient.getClientDetails(eq(clientId), eq(correlationId)))
            .thenReturn(ResponseEntity.ok(client))
        whenever(accountsClient.listGet(eq(clientId),  eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.ok(accounts))

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isRight())
        val body = (res as Either.Right<GetEligibilityResponse>).value
        Assertions.assertFalse(body.eligible)
        Assertions.assertEquals(
            listOf(GetEligibilityResponse.Reasons.ACCOUNT, GetEligibilityResponse.Reasons.ADULT),
            body.reasons
        )
        verify(repository).save(any())
    }

    @Test
    fun `failure - clients API 401 Unauthorized`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        whenever(clientsClient.getClientDetails(eq(clientId), eq(correlationId)))
            .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())

        whenever(accountsClient.listGet(eq(clientId), eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.ok(null as GetAccountsResponse?))

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isLeft())
        Assertions.assertEquals(EligibilityFailure.Unauthorized, (res as Either.Left).value)
        verify(repository, never()).save(any())
    }

    @Test
    fun `failure - accounts API 400 BadRequest`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        whenever(clientsClient.getClientDetails(eq(clientId), eq(correlationId)))
            .thenReturn(ResponseEntity.ok(TestDataFactory.clientDetail(clientId)))
        whenever(accountsClient.listGet(eq(clientId), eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isLeft())
        Assertions.assertEquals(EligibilityFailure.BadRequest, (res as Either.Left).value)
        verify(repository, never()).save(any())
    }

    @Test
    fun `failure - null bodies BadRequest`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        whenever(clientsClient.getClientDetails(eq(clientId),  eq(correlationId)))
            .thenReturn(ResponseEntity.ok(null as GetClientDetailResponse?))
        whenever(accountsClient.listGet(eq(clientId), eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.ok(null as GetAccountsResponse?))

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isLeft())
        Assertions.assertEquals(EligibilityFailure.BadRequest, (res as Either.Left).value)
        verify(repository, never()).save(any())
    }

    @Test
    fun `clientId mismatch between services - returns BadRequest`() = runTest {
        val clientId = TestDataFactory.uuid()
        val otherId = TestDataFactory.uuid()
        val correlationId = UUID.randomUUID().toString()

        val client = TestDataFactory.clientDetail(clientId = clientId)
        val accounts = TestDataFactory.accountsResponse(clientId = otherId, accountsCount = 1)

        whenever(clientsClient.getClientDetails(eq(clientId), eq(correlationId)))
            .thenReturn(ResponseEntity.ok(client))
        whenever(accountsClient.listGet(eq(clientId), eq(correlationId), isNull()))
            .thenReturn(ResponseEntity.ok(accounts))

        val res = service.retrieveEligibility(correlationId, clientId)

        Assertions.assertTrue(res.isLeft())
        Assertions.assertEquals(EligibilityFailure.BadRequest, (res as Either.Left).value)
        verify(repository, never()).save(any())
    }
}
