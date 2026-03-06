# TODO

- Split persistent state components from business services:
  - Introduce dedicated `PersistentStateComponent` stores for tips/settings.
  - Keep business logic in separate services that depend on those stores.
- Improve tip storage strategy:
  - Move tip cache to application scope instead of project scope.
  - Persist large tip payloads in JSON cache storage; keep only metadata in XML state.
- Read later: IntelliJ plugin persistence guidance
  - Persisting State of Components: https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html
  - Plugin Services: https://plugins.jetbrains.com/docs/intellij/plugin-services.html
  - Persisting Sensitive Data: https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html
  - Settings Tutorial: https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html
  - Checklist:
    - Use `PersistentStateComponent` for normal plugin state.
    - Prefer `SerializablePersistentStateComponent` in Kotlin.
    - Store cache data in `StoragePathMacros.CACHE_FILE`.
    - Split roamable/shareable state from local cache state.
    - Use `PropertiesComponent` only for very small non-roamable flags.
    - Never store secrets in state; use `PasswordSafe`.
