import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.github.balaelangovan.java-conventions")
}

repositories {
    mavenCentral()
    mavenLocal()
}

description = "Core common components for both WebMVC and WebFlux"

// Apply Spring Boot's dependency management
apply(plugin = "io.spring.dependency-management")

val springBootVersion: String by project.extra {
    rootProject.libs.versions.spring.boot
        .get()
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
    }
}

dependencies {
    // Spring Data Commons (for PageableDto, PageImpl, Pageable)
    api(rootProject.libs.spring.data.commons)

    // Kotlin
    api(rootProject.libs.kotlin.reflect)
    api(rootProject.libs.kotlin.stdlib)

    // Jackson for JSON
    api(rootProject.libs.jackson.module.kotlin)
    api(rootProject.libs.jackson.databind)
    api(rootProject.libs.jackson.annotations)

    // Swagger/OpenAPI for API documentation
    compileOnly(rootProject.libs.swagger.annotations)

    // SLF4J for logging
    api(rootProject.libs.slf4j.api)

    // Log4j2 for logging implementation
    api(rootProject.libs.log4j2.api)
    api(rootProject.libs.log4j2.core)
    api(rootProject.libs.log4j2.slf4j2.impl)

    // Spring Framework (core only, not Boot)
    compileOnly(rootProject.libs.spring.context)
    compileOnly(rootProject.libs.spring.web)

    // Spring Boot (for metrics autoconfiguration)
    compileOnly(rootProject.libs.spring.boot.autoconfigure)
    compileOnly(rootProject.libs.spring.boot.starter.actuator)

    // Micrometer for metrics
    compileOnly(rootProject.libs.micrometer.core)

    // Validation API
    compileOnly(rootProject.libs.jakarta.validation.api)
}
