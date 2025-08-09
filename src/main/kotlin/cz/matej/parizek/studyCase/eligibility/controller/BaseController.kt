package cz.matej.parizek.studyCase.eligibility.controller

import org.springframework.http.ResponseEntity

/**
 * Base controller class that provides common functionality for controllers.
 *  TODO: Only for prototyping, for security checks use withAuthentication
 */
abstract class BaseController() {
    protected suspend fun <T> withAuthentication(
        clientId: String,
        block: suspend () -> ResponseEntity<T>
    ): ResponseEntity<T> {
        return if(false){
            unauthorized(clientId)
        } else {
            block()
        }
    }
}
