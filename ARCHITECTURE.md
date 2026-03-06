# Architecture

The codebase is organized by plugin capability, centered on the `tips` feature.

## Main Structure

- `src/main/kotlin/com/github/pooryam92/vimcoach/core/shared/i18n`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/domain`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/state`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/source`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/application`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/ui`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/entrypoints`

## Responsibilities

- `core/shared/i18n`: shared bundle access (`MyBundle`)
- `features/tips/domain`: tip models and result types
- `features/tips/state`: persistent project state service for cached tips/metadata
- `features/tips/source`: source selection and integrations (remote/file/parsing)
- `features/tips/application`: tip loading/update orchestration
- `features/tips/ui`: notification rendering and interactions
- `features/tips/entrypoints`: IntelliJ actions and startup activity

## IntelliJ Registration Notes

`plugin.xml` keeps action IDs stable (`com.github.pooryam92.vimcoach.actions.*`) while action/service/startup class FQCNs point to the feature-based packages.
