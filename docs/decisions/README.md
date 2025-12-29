# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records (ADRs) for Spring Commons.

## What is an ADR?

An Architecture Decision Record (ADR) is a document that captures an important architectural decision made along with its context and consequences.

## When to Write an ADR

Create an ADR when you make a decision that:
- Affects the structure or behavior of the system
- Is difficult or expensive to reverse
- Impacts multiple modules or teams
- Requires explanation for future maintainers

## ADR Format

Each ADR should include:

1. **Title**: Clear, descriptive title
2. **Status**: Proposed, Accepted, Deprecated, Superseded
3. **Date**: When the decision was made
4. **Deciders**: Who made the decision
5. **Context**: The problem and constraints
6. **Decision**: What was decided
7. **Consequences**: Positive and negative outcomes
8. **Alternatives**: Other options considered

## Naming Convention

ADRs are numbered sequentially:
```
000-template.md
001-spring-commons-library.md
002-next-decision.md
...
```

## How to Create a New ADR

1. Copy `000-template.md`
2. Rename with next number and descriptive title
3. Fill out all sections
4. Include in your PR
5. Update this index

## ADR Index

| # | Title | Status | Date |
|---|-------|--------|------|
| [001](./001-spring-commons-library.md) | Spring Commons Library for Cross-Cutting Concerns | Accepted | 2025-11-02 |

## References

- [ADR GitHub Organization](https://adr.github.io/)
- [Michael Nygard's ADR Article](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
