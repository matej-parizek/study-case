package cz.matej.parizek.studyCase.eligibility.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.annotation.Immutable
import java.util.UUID

@Immutable
@Table("eligibility")
data class Eligibility(
    @Column("client_id")
    val clientId: UUID,
    @Column("eligible")
    val eligible: Boolean
) : BaseEntity()
