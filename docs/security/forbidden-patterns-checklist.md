# Forbidden Patterns Checklist

## Spring Boot

- [ ] No field injection (`@Autowired` on fields)
- [ ] No controller business logic
- [ ] No JPA entities returned directly from controllers
- [ ] No swallowed exceptions
- [ ] No raw SQL without explicit review

## React

- [ ] No ad-hoc `fetch` in view components
- [ ] No untyped `any` API error handling
- [ ] No duplicated endpoint strings outside API layer
- [ ] No silent promise failures

## Security

- [ ] No plaintext logging of passwords
- [ ] No plaintext logging of tokens
- [ ] Correlation ID present in error responses
- [ ] Contract error codes align with specification
