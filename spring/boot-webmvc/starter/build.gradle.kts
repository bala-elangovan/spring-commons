import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.github.balaelangovan.java-conventions")
}

description = "Spring Boot WebMVC starter with logging, security, and exception handling"

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
    // Depend on spring-core for common components
    api(project(":spring:core"))

    // Spring Boot WebMVC
    api(rootProject.libs.spring.boot.starter.web) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api(rootProject.libs.spring.boot.starter.validation)
    api(rootProject.libs.spring.data.commons)

    // Use Log4j2 instead of Logback
    api(rootProject.libs.spring.boot.starter.log4j2)

    // AspectJ weaver for @Aspect annotations
    api(rootProject.libs.aspectjweaver)

    // Spring Boot Autoconfigure
    implementation(rootProject.libs.spring.boot.autoconfigure)
    annotationProcessor(rootProject.libs.spring.boot.autoconfigure.processor)
    annotationProcessor(rootProject.libs.spring.boot.configuration.processor)

    // Apache Commons
    api(rootProject.libs.commons.lang3)

    // Kotlin
    api(rootProject.libs.kotlin.reflect)
    api(rootProject.libs.kotlin.stdlib)
    api(rootProject.libs.jackson.module.kotlin)

    // Swagger/OpenAPI for API documentation
    api(rootProject.libs.swagger.annotations)
    api(rootProject.libs.springdoc.openapi.webmvc)
}
