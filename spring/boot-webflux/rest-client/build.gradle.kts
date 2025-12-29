import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.github.balaelangovan.java-conventions")
}

description = "Reactive REST client (WebClient) with OAuth support for Spring Boot WebFlux"

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
    api(project(":spring:boot-webflux:starter"))

    // Spring Boot WebFlux
    api(rootProject.libs.spring.boot.starter.webflux)
    implementation(rootProject.libs.spring.boot.autoconfigure)
    implementation(rootProject.libs.spring.boot.configuration.processor)

    // SLF4J
    api(rootProject.libs.slf4j.api)

    // Kotlin
    api(rootProject.libs.kotlin.reflect)
    api(rootProject.libs.kotlin.stdlib)
    api(rootProject.libs.reactor.kotlin.extensions)
}
