# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versions follow [SemVer](https://semver.org/).

## [1.0.0-RC7] — unreleased

### Added
- `pagbank.connect-timeout` (default `5s`) and `pagbank.read-timeout` (default `20s`), both
  `java.time.Duration`; `0` waits forever. The client had no timeouts at all, so a slow PagBank meant the
  caller's proxy gave up first — on Heroku, a 30s router cut that returned an opaque 503 while the
  subscription was in fact created.
- `PagBankException.Timeout(phase, method, path, cause)` with `TimeoutPhase.CONNECT` / `READ`. `CONNECT`
  never reached PagBank and is safe to retry; `READ` was sent and its **outcome is unknown** — re-read the
  resource instead of reporting failure. Previously a timeout surfaced as Spring's `ResourceAccessException`,
  indistinguishable from any other transport error. **Breaking:** exhaustive `when` over `PagBankException`
  needs the new branch.
- `META-INF/spring-configuration-metadata.json` now describes every `pagbank.*` property (type, default,
  description) for IDE completion. The file shipped with `"properties": []` up to RC6: the Spring
  configuration processor ran through kapt, which does not expose `src/main/resources` to it, so neither
  the generated nor an `additional-*` file ever described a property. It is now versioned and kept in sync
  by a test.

### Changed
- New runtime dependency `org.springframework.boot:spring-boot-http-client` (version from the Boot BOM):
  `spring-boot-starter-web` does not bring it, and it is what lets the starter apply timeouts to whichever
  HTTP client is on the classpath.
- A call that previously hung forever now fails once the read timeout elapses. Applications relying on a
  deliberately slow endpoint should raise `pagbank.read-timeout` or set it to `0`.

## [1.0.0-RC6] — 2026-09-17

### Changed
- `WebhookPayload.resource` is now `Map<String, Any?>` — values are nullable at every level, which is
  what the PagBank sandbox sends (`"coupon": null`, `"trial": null`, `"holder": {"phone": null}` …).
  Source-compatible for consumers reading `resource["id"] as? String`. Before RC5 such payloads failed
  with `Invalid null value encountered for property "resource"` (the Kotlin module's strict null check
  on the map contents); RC5's dedicated deserializer already accepted them, and this release pins that
  behaviour with a test on a real recurrence payload and makes the type honest.

## [1.0.0-RC5] — 2026-09-16

### Fixed
- `WebhookPayload.resource` now accepts the three shapes the PagBank sandbox sends — a JSON object
  (documented), a **string containing the serialized object**, and `null` (→ empty map). Previously the
  string form failed with Jackson's generic `Cannot deserialize value of type LinkedHashMap from String
  value` and the `null` form failed on the non-null map, so consumers dropped the event. Any other type
  (number, array, boolean, or a string without a JSON object inside) is rejected with a message naming
  the field. `WebhookPayload` also ignores unknown top-level fields regardless of the mapper in use.

## [1.0.0-RC4] — 2026-09-13

### Fixed
- `CardRequest.Encrypted` gained `securityCode`; the Subscriptions API rejects an encrypted card without
  `card.security_code` (422) even though the blob already contains the CVV. `toString()` of `Plain` and
  `Encrypted` no longer prints card secrets.

### Changed
- Kotlin 2.3.21 → 2.4.20 (consumers need a Kotlin compiler ≥ 2.3); Gradle wrapper 8.14 → 9.7.1.

## [1.0.0-RC3] — 2026-09-11

### Changed
- Spring Boot 4.0.4 → 4.1.1 and Kotlin 2.1.20 → 2.3.21. Clears the CVE-2026-41001 flag on Maven Central
  (embedded Artemis; the starter never pulled it). Minimum Spring Boot for consumers is now 4.1.

## [1.0.0-RC2] — 2026-09-11

### Fixed
- The internal snake_case `JsonMapper` is no longer a Spring bean. It was replacing Spring Boot's
  application-wide mapper (`@ConditionalOnMissingBean`), silently changing consumers' JSON contracts.
  **Breaking:** the `pagBankObjectMapper` bean no longer exists.
- Request/response logging (`pagbank.log-requests`) masks card data, CPF/CNPJ, e-mail and phone numbers.
- `PagBankProperties.toString()` hides the token.

### Added
- Opt-in webhook signature verification: `pagbank.webhook.verify-signature`, `PagBankWebhookVerifier`,
  `PagBankWebhookParser.parseVerified(ByteArray, String?)`, `PagBankException.InvalidSignature`.
- `pagbank.base-url` (overrides the environment URL) and `PagBankRestClientCustomizer`.
- `PagBankException.RateLimited` for HTTP 429 with `Retry-After`. **Breaking:** exhaustive `when` over
  `PagBankException` needs the new branches.
- Release process: version lives in `gradle.properties`; the release workflow refuses a tag that does
  not match it.

## [1.0.0-RC1] — 2026-05-05

Initial release candidate.
