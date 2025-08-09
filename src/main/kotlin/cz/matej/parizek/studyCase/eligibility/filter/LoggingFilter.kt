package cz.matej.parizek.studyCase.eligibility.filter

import cz.matej.parizek.studyCase.eligibility.service.auditLog.IStoreAuditLogImp
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class LoggingFilter(
    private val service: IStoreAuditLogImp
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val req = exchange.request
        val resp = exchange.response

        return chain.filter(exchange).then(
            mono {
                runCatching {
                    service.storeAuditLog(response = resp, request = req)
                }.onFailure { ex ->
                    org.slf4j.LoggerFactory.getLogger(LoggingFilter::class.java)
                        .warn("Audit logging failed: {}", ex.message)
                }
            }
        ).then()
    }
}
