pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "spring-commons"

// Core module - common to both WebMVC and WebFlux
include("spring:core")

// Spring Boot WebMVC modules
include("spring:boot-webmvc:starter")
include("spring:boot-webmvc:rest-client")
include("spring:boot-webmvc:test-support")

// Spring Boot WebFlux modules
include("spring:boot-webflux:starter")
include("spring:boot-webflux:rest-client")
include("spring:boot-webflux:test-support")
