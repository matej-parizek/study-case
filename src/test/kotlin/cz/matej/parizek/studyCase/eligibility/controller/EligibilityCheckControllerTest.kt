package cz.matej.parizek.studyCase.eligibility.controller

import arrow.core.left
import arrow.core.right
import cz.matej.parizek.eligibility.model.GetEligibilityResponse
import cz.matej.parizek.studyCase.eligibility.TestDataFactory
import cz.matej.parizek.studyCase.eligibility.service.eligibility.EligibilityFailure
import cz.matej.parizek.studyCase.eligibility.service.eligibility.IRetrieveEligibility
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EligibilityCheckControllerTest {

    private lateinit var client: WebTestClient
    private val service: IRetrieveEligibility = mock()

    @BeforeEach
    fun setUp() {
        val controller = EligibilityCheckController(service)
        client = WebTestClient
            .bindToController(controller)
            .build()
    }

    @Test
    fun `success - 200 OK`() = runTest {
        val clientId = TestDataFactory.uuid()
        val correlationId = TestDataFactory.uuid()
        val response = TestDataFactory.eligibilityResponse(eligible = true, reasons = null)

        whenever(service.retrieveEligibility(eq(correlationId), eq(clientId)))
            .thenReturn(response.right())

        val auth = UsernamePasswordAuthenticationToken("user", "TEST-KEY", emptyList())

        val body = client
            .mutateWith(mockAuthentication(auth))
            .get()
            .uri {
                it.path("/api/v1/eligibility")
                    .queryParam("clientId", clientId)
                    .queryParam("correlationId", correlationId)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody(GetEligibilityResponse::class.java)
            .returnResult()
            .responseBody

        verify(service).retrieveEligibility(eq(correlationId), eq(clientId))
        assertNotNull(body)
        assertTrue(body.eligible)
        assertNull(body.reasons)
    }
// TODO: Neni testovano neni implementovana security
//    @Test
//    fun `failure - 400 BadRequest`() = runTest {
//        val clientId = TestDataFactory.uuid()
//        val correlationId = TestDataFactory.uuid()
//
//        whenever(service.retrieveEligibility(eq(correlationId), eq(clientId), eq("TEST-KEY")))
//            .thenReturn(EligibilityFailure.BadRequest.left())
//
//        val auth = UsernamePasswordAuthenticationToken("user", "TEST-KEY", emptyList())
//
//        client
//            .mutateWith(mockAuthentication(auth))
//            .get()
//            .uri {
//                it.path("/api/v1/eligibility")
//                    .queryParam("clientId", clientId)
//                    .queryParam("correlationId", correlationId)
//                    .build()
//            }
//            .accept(MediaType.APPLICATION_JSON)
//            .exchange()
//            .expectStatus().isBadRequest
//            .expectBody().isEmpty
//    }
//
//    @Test
//    fun `unauthorized - 401 when api-key missing`() = runTest {
//        val clientId = TestDataFactory.uuid()
//        val correlationId = TestDataFactory.uuid()
//
//        client.get()
//            .uri {
//                it.path("/api/v1/eligibility")
//                    .queryParam("clientId", clientId)
//                    .queryParam("correlationId", correlationId)
//                    .build()
//            }
//            .accept(MediaType.APPLICATION_JSON)
//            .exchange()
//            .expectStatus().isUnauthorized
//            .expectBody().isEmpty
//    }
}
