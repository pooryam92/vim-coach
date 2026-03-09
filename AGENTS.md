# AGENTS.md

## Project
- IntelliJ plugin: Vim Coach
- Build tool: Gradle (`./gradlew`)
- Primary language: Kotlin
- Plugin descriptor: `src/main/resources/META-INF/plugin.xml`

## What Agents Need To Know
- Organize code by feature first.
- Main feature: `features/tips`.
- Keep shared code minimal and only in `core/shared` when reused across features.
- Keep IntelliJ entrypoints close to the owning feature:
  - actions: `features/<feature>/entrypoints/actions`
  - startup activities: `features/<feature>/entrypoints/startup`
- Keep UI/presentation separate from state, persistence, and source/integration code.

## Current Tips Feature Layout
- `core/shared/i18n`: shared bundle access
- `features/tips/domain`: tip models and result types
- `features/tips/state`: business services and settings/tip state logic
- `features/tips/state/store`: `PersistentStateComponent` stores
- `features/tips/source`: source selection and integrations
- `features/tips/application`: loading/update orchestration
- `features/tips/ui`: notifications, settings UI, and interactions
- `features/tips/entrypoints`: actions and startup activity

## IntelliJ Guardrails
- Preserve plugin behavior and extension registrations.
- Keep action IDs stable unless explicitly requested.
- Update `plugin.xml` class references if packages or classes move.
- Respect service scope and lifecycle boundaries.
- Do not move threading-sensitive behavior such as `Task.Backgroundable` or `invokeLater` without a clear reason.

Current service scopes:
- Application services: `VimTipStore`, `VimTipService`, `VimCoachSettingsStore`, `VimCoachSettingsService`, `TipSourceService`, `TipLoaderService`
- Project services: `PeriodicTipSchedulerService`

## Testing Guidance
- Use the lowest test layer that proves the behavior.
- Unit tests: pure logic, no IntelliJ fixture, suffix `UnitTest`, path `src/test/kotlin/.../features/<feature>/unit/...`
- Integration tests: IntelliJ/platform wiring, suffix `IntTest`, path `src/test/kotlin/.../features/<feature>/integration/...`
- UI tests: small headless UI-flow checks, suffix `UiTest`, path `src/test/kotlin/.../features/<feature>/ui/...`
- Do not use `BasePlatformTestCase` for pure logic.
- Do not hide slow platform tests inside `UnitTest` classes.

Useful test tasks:
1. `./gradlew test`
2. `./gradlew unitTest`
3. `./gradlew integrationTest`
4. `./gradlew uiTest`
5. `./gradlew buildPlugin`

## Working Rules
- Prefer moving existing files over rewriting logic.
- Keep files cohesive and small.
- Avoid adding dependencies unless necessary.
- Keep this as a single-module project unless explicitly asked otherwise.

## Verification
Run after meaningful refactors:
1. `./gradlew test`
2. `./gradlew buildPlugin`

If plugin registration changes were made, also verify:
- startup activity FQCN
- project service interface/implementation FQCN pairs
- action `class` FQCNs and unchanged action IDs
