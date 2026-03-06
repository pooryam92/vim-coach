# AGENTS.md

## Project Overview
- IntelliJ plugin: Vim Coach
- Build tool: Gradle (`./gradlew`)
- Primary language: Kotlin
- Plugin descriptor: `src/main/resources/META-INF/plugin.xml`

## Architecture Conventions
- Organize code by feature first.
- Main feature in this repo: `features/tips`.
- Keep shared code minimal and in `core/shared` only when reused across features.
- Keep IntelliJ entrypoints close to feature ownership:
  - actions in `features/<feature>/entrypoints/actions`
  - startup activities in `features/<feature>/entrypoints/startup`
- Keep UI/presentation separate from persistent state and source/integration code.

## IntelliJ-Specific Guardrails
- Preserve plugin behavior and extension registrations.
- Keep action IDs stable unless explicitly requested.
- Update `plugin.xml` class references if packages/classes move.
- Respect service scope (project/application) and lifecycle boundaries.
- Do not move threading-sensitive behavior (`Task.Backgroundable`, `invokeLater`) without clear reason.

## Working Rules
- Prefer moving existing files over rewriting logic.
- Keep files cohesive and small.
- Avoid adding dependencies unless necessary.
- Keep this as a single-module project unless explicitly asked otherwise.

## Verification
Run these after meaningful refactors:
1. `./gradlew test`
2. `./gradlew buildPlugin`

If plugin registration changes were made, also verify:
- startup activity FQCN
- project service interface/implementation FQCN pairs
- action `class` FQCNs and unchanged action IDs
