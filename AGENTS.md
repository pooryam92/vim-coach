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
- `features/tips/application`: loading, scheduling, and notification orchestration
- `features/tips/ui`: notification presentation and settings UI
- `features/tips/entrypoints`: actions and startup activity

## Docs
- Keep user-facing tip authoring docs in `docs/`
- Current docs:
- `docs/vim_tips_reference.md`: tip authoring reference, categories, resources, format, and review rules.
- `docs/category_count_plan.md`: category-count persistence and settings-plan notes.
- Planning notes and one-off design docs for the tips feature should also live in `docs/`.
- When changing the tip taxonomy or authoring rules, update the matching docs in `docs/` in the same pass.

## IntelliJ Guardrails
- Preserve plugin behavior and extension registrations.
- Keep action IDs stable unless explicitly requested.
- Update `plugin.xml` class references if packages or classes move.
- Respect service scope and lifecycle boundaries.
- Do not move threading-sensitive behavior such as `Task.Backgroundable` or `invokeLater` without a clear reason.

Current service scopes:
- Application services: `VimTipStore`, `VimTipService`, `VimCoachSettingsStore`, `VimCoachSettingsService`, `TipSourceService`, `TipLoaderService`
- Project services: `PeriodicTipSchedulerService`, `TipNotificationService`

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

## Clean Architecture
- Keep layers pointed inward: UI/entrypoints depend on application/state/domain, not the other way around.
- Keep UI/presentation thin. Put derivation, normalization, filtering, and decision logic in services or domain types instead of UI classes.
- Keep stores focused on persistence and simple state snapshots. Do not move business logic into `PersistentStateComponent` implementations.
- Prefer one clear owner for a piece of logic or state. Avoid duplicating rules across UI, services, and stores.
- Minimize coupling between parts of the system. If a class starts depending on multiple services or unrelated concerns, consider introducing a narrower feature-facing service or query model.
- Prefer simple data crossing boundaries. Avoid leaking UI types into state/domain layers and avoid persistence concerns in UI/application code.

## Clean Code
- Favor small, cohesive methods with explicit names over long methods with mixed responsibilities.
- Keep control flow flat when possible. Reduce duplicated branching and repeated state lookups.
- Avoid parallel mutable state when a single source of truth is enough.
- Make migration, fallback, and repair paths explicit and rare.
- Keep naming concrete and intention-revealing. Prefer names that explain why a path exists, not just what it does.
- Before finishing, do a cleanup pass: remove incidental complexity, tighten APIs, and simplify any code that feels harder to read than the behavior warrants.

## Verification
Run after meaningful refactors:
1. `./gradlew test`
2. `./gradlew buildPlugin`

If plugin registration changes were made, also verify:
- startup activity FQCN
- project service interface/implementation FQCN pairs
- action `class` FQCNs and unchanged action IDs
