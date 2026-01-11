import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.github.balaelangovan.java-conventions")
    id("io.github.balaelangovan.spring-test-conventions")
}

description = "REST client with OAuth support for Spring Boot WebMVC"

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
    api(project(":spring:boot-webmvc:starter"))

    // Spring Boot Web
    api(rootProject.libs.spring.boot.starter.web)
    implementation(rootProject.libs.spring.boot.autoconfigure)
    implementation(rootProject.libs.spring.boot.configuration.processor)

    // SLF4J
    api(rootProject.libs.slf4j.api)

    // Kotlin
    api(rootProject.libs.kotlin.reflect)
    api(rootProject.libs.kotlin.stdlib)
}
