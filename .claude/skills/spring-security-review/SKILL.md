---
name: spring-security-review
description: Reviews Spring Security authentication and authorization code for enterprise-level quality. Analyzes AuthenticationProvider, UserDetailsService, SecurityFilterChain, and related classes. Use when the user asks for a security review, asks how to improve authentication code, mentions Spring Security, or shares files like AuthenticationProvider, SecurityConfig, UserDetailsService, or JWT-related classes.
---

# Spring Security Enterprise Review

Reviews Spring Security code against production/enterprise standards.

## Checklist

### AuthenticationProvider
- [ ] Principal is `UserDetails` (not raw username string)
- [ ] Credentials are `null` in the returned token (credential erasure)
- [ ] Null-guard on `authentication.getCredentials()` before `.toString()`
- [ ] `UsernameNotFoundException` is caught and re-thrown as `BadCredentialsException` (prevent username enumeration)
- [ ] Logging with `@Slf4j` — failed attempts logged at WARN, successes at DEBUG (never log passwords)
- [ ] Class registered as Spring bean (`@Component` or `@Bean`)

### UserDetailsService
- [ ] `@Transactional(readOnly = true)` on `loadUserByUsername`
- [ ] Account state checks: disabled, locked, expired, credentials expired
- [ ] `UsernameNotFoundException` message does not reveal whether the user exists

### SecurityFilterChain
- [ ] CSRF configured intentionally (disabled for stateless APIs, enabled for form-login apps)
- [ ] Session fixation protection enabled
- [ ] HTTPS enforced in production profiles
- [ ] No in-memory users in production config

### General
- [ ] No hardcoded secrets or credentials
- [ ] Package structure: providers in `security.provider`, config in `security.config`
- [ ] `@EnableWebSecurity` present on config class

## Key Patterns

### Correct token construction
```java
// Principal = UserDetails, Credentials = null (erased)
return new UsernamePasswordAuthenticationToken(
    userDetails, null, userDetails.getAuthorities());
```

### Username enumeration defense
```java
try {
    userDetails = userDetailsService.loadUserByUsername(username);
} catch (UsernameNotFoundException ex) {
    log.warn("Authentication failed for unknown user: {}", username);
    throw new BadCredentialsException("Invalid credentials");
}
```

### Null-safe credential extraction
```java
Object credentials = authentication.getCredentials();
if (credentials == null) {
    throw new BadCredentialsException("Invalid credentials");
}
String password = credentials.toString();
```

## Severity Labels
- **CRITICAL** — Security vulnerability, must fix
- **MAJOR** — Breaks expected Spring Security contract or enterprise convention
- **MINOR** — Style, clarity, or maintainability concern
