package cz.matej.parizek.studyCase.eligibility.service.auditLog

import cz.matej.parizek.studyCase.eligibility.entity.AuditLog
import cz.matej.parizek.studyCase.eligibility.repository.AuditLogRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Service

@Service
class IStoreAuditLogImp(
    private val repository: AuditLogRepository
) : IStoreAuditLog {

    override suspend fun storeAuditLog(
        response: ServerHttpResponse,
        request: ServerHttpRequest
    ) {
        val method = request.method.name()
        val path = request.path.value()
        val query = request.uri.rawQuery
        val status = response.statusCode?.value()
        val reqHeaders = headersToString(request.headers)
        val respHeaders = headersToString(response.headers)
        val clientIp = request.remoteAddress?.address?.hostAddress
        val traceId = request.headers.getFirst("X-Request-Id")
            ?: response.headers.getFirst("X-Request-Id")

        val entity = AuditLog(
            method = method,
            path = path,
            query = query,
            status = status,
            requestHeaders = reqHeaders,
            responseHeaders = respHeaders,
            clientIp = clientIp,
            traceId = traceId,
        )

        repository.save(entity)
    }

    private fun headersToString(h: HttpHeaders): String =
        h.entries.joinToString("\n") { (k, v) -> "$k: ${v.joinToString(", ")}" }
}
