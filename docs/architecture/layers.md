# Layers

Dependency rule: arrows should only point **inward/downward**. Two violations exist today (marked ⚠).

```mermaid
graph TD
    UI["**UI**\nTipNotificationFactory · TipNotificationActions\nVimCoachSettingsConfigurable · ExcludedTipsListPanel"]

    APP["**Application**\nTipNotifications ⚠ · TipRefresh · TipScheduler\nTipIdeaVimRc ⚠ · AddTipToIdeaVimRc\nVimCoachSettingsScreenController\n─────────────────────\nShowTips · RefreshTips · ScheduleTips  *(interfaces)*\nTipSourceService · FindIdeaVimRc  *(interfaces)*"]

    PERSISTENCE["**Persistence**\nVimTipRepositoryImpl · SettingsRepositoryImpl ⚠\nPersistentVimTipStore · PersistentSettingsStore\n─────────────────────\nVimTipRepository · SettingsRepository  *(interfaces)*"]

    INFRA["**Source / Infra**\nTipSourceServiceImpl\nRemoteTipSourceServiceImpl · FileTipSourceServiceImpl\nTipJsonParser · IdeaVimPluginFindVimRc"]

    DOMAIN["**Domain**\nVimTip · TipMetadata · TipHash · TipCategories · TipLoadResult"]

    UI --> APP --> PERSISTENCE --> INFRA --> DOMAIN

    APP -->|"⚠ TipNotifications + TipIdeaVimRc import\nTipNotificationFactory + TipNotificationActions\n+ VimCoachSettingsConfigurable"| UI
    PERSISTENCE -->|"⚠ SettingsRepositoryImpl imports\nScheduleTips"| APP

    linkStyle 4 stroke:#e74c3c,stroke-width:2px,stroke-dasharray:5
    linkStyle 5 stroke:#e74c3c,stroke-width:2px,stroke-dasharray:5
```

## Fixes

**Violation 1 — `TipNotifications` / `TipIdeaVimRc` → UI layer**
`TipNotifications` and `TipIdeaVimRc` depend on `TipNotificationFactory` and `TipNotificationActions` to build and display notifications. Fix: define a `NotificationFactory` interface in the application layer; have the UI implement it. Remove the direct imports of the concrete UI classes.

The `VimCoachSettingsConfigurable` import is used to open the settings panel from a notification action. Fix: use `ShowSettingsUtil.showSettingsDialog` with a string ID rather than importing the class directly — the ID is already registered in `plugin.xml`.

**Violation 2 — `SettingsRepositoryImpl` → `ScheduleTips`**
When the tip interval or scheduling toggle changes, `SettingsRepositoryImpl` calls `project.service<ScheduleTips>().onSettingsChanged()`. Fix: invert via an observer — emit a `SettingsChangedEvent` or expose a listener list in the persistence layer, and have `TipScheduler` subscribe. Persistence layer stays unaware of Application.
