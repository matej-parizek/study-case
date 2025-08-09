package cz.matej.parizek.studyCase.eligibility.service.auditLog

import cz.matej.parizek.studyCase.eligibility.entity.AuditLog
import cz.matej.parizek.studyCase.eligibility.repository.AuditLogRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.RequestPath
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import java.net.InetSocketAddress
import java.net.URI

@ExtendWith(MockitoExtension::class)
class IStoreAuditLogImpUnitTest {

    private lateinit var repository: AuditLogRepository
    private lateinit var request: ServerHttpRequest
    private lateinit var response: ServerHttpResponse
    private lateinit var requestPath: RequestPath
    private lateinit var service: IStoreAuditLogImp

    @BeforeEach
    fun setUp() {
        repository = mock()
        request = mock()
        response = mock()
        requestPath = mock()
        service = IStoreAuditLogImp(repository)
    }

    @Test
    fun `storeAuditLog - happy path`() = runTest {
        val reqHeaders = HttpHeaders().apply {
            add("Content-Type", "application/json")
            add("X-Request-Id", "trace-123")
            add("Accept", "application/json")
        }
        whenever(request.method).thenReturn(org.springframework.http.HttpMethod.POST)
        whenever(request.uri).thenReturn(URI.create("http://localhost/api/v1/eligibility?clientId=42&active=true"))
        whenever(requestPath.value()).thenReturn("/api/v1/eligibility")
        whenever(request.path).thenReturn(requestPath)
        whenever(request.headers).thenReturn(reqHeaders)
        whenever(request.remoteAddress).thenReturn(InetSocketAddress("127.0.0.1", 54321))

        val respHeaders = HttpHeaders().apply {
            add("Content-Type", "application/json")
        }
        whenever(response.statusCode).thenReturn(HttpStatus.OK)
        whenever(response.headers).thenReturn(respHeaders)

        whenever(repository.save(any(AuditLog::class.java))).thenAnswer { it.arguments[0] }

        service.storeAuditLog(response, request)

        val captor = argumentCaptor<AuditLog>()
        verify(repository).save(captor.capture())

        val saved = captor.firstValue
        assertEquals("POST", saved.method)
        assertEquals("/api/v1/eligibility", saved.path)
        assertEquals("clientId=42&active=true", saved.query)
        assertEquals(200, saved.status)
        assertEquals(
            "Content-Type: application/json\nX-Request-Id: trace-123\nAccept: application/json",
            saved.requestHeaders
        )
        assertEquals("Content-Type: application/json", saved.responseHeaders)
        assertEquals("127.0.0.1", saved.clientIp)
        assertEquals("trace-123", saved.traceId)
    }

    @Test
    fun `storeAuditLog - missing optionals, trace id only in response`() = runTest {
        val reqHeaders = HttpHeaders().apply {
            add("Content-Type", "text/plain")
        }
        whenever(request.method).thenReturn(org.springframework.http.HttpMethod.GET)
        whenever(request.uri).thenReturn(URI.create("http://localhost/health"))
        whenever(requestPath.value()).thenReturn("/health")
        whenever(request.path).thenReturn(requestPath)
        whenever(request.headers).thenReturn(reqHeaders)
        whenever(request.remoteAddress).thenReturn(null)

        // response (no status, but has X-Request-Id)
        val respHeaders = HttpHeaders().apply {
            add("X-Request-Id", "resp-trace-999")
        }
        whenever(response.statusCode).thenReturn(null)
        whenever(response.headers).thenReturn(respHeaders)

        whenever(repository.save(any(AuditLog::class.java))).thenAnswer { it.arguments[0] }

        service.storeAuditLog(response, request)

        val captor = argumentCaptor<AuditLog>()
        verify(repository).save(captor.capture())

        val saved = captor.firstValue
        assertEquals("GET", saved.method)
        assertEquals("/health", saved.path)
        assertEquals(null, saved.query)
        assertEquals(null, saved.status)
        assertEquals("Content-Type: text/plain", saved.requestHeaders)
        assertEquals("X-Request-Id: resp-trace-999", saved.responseHeaders)
        assertEquals(null, saved.clientIp)
        assertEquals("resp-trace-999", saved.traceId)
    }
}
