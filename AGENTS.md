# Vim Coach — Agent Guide

**Stack:** IntelliJ `2026.1.3` · Kotlin JVM `21` · Gradle `9.3.1`

**Rules**
- Always write or update tests when adding or changing behavior.
- Always update relevant docs when adding or changing behavior.
  Add concise class-level docstrings and targeted inline comments where they explain intent, non-obvious logic, or important design decisions. Do not add comments that merely describe obvious code behavior.- Use logging when it adds value for debugging, observability, or diagnosing user/plugin issues.
- Keep comments and logs useful; avoid restating obvious code behavior.
- Verify changes with: `./gradlew test && ./gradlew buildPlugin`



**Docs — read when:**
- Changing code structure or layers → [Architecture](docs/architecture/overview.md)
- Working on a specific feature → [Features](docs/features/)
- Adding/editing tips → [Tip authoring](docs/tips-authoring.md)
- Changing tip categories or JSON pipeline → [Tips pipeline](docs/tips-pipeline.md)
