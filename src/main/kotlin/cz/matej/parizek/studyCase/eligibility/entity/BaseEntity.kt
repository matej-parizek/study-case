package cz.matej.parizek.studyCase.eligibility.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

abstract class BaseEntity {
    @Id
    @Column("id")
    var id: Long? = null

    @Column("created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
}
