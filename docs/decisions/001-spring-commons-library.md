# ADR 001: Spring Commons Library for Cross-Cutting Concerns

**Status:** Accepted
**Date:** 2025-11-02
**Deciders:** Engineering Team

## Context

When building multiple Spring Boot microservices, teams repeatedly implement the same cross-cutting concerns in each service: request/response logging, security headers, error handling, metrics collection, distributed tracing, and REST client configurations. This leads to:

- **Code duplication** across services
- **Inconsistent implementations** of common patterns
- **Maintenance burden** when fixing bugs or adding features
- **Slower development** as each team reinvents the wheel
- **Quality variance** as not all teams have the same expertise
- **Difficult standardization** across the platform

We needed a solution to centralize these common concerns while supporting both Spring WebMVC (servlet-based) and Spring WebFlux (reactive) applications, as our platform uses both programming models.

## Decision Drivers

- Eliminate code duplication across microservices
- Standardize cross-cutting concerns (logging, security, error handling)
- Accelerate development of new services
- Ensure consistent behavior across the platform
- Support both WebMVC and WebFlux stacks
- Make it easy to adopt and consume
- Enable platform-wide updates without touching every service

## Decision

We will create **Spring Commons**, a shared library that provides reusable components for cross-cutting concerns in Spring Boot applications.

### Architecture

The library is structured as:

```
spring-commons/
├── spring/core/              # Framework-agnostic shared code
│   ├── exceptions
│   ├── DTOs (PageableDto, ServiceError)
│   ├── constants
│   └── annotations
├── spring/boot-webmvc/       # Servlet-based (WebMVC) components
│   ├── filters
│   ├── exception handlers
│   ├── REST client base classes
│   └── security configurations
└── spring/boot-webflux/      # Reactive (WebFlux) components
    ├── web filters
    ├── exception handlers
    ├── reactive REST client base classes
    └── security configurations
```

### What Goes in Spring Commons

**Cross-cutting concerns:**
- Request/response logging with correlation IDs
- Global exception handling and error responses
- Security filters (authentication, authorization)
- Metrics and observability
- REST client abstractions with OAuth token management
- Common DTOs (pagination, errors)
- Standard HTTP headers and constants

**What stays in individual services:**
- Business logic
- Domain models
- Service-specific configurations
- Database schemas and repositories

## Positive Consequences

- **Zero duplication**: Logging, security, error handling implemented once
- **Consistency**: All services behave the same way
- **Faster development**: New services get best practices out of the box
- **Easier maintenance**: Fix once, benefit everywhere
- **Standardization**: Platform-wide patterns and conventions
- **Quality assurance**: Shared code gets more scrutiny and testing
- **Knowledge sharing**: Common codebase improves team learning
- **Compliance**: Easier to enforce security and audit requirements

## Negative Consequences

- **Coupling**: Services depend on library versions
- **Breaking changes**: Library updates may require service updates
- **Coordination**: Changes need to consider all consumers
- **Learning curve**: Teams need to understand library conventions
- **Abstraction overhead**: May not fit every edge case

## Alternatives Considered

### Option 1: Copy-Paste Common Code

Each service copies boilerplate code from a template.

**Pros:**
- No dependencies
- Full control per service

**Cons:**
- Massive duplication
- Inconsistent implementations
- Hard to update all services
- Bug fixes need to be applied everywhere

**Rejection Reason:** Duplication and inconsistency are unacceptable at scale

### Option 2: Code Generation

Use code generators to scaffold common code.

**Pros:**
- No runtime dependencies
- Customizable per service

**Cons:**
- Generated code still needs maintenance
- Updates require regeneration
- Versioning is complex
- Still creates duplication

**Rejection Reason:** Maintenance burden remains

### Option 3: Single Unified Module

One module with all WebMVC and WebFlux code together.

**Pros:**
- Single dependency

**Cons:**
- Dependency conflicts between servlet and reactive APIs
- Larger footprint
- Confusion about which APIs to use

**Rejection Reason:** Technical incompatibility between servlet and reactive stacks

## Implementation Strategy

### For Library Maintainers

1. Keep the API stable and backward compatible
2. Version using semantic versioning
3. Provide clear migration guides for breaking changes
4. Document all features with examples
5. Maintain separate modules for WebMVC and WebFlux

### For Service Teams

**WebMVC services:**
```kotlin
dependencies {
    implementation("io.github.balaelangovan:spring-boot-webmvc-starter:1.0.0")
}
```

**WebFlux services:**
```kotlin
dependencies {
    implementation("io.github.balaelangovan:spring-boot-webflux-starter:1.0.0")
}
```

## Success Criteria

This decision will be considered successful if:

- 80%+ of new services adopt the library
- Cross-cutting concern code is reduced by 70%+ per service
- Platform-wide updates can be rolled out in days, not months
- Security and compliance audits are simplified
- Developer satisfaction improves with faster service creation

## References

- [Spring Boot Starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html#using.build-systems.starters)
- [The Twelve-Factor App - Dependencies](https://12factor.net/dependencies)

## Notes

This library is the foundation for platform standardization. It doesn't aim to solve every problem but focuses on the 80% of common concerns that every service needs. Service-specific requirements should still be implemented in individual services.
