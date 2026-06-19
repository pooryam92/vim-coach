# Vim Coach — Agent Guide

**Stack:** IntelliJ `2026.1.3` · Kotlin JVM `21` · Gradle `9.3.1`

**Rules**
- Always write or update tests when adding or changing behavior.
- Always update relevant docs when adding or changing behavior.
- Comment sparingly. Prefer self-explanatory names over comments. Add one only when it explains the non-obvious *why* — intent, a design decision, or a gotcha — never to restate what the code already says.
- Use logging when it adds value for debugging, observability, or diagnosing user/plugin issues.
- Verify changes with: `./gradlew test && ./gradlew buildPlugin`



**Docs — read when:**
- Changing code structure or layers → [Architecture](docs/architecture/overview.md)
- Working on a specific feature → [Features](docs/features/)
- Adding/editing/reviewing tips → the `tips-maintain` skill ([.claude/skills/tips-maintain/SKILL.md](.claude/skills/tips-maintain/SKILL.md))
- Tip build pipeline (generator, CI, runtime fetch) → [Tips pipeline](docs/tips/tips-pipeline.md)
