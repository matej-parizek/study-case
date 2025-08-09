package cz.matej.parizek.studyCase.eligibility.service.auditLog

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse

interface IStoreAuditLog {
    suspend fun storeAuditLog(
        response: ServerHttpResponse,
        request: ServerHttpRequest
    ): Unit
}