package cz.matej.parizek.studyCase.eligibility.repository

import cz.matej.parizek.studyCase.eligibility.entity.AuditLog
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : BaseRepository<AuditLog>