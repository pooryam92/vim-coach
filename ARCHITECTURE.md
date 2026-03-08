# Architecture

The codebase is organized by plugin capability, centered on the `tips` feature.

## Main Structure

- `src/main/kotlin/com/github/pooryam92/vimcoach/core/shared/i18n`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/domain`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/state`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/state/store`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/source`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/application`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/ui`
- `src/main/kotlin/com/github/pooryam92/vimcoach/features/tips/entrypoints`

## Responsibilities

- `core/shared/i18n`: shared bundle access (`MyBundle`)
- `features/tips/domain`: tip models and result types
- `features/tips/state`: business services for reading/updating tips and user preferences
- `features/tips/state/store`: `PersistentStateComponent` stores for serialized XML state
- `features/tips/source`: source selection and integrations (remote/file/parsing)
- `features/tips/application`: tip loading/update orchestration
- `features/tips/ui`: notification rendering and interactions
- `features/tips/entrypoints`: IntelliJ actions and startup activity
- `features/tips/ui/settings`: settings configurable and per-setting UI components

## IntelliJ Registration Notes

`plugin.xml` keeps action IDs stable (`com.github.pooryam92.vimcoach.actions.*`) while action/service/startup class FQCNs point to the feature-based packages.

Current service scopes:
- `VimTipStore`, `VimTipService`, `VimCoachSettingsStore`, `VimCoachSettingsService`: application services
- `TipSourceService`, `TipLoaderService`: project services
