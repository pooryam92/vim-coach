# Vim Coach — Agent Guide

**Stack:** IntelliJ `2026.1.3` · Kotlin JVM `21` · Gradle `9.3.1`

**Rules:**
- Always write tests and update docs when adding or changing behavior
- Verify: `./gradlew test && ./gradlew buildPlugin`

**Docs — read when:**
- Changing code structure or layers → [Architecture](docs/architecture/overview.md)
- Working on a specific feature → [Features](docs/features/)
- Adding/editing tips → [Tip authoring](docs/tips-authoring.md)
- Changing tip categories or JSON pipeline → [Tips pipeline](docs/tips-pipeline.md)
