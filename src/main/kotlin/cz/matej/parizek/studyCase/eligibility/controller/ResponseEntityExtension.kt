package cz.matej.parizek.studyCase.eligibility.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> ok(id: String, dto: T): ResponseEntity<T> =
    ResponseEntity.ok(dto).withCorrelationId(id)

inline fun <T> ok(id: String, block: () -> T): ResponseEntity<T> =
    ResponseEntity.ok(block()).withCorrelationId(id)

fun <T> accepted(id: String): ResponseEntity<T> =
    ResponseEntity.status(HttpStatus.ACCEPTED).build<T>().withCorrelationId(id)

fun <T> unauthorized(id: String): ResponseEntity<T> =
    ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<T>().withCorrelationId(id)

fun <T> badRequest(id: String): ResponseEntity<T> =
    ResponseEntity.status(HttpStatus.BAD_REQUEST).build<T>().withCorrelationId(id) // ← FIX

fun <T> forbidden(id: String): ResponseEntity<T> =
    ResponseEntity.status(HttpStatus.FORBIDDEN).build<T>().withCorrelationId(id)

fun <T> notFound(id: String): ResponseEntity<T> =
    ResponseEntity.status(HttpStatus.NOT_FOUND).build<T>().withCorrelationId(id)

fun <T> conflict(id: String): ResponseEntity<T> =
    ResponseEntity.status(HttpStatus.CONFLICT).build<T>().withCorrelationId(id)

fun <T> internalServerError(id: String): ResponseEntity<T> =
    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<T>().withCorrelationId(id)

fun <T> ResponseEntity<T>.withCorrelationId(id: String): ResponseEntity<T> =
    ResponseEntity(this.body, HttpHeaders().apply { add("correlation-id", id) }, this.statusCode)
