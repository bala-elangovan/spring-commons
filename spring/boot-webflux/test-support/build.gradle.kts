import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.github.balaelangovan.spring-test-conventions")
}

description = "Test support utilities for Spring Boot WebFlux applications"

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

    // Spring Boot Test
    api(rootProject.libs.spring.boot.webflux.test)
    api(rootProject.libs.spring.boot.starter.webflux)
    api(rootProject.libs.reactor.test)

    // Spring Security for reactive test configuration
    api(rootProject.libs.spring.boot.starter.security)
    api(rootProject.libs.spring.security.test)

    // Kotlin
    api(rootProject.libs.kotlin.reflect)
    api(rootProject.libs.kotlin.stdlib)
}
