# PRD: Tip Selection Redesign (unified filter pipeline)

Status: **agreed, not started** (2026-07-11). Follow-up to the no-repeat
rotation work, which landed inside the current structure.

## Problem

Selection policy — "which tip does the user see next" — is smeared across two
layers and three components:

- `TipNotifications` (application, **project** service) applies the category
  filter by choosing which `getRandomTip` overload to call.
- `VimTipRepositoryImpl` (persistence, application service) applies the other
  three filters via `TipSelector` (excluded hashes, config-without-IdeaVim,
  advanced opt-in), runs `TipRotation`, and owns the fallback tips.
- The repository depends on `SettingsRepository` to do this, so the
  persistence layer reads user preferences — business rules living below the
  layer that should own them.

Consequences:

1. Adding a filter means editing a hardcoded chain and deciding *which layer*
   it belongs in; the no-repeat feature had to go in persistence purely
   because that's where app-wide state happened to live.
2. The repository's contract (`getRandomTip`) is policy, not data access —
   it can't be tested or reused as a plain query surface.
3. Session state (`TipRotation`) sits in a *persistence* class despite
   persisting nothing.

## Goals

- All selection policy in one named application-layer component.
- Filters are one composable shape: adding one is adding a list element.
- Repository becomes dumb queries with no `SettingsRepository` dependency.
- Behavior unchanged: same filters, same rotation semantics, same fallback
  messages, rotation still app-wide and in-memory.

## Non-goals

- No new filters, weighting, or spaced repetition (this creates the seam for
  them, nothing more).
- No persistence changes; stores are untouched.
- No UI or entry-point changes.

## Design

### `SelectNextTip` — application-layer use case (new)

Application-level service (registered in `plugin.xml`), the only caller of
tip queries and the single chokepoint every entry point goes through:

```kotlin
class SelectNextTip(...) {
    fun select(includeConfigTips: Boolean): VimTip  // tip or fallback, never null
}
```

Owns, in order:

1. **Context building** — reads `SettingsRepository` once per selection into a
   `TipSelectionContext` (enabled categories, hidden hashes, advanced opt-in,
   `includeConfigTips`).
2. **Filter chain** — `List<TipFilter>` applied left to right:

   ```kotlin
   fun interface TipFilter {
       fun apply(pool: List<VimTip>, context: TipSelectionContext): List<VimTip>
   }
   // categoryFilter, excludedTipsFilter, configTipsFilter, advancedTipsFilter
   ```

   The category filter keeps its current edge rule internally: no available
   categories yet → pass-through.
3. **Rotation** — `TipRotation` moves to this package unchanged. It stays a
   distinct final stage rather than a `TipFilter` because it is stateful and
   *selects* rather than filters. App-wide because `SelectNextTip` is an
   application service; still deliberately in-memory.
4. **Fallbacks** — the two fallback tips move here from the repository.
   Rule: empty tip cache → "No tips found."; non-empty cache but empty pool
   after filtering → "No tips match the selected categories." (Today the
   unfiltered overload reports "No tips found." even when tips exist but are
   all hidden — the redesign fixes that inconsistency.)

`TipNotifications.selectRandomTip()` collapses to
`selectNextTip.select(ideaVimAvailable())`; `includeConfigTips` stays a parameter
because IdeaVim availability is resolved at the project-service seam.

### `VimTipRepository` — slimmed to queries

- Add `getTips(): List<VimTip>` (and drop both `getRandomTip` overloads).
- Drop the `SettingsRepository` dependency and the fallback constants.
- `TipSelector` is deleted; its filters become the chain above.
- `TipSelectionIndex` is retired: at ~300 tips a linear category filter per
  selection is negligible, and keeping the index would leave category matching in
  the persistence layer — against the point of the redesign.

### Layer map after

```
TipNotifications (app, project svc)  →  SelectNextTip (app, application svc)
                                          ├── SettingsRepository (persistence)
                                          ├── VimTipRepository   (persistence, queries only)
                                          └── TipRotation        (app, in-memory)
```

## Migration validator

`TipSelectionBehaviorIntTest` (added ahead of the migration) pins every
selection behavior — rotation cycling, exclusion, category, advanced, config
filter (include direction only; the test IDE bundles IdeaVim), both fallbacks —
at the seam that survives the redesign: production
`ShowTips` wiring in front, the `TipNotifier` port behind. It references no
selection internals and **must pass unchanged at every migration step**. The
one behavior this PRD intentionally changes (all-hidden + unfiltered fallback
message) is deliberately not pinned there.

## Migration steps (each keeps tests green)

1. Introduce `SelectNextTip` + `TipSelectionContext` + `TipFilter` with the chain
   delegating to a repository `getTips()` query; keep old repository methods
   temporarily. Point `TipNotifications` at `SelectNextTip`.
2. Move `TipRotation` and the fallback rule into the new package/service.
3. Delete `getRandomTip`, `TipSelector`, `TipSelectionIndex`, and the
   repository's settings dependency; update `FakeVimTipRepository`.
4. Move tests: `VimTipPreferenceSelectionUnitTest`, the no-repeat unit tests,
   and the rotation integration test retarget `SelectNextTip`; repository tests
   shrink to query coverage. Add one unit test per filter.
5. Update `docs/features/show-tip.md` (selection section + diagram) and
   `docs/architecture/overview.md` (the "all filtering happens in the
   repository layer" claim moves to `SelectNextTip`).

## Risks / notes

- `VimTipRepository` is internal API only; no external consumers break.
- `AdvancedTipsNudge` keeps using `hasAdvancedTips()` — unaffected.
- Rotation state moves service instances during the refactor; acceptable, it
  is session-scoped by design.

## Acceptance

- `TipSelectionBehaviorIntTest` passes without a single edit.
- Every existing selection behavior test passes against `SelectNextTip`.
- Repository has no settings dependency and no selection logic.
- Adding a hypothetical filter touches exactly one list plus its own tests.
