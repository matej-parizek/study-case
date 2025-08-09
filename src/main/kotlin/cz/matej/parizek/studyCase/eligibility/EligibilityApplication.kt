package cz.matej.parizek.studyCase.eligibility

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@EnableAspectJAutoProxy
@SpringBootApplication
class EligibilityApplication

fun main(args: Array<String>) {
	runApplication<EligibilityApplication>(*args)
}
