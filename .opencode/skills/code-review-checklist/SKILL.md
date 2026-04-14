---
name: code-review-checklist
description: Code review guidelines covering code quality, security, and best practices. Use before completing tasks, when reviewing code, or when quality checking existing code.
triggers:
  - auto
---

# Code Review Checklist

## When to Use This Skill

Use this skill when:
- **Before completing a task** - Run through the checklist before saying "done"
- **Reviewing code** - Systematically evaluate code quality
- **Self-review** - Check your own work before submitting
- **Security review** - Ensure no vulnerabilities exist
- **Performance review** - Check for optimization opportunities

This skill helps catch issues **before** they become problems.

## Quick Review Checklist

### Correctness
- [ ] Code does what it's supposed to do
- [ ] Edge cases handled
- [ ] Error handling in place
- [ ] No obvious bugs

### Security
- [ ] Input validated and sanitized
- [ ] No SQL/NoSQL injection vulnerabilities
- [ ] No XSS or CSRF vulnerabilities
- [ ] No hardcoded secrets or sensitive credentials
- [ ] **AI-Specific:** Protection against Prompt Injection (if applicable)
- [ ] **AI-Specific:** Outputs are sanitized before being used in critical sinks

### Performance
- [ ] No N+1 queries
- [ ] No unnecessary loops
- [ ] Appropriate caching
- [ ] Bundle size impact considered

### Code Quality
- [ ] Clear naming
- [ ] DRY - no duplicate code
- [ ] SOLID principles followed
- [ ] Appropriate abstraction level

### Testing
- [ ] Unit tests for new code
- [ ] Edge cases tested
- [ ] Tests readable and maintainable

### Documentation
- [ ] Complex logic commented
- [ ] Public APIs documented
- [ ] README updated if needed

## AI & LLM Review Patterns (2025)

### Logic & Hallucinations
- [ ] **Chain of Thought:** Does the logic follow a verifiable path?
- [ ] **Edge Cases:** Did the AI account for empty states, timeouts, and partial failures?
- [ ] **External State:** Is the code making safe assumptions about file systems or networks?

### Prompt Engineering Review
```markdown
// ❌ Vague prompt in code
const response = await ai.generate(userInput);

// ✅ Structured & Safe prompt
const response = await ai.generate({
  system: "You are a specialized parser...",
  input: sanitize(userInput),
  schema: ResponseSchema
});
```

## Anti-Patterns to Flag

```typescript
// ❌ Magic numbers
if (status === 3) { ... }

// ✅ Named constants
if (status === Status.ACTIVE) { ... }

// ❌ Deep nesting
if (a) { if (b) { if (c) { ... } } }

// ✅ Early returns
if (!a) return;
if (!b) return;
if (!c) return;
// do work

// ❌ Long functions (100+ lines)
// ✅ Small, focused functions

// ❌ any type
const data: any = ...

// ✅ Proper types
const data: UserData = ...
```

## Review Comments Guide

```
// Blocking issues use 🔴
🔴 BLOCKING: SQL injection vulnerability here

// Important suggestions use 🟡
🟡 SUGGESTION: Consider using useMemo for performance

// Minor nits use 🟢
🟢 NIT: Prefer const over let for immutable variable

// Questions use ❓
❓ QUESTION: What happens if user is null here?
```

## Language-Specific Checks

### Java (Spring Boot)
- [ ] Proper exception handling with custom exceptions
- [ ] Lombok annotations used correctly
- [ ] Constructor injection preferred
- [ ] OpenAPI annotations on endpoints
- [ ] Input validation with `@Valid`
- [ ] ResponseEntity with proper HTTP status codes

### TypeScript/JavaScript
- [ ] Proper type annotations (no `any`)
- [ ] Async/await used correctly
- [ ] Error boundaries for React components
- [ ] Proper hook dependencies in useEffect

## Pre-Completion Self-Review

Before marking task as complete, run through:

1. **Re-read the code** - Does it make sense?
2. **Check imports** - Any unused imports?
3. **Check tests** - Do they cover the changes?
4. **Check for secrets** - Any hardcoded values?
5. **Check edge cases** - Null, empty, error states?
