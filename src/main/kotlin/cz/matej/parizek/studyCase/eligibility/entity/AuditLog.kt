package cz.matej.parizek.studyCase.eligibility.entity

import org.springframework.data.relational.core.mapping.Table

@Table("audit_log")
data class AuditLog(
    val method: String,
    val path: String,
    val query: String?,
    val status: Int?,
    val requestHeaders: String,
    val responseHeaders: String,
    val clientIp: String?,
    val traceId: String?,
) : BaseEntity()
