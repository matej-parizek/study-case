package cz.matej.parizek.studyCase.eligibility.repository

import cz.matej.parizek.studyCase.eligibility.entity.BaseEntity
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> :  CoroutineCrudRepository<T, Long>{
}