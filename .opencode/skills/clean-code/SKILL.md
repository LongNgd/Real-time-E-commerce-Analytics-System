---
name: clean-code
description: Pragmatic coding standards - concise, direct, no over-engineering, no unnecessary comments. Use when writing new code, refactoring existing code, or reviewing code quality.
triggers:
  - auto
---

# Clean Code - Pragmatic AI Coding Standards

> **CRITICAL SKILL** - Be **concise, direct, and solution-focused**.

## When to Use This Skill

Use this skill when:
- **Writing new code** - Follow clean code principles from the start
- **Refactoring** - Improve existing code quality
- **Code review** - Evaluate code against standards
- **Adding features** - Ensure new code follows conventions
- **Bug fixing** - Clean up while fixing issues

This skill applies to **all programming languages** with specific guidelines for Java and TypeScript.

## Core Principles

| Principle | Rule |
|-----------|------|
| **SRP** | Single Responsibility - each function/class does ONE thing |
| **DRY** | Don't Repeat Yourself - extract duplicates, reuse |
| **KISS** | Keep It Simple - simplest solution that works |
| **YAGNI** | You Aren't Gonna Need It - don't build unused features |
| **Boy Scout** | Leave code cleaner than you found it |

## Naming Rules

| Element | Convention |
|---------|------------|
| **Variables** | Reveal intent: `userCount` not `n` |
| **Functions** | Verb + noun: `getUserById()` not `user()` |
| **Booleans** | Question form: `isActive`, `hasPermission`, `canEdit` |
| **Constants** | SCREAMING_SNAKE: `MAX_RETRY_COUNT` |

> **Rule:** If you need a comment to explain a name, rename it.

## Function Rules

| Rule | Description |
|------|-------------|
| **Small** | Max 20 lines, ideally 5-10 |
| **One Thing** | Does one thing, does it well |
| **One Level** | One level of abstraction per function |
| **Few Args** | Max 3 arguments, prefer 0-2 |
| **No Side Effects** | Don't mutate inputs unexpectedly |

## Code Structure

| Pattern | Apply |
|---------|-------|
| **Guard Clauses** | Early returns for edge cases |
| **Flat > Nested** | Avoid deep nesting (max 2 levels) |
| **Composition** | Small functions composed together |
| **Colocation** | Keep related code close |

## AI Coding Style

| Situation | Action |
|-----------|--------|
| User asks for feature | Write it directly |
| User reports bug | Fix it, don't explain |
| No clear requirement | Ask, don't assume |

## Anti-Patterns (DON'T)

| Pattern | Fix |
|---------|-----|
| Comment every line | Delete obvious comments |
| Helper for one-liner | Inline the code |
| Factory for 2 objects | Direct instantiation |
| utils.ts with 1 function | Put code where used |
| "First we import..." | Just write code |
| Deep nesting | Guard clauses |
| Magic numbers | Named constants |
| God functions | Split by responsibility |

## Before Editing ANY File (THINK FIRST!)

**Before changing a file, ask yourself:**

| Question | Why |
|----------|-----|
| **What imports this file?** | They might break |
| **What does this file import?** | Interface changes |
| **What tests cover this?** | Tests might fail |
| **Is this a shared component?** | Multiple places affected |

**Quick Check:**
```
File to edit: UserService.java
└── Who imports this? → UserController.java, AuthController.java
└── Do they need changes too? → Check function signatures
```

> **Rule:** Edit the file + all dependent files in the SAME task.
> **Never leave broken imports or missing updates.**

## Summary

| Do | Don't |
|----|-------|
| Write code directly | Write tutorials |
| Let code self-document | Add obvious comments |
| Fix bugs immediately | Explain the fix first |
| Inline small things | Create unnecessary files |
| Name things clearly | Use abbreviations |
| Keep functions small | Write 100+ line functions |

> **Remember: The user wants working code, not a programming lesson.**

## Self-Check Before Completing (MANDATORY)

**Before saying "task complete", verify:**

| Check | Question |
|-------|----------|
| **Goal met?** | Did I do exactly what user asked? |
| **Files edited?** | Did I modify all necessary files? |
| **Code works?** | Did I test/verify the change? |
| **No errors?** | Lint and typecheck pass? |
| **Nothing forgotten?** | Any edge cases missed? |

> **Rule:** If ANY check fails, fix it before completing.

## Language-Specific Guidelines

### Java (Spring Boot)
- Use Lombok: `@Data`, `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`
- Constructor injection with `final` fields
- Annotations order: `@Slf4j` → `@RestController` → `@RequestMapping` → `@RequiredArgsConstructor`
- Max line length: ~120 characters
- Indent: 4 spaces

### TypeScript/JavaScript
- Use `const` by default, `let` when reassignment needed
- Prefer arrow functions for callbacks
- Use destructuring for props/parameters
- Max line length: 100 characters
- Indent: 2 spaces

### General
- Opening brace on same line: `public void method() {`
- One blank line between methods
- No blank lines between import groups
