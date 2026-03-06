# Testing Strategy

This plugin uses a simple 3-layer test pyramid with naming and folder conventions.

## Test Pyramid

1. Unit tests (`*UnitTest`)
- Purpose: fast checks for pure logic.
- No IntelliJ test fixture bootstrap.
- Examples in this repo:
  - JSON parsing and normalization
  - Source mode routing
  - File source behavior
  - Notification content formatting

2. Integration tests (`*IntTest`)
- Purpose: plugin behavior that needs IntelliJ Platform test infrastructure.
- Uses `BasePlatformTestCase` when project/services/plugin wiring are involved.
- Examples in this repo:
  - Project service state (`VimTipService`)
  - Loader + source + service interactions (`TipLoaderService`)
  - Service registration wiring from `plugin.xml`

3. UI-level tests (`*UiTest`)
- Purpose: small, high-value UI flow checks that are reliable in CI.
- For this project, these are headless UI-flow tests (notification action wiring and show flow), not full GUI robot-driven scenarios.
- Full end-to-end GUI automation is available via `runIdeForUiTests` + Robot Server, but is intentionally kept minimal because it is slower and more brittle.

## Directory and Naming Conventions

- Unit tests:
  - `src/test/kotlin/.../features/<feature>/unit/...`
  - Class suffix: `UnitTest`
- Integration tests:
  - `src/test/kotlin/.../features/<feature>/integration/...`
  - Class suffix: `IntTest`
- UI tests:
  - `src/test/kotlin/.../features/<feature>/ui/...`
  - Class suffix: `UiTest`

These suffixes drive Gradle test-layer tasks.

## Gradle Tasks

- Run unit tests only:
```bash
./gradlew unitTest
```

- Run integration tests only:
```bash
./gradlew integrationTest
```

- Run UI-level tests only:
```bash
./gradlew uiTest
```

Run one layer command at a time. Layer wrappers route through the main `test` task and apply class filters only for direct single-layer invocations.

- Default local/CI checks (`check`) run `test`, which includes unit + integration tests and excludes `*UiTest` by default.
```bash
./gradlew check
```

- UI workflow (`.github/workflows/run-ui-tests.yml`) runs `./gradlew uiTest`.

## How To Add New Tests

1. Choose the lowest layer that can prove the behavior.
2. Put the file under the matching `unit` / `integration` / `ui` directory.
3. Use the required class suffix (`UnitTest`, `IntTest`, `UiTest`).
4. Keep assertions explicit so failures are easy to diagnose.
5. Prefer small fake objects in test files over large test utility frameworks.

## What To Avoid

- Do not use `BasePlatformTestCase` for pure logic.
- Do not put slow platform tests in `UnitTest` classes.
- Do not over-mock IntelliJ APIs; use integration tests when real platform behavior matters.
- Do not add large helper abstractions unless duplication becomes real and repetitive.
