# Contributing to Spring Commons

Thank you for considering contributing to Spring Commons! This document provides guidelines and instructions for contributing to this project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Code Style](#code-style)
- [Testing](#testing)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Architecture Decision Records](#architecture-decision-records)

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help create a welcoming environment

## Getting Started

### Prerequisites

- Java 21+
- Gradle 9.2.0+
- Git
- IDE with Kotlin support (IntelliJ IDEA recommended)
- Pre-commit (optional but recommended): `pip install pre-commit`

### Fork and Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/spring-commons.git
cd spring-commons
```

## Development Setup

### 1. Install Dependencies

```bash
# Install pre-commit hooks
make pre-commit-install

# Or manually
pre-commit install
pre-commit install --hook-type commit-msg
```

### 2. Build the Project

```bash
# Build all modules
make build

# Or using Gradle directly
./gradlew build
```

### 3. Run Tests

```bash
make test
```

### 4. Verify Setup

```bash
# Check all modules are recognized
make check-modules

# Run pre-commit checks
make pre-commit-run
```

## Making Changes

### Branching Strategy

We follow a simplified Git Flow:

- `main` - Production-ready code
- `feature/*` - New features
- `fix/*` - Bug fixes
- `docs/*` - Documentation changes
- `refactor/*` - Code refactoring

```bash
# Create a feature branch
git checkout -b feature/add-new-validator

# Create a fix branch
git checkout -b fix/authorization-header-parsing
```

### Before You Code

1. **Check existing issues** - Search for related issues or discussions
2. **Create an issue** - If one doesn't exist, create one describing your change
3. **Discuss approach** - For large changes, discuss the approach first
4. **Check ADRs** - Review [Architecture Decision Records](./docs/decisions) for relevant decisions

## Code Style

### Formatting

We use **Spotless** for automated code formatting with ktlint for Kotlin.

Pre-commit hooks automatically run Spotless and ktlint on every commit, so formatting issues are caught and fixed automatically.

```bash
# Format code manually (optional - pre-commit hooks do this automatically)
make format

# Check formatting
make check-format
```

### Code Guidelines

#### Kotlin

```kotlin
// ✅ Good - Constructor injection
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    fun createUser(user: User): User {
        // Implementation
    }
}

// ❌ Bad - Field injection
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository
}

// ✅ Good - Explicit return types for public functions
fun getUser(id: String): User {
    return userRepository.findById(id)
        ?: throw ResourceNotFoundException("User not found")
}

// ✅ Good - Use meaningful names
fun calculateTotalAmount(items: List<Item>): BigDecimal

// ❌ Bad - Vague names
fun calc(items: List<Item>): BigDecimal
```

#### Exception Handling

```kotlin
// ✅ Good - Use specific exceptions
throw ResourceNotFoundException("User not found: $userId")
throw ValidationException("Email format is invalid")

// ❌ Bad - Generic exceptions
throw RuntimeException("Error")
```

#### Reactive Code (WebFlux)

```kotlin
// ✅ Good - Non-blocking
fun getUsers(): Flux<User> {
    return userRepository.findAll()
}

// ❌ Bad - Blocking in reactive chain
fun getUsers(): List<User> {
    return userRepository.findAll().collectList().block()!!
}

// ✅ Good - Proper error handling
fun getUser(id: String): Mono<User> {
    return userRepository.findById(id)
        .switchIfEmpty(Mono.error(ResourceNotFoundException("User not found")))
}
```

### EditorConfig

Your IDE should automatically use the `.editorconfig` settings:

```ini
[*.{kt,kts}]
indent_size = 4
indent_style = space
max_line_length = 120
```

## Testing

### Test Structure

```kotlin
@Test
fun `should return user when user exists`() {
    // Given
    val userId = "123"
    val expectedUser = User(id = userId, name = "John")
    whenever(userRepository.findById(userId)).thenReturn(expectedUser)

    // When
    val result = userService.getUser(userId)

    // Then
    assertThat(result).isEqualTo(expectedUser)
    verify(userRepository).findById(userId)
}
```

### Running Tests

```bash
# Run all tests
make test

# Run tests for specific module
./gradlew :spring:core:test

# Run with coverage
./gradlew test jacocoTestReport
```

### Test Guidelines

- Write tests for all new features
- Maintain or improve code coverage
- Use descriptive test names (backticks for readability)
- Follow Given-When-Then pattern
- Mock external dependencies
- Test both success and failure scenarios

## Commit Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic changes)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```bash
# Feature
git commit -m "feat(webmvc): add request timeout configuration"

# Bug fix
git commit -m "fix(authorization): handle null user groups header"

# Documentation
git commit -m "docs: update REST client OAuth configuration examples"

# Breaking change
git commit -m "feat(core)!: change PageableDto default page size to 10

BREAKING CHANGE: Default page size changed from 20 to 10"
```

### Commit Message Validation

Pre-commit hooks automatically validate commit messages:

```bash
# This will be rejected
git commit -m "updated code"

# This will be accepted
git commit -m "feat(core): add new validation exception"
```

## Pull Request Process

### 1. Prepare Your PR

```bash
# Update from main
git checkout main
git pull origin main

# Rebase your branch
git checkout feature/your-feature
git rebase main

# Format code
make format

# Run all checks
make pre-commit-run

# Run tests
make test
```

### 2. Create Pull Request

1. Push your branch to your fork
2. Go to the original repository
3. Click "New Pull Request"
4. Select your branch
5. Fill out the PR template

### PR Template

```markdown
## Description
Brief description of the changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issue
Closes #123

## How Has This Been Tested?
- [ ] Unit tests
- [ ] Integration tests
- [ ] Manual testing

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests passing
- [ ] No new warnings
```

### 3. PR Review Process

1. **Automated Checks** - CI/CD pipeline runs automatically
2. **Code Review** - At least one approval required
3. **Address Feedback** - Make requested changes
4. **Merge** - Maintainer will merge once approved

### PR Guidelines

- Keep PRs focused and reasonably sized
- Reference related issues
- Update documentation if needed
- Add tests for new functionality
- Ensure all CI checks pass
- Respond to review comments promptly

## Architecture Decision Records

When making significant architectural decisions:

1. Create an ADR in `docs/decisions/`
2. Use the template: `docs/decisions/000-template.md`
3. Number sequentially: `001-`, `002-`, etc.
4. Include it in your PR

See [ADR documentation](./docs/decisions/README.md) for details.

## Module-Specific Guidelines

### spring/core

- Keep framework-agnostic
- No WebMVC or WebFlux specific code
- Focus on shared components

### spring/boot-webmvc

- Use servlet-based APIs
- No reactive types (Mono/Flux)
- Use `HttpServletRequest`/`HttpServletResponse`

### spring/boot-webflux

- Use reactive types (Mono/Flux)
- Never block (no `.block()` calls)
- Use `ServerWebExchange`

## Getting Help

- **Issues**: Search existing issues or create a new one
- **Discussions**: Use GitHub Discussions for questions
- **Documentation**: Check the [docs](./docs) folder
- **Examples**: Look at existing code for patterns

## Release Process

Releases are managed by maintainers:

1. Version bump in `gradle.properties`
2. Update CHANGELOG.md
3. Create release tag
4. Publish to Maven Central

## Recognition

Contributors will be recognized in:
- CHANGELOG.md
- GitHub releases
- README.md contributors section

## License

By contributing, you agree that your contributions will be licensed under the project's license.

---

Thank you for contributing to Spring Commons!
