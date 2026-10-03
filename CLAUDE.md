# Vim Coach — Agent Guide

**Stack:** IntelliJ `2026.1.3` · Kotlin JVM `21` · Gradle `9.8.0`

**Rules**
- Always write or update tests when adding or changing behavior.
- Always update relevant docs when adding or changing behavior.
- Comment sparingly. Prefer self-explanatory names over comments. Add one only when it explains the non-obvious *why* — intent, a design decision, or a gotcha — never to restate what the code already says.
- Tag code, tests and doc sections that only bridge an upgrade from an older version with `TODO(X.Y.Z upgrade bridge)`, X.Y.Z being the release that adds it. Starting the release after X.Y.Z, grep the tag and delete everything it marks.
- Use logging when it adds value for debugging, observability, or diagnosing user/plugin issues.
- Verify changes with: `./gradlew test && ./gradlew buildPlugin`



**Docs — read when:**
- Changing code structure or layers → [Architecture](docs/architecture/overview.md)
- Working on a specific feature → [Features](docs/features)
- Adding/editing/reviewing tips → the `tips-maintain` skill ([.claude/skills/tips-maintain/SKILL.md](.claude/skills/tips-maintain/SKILL.md))
- Tip build pipeline (generator, CI, runtime fetch) → [Tips pipeline](docs/tips/tips-pipeline.md)
