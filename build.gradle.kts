import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.6.0"
}

group = "cz.matej.parizek.studyCase"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
	maven("https://s01.oss.sonatype.org/content/repositories/releases/")
}

dependencies {
	// ===== SPRING =====
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-aop")


	// ===== KOTLIN + REACTOR + COROUTINES =====
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

	// ===== DATABASE =====
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.liquibase:liquibase-core")
	runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.postgresql:r2dbc-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	// ===== OPENAPI =====
	implementation("io.swagger.core.v3:swagger-annotations:2.2.22")

	// ===== ARROW =====
	implementation("io.arrow-kt:arrow-core:1.2.1")
	implementation("io.arrow-kt:arrow-fx-coroutines:1.2.1")

	// ===== DEV ONLY =====
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	// ===== TESTING =====
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
	testImplementation("org.springframework.boot:spring-boot-starter-aop")


	testImplementation("net.datafaker:datafaker:2.3.1")

	// Mockito
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
	testImplementation("org.mockito:mockito-inline:5.2.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")


	// H2 Database pro testy JDBC/Liquibase
	testRuntimeOnly("com.h2database:h2")
}

tasks.register<GenerateTask>("generateEligibilityApi") {
	generatorName.set("kotlin-spring")
	inputSpec.set("$rootDir/src/main/resources/api/eligibility-api-1.0.0.yml")
	outputDir.set("$rootDir/build/generated")
	apiPackage.set("cz.matej.parizek.eligibility.api")
	modelPackage.set("cz.matej.parizek.eligibility.model")
	invokerPackage.set("cz.matej.parizek.eligibility.invoker")
	configOptions.set(
		mapOf(
			"useSpringBoot3" to "true",
			"interfaceOnly" to "true",
			"reactive" to "true",
			"dateLibrary" to "java8",
			"useTags" to "true",
		)
	)
	skipValidateSpec.set(true)
	validateSpec.set(false)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

sourceSets["main"].java.srcDir("$rootDir/build/generated/src/main/kotlin")

tasks.named("compileKotlin") {
	dependsOn("generateEligibilityApi")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<Test>("test") {
	onlyIf { !project.hasProperty("skipTests") }
}
