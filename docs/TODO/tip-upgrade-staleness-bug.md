# Bug: config-bearing tips (and the "Add to .ideavimrc" button) don't appear after a plugin upgrade

Status: **diagnosed, failing test written, NOT fixed.** Branch `bug/tip-upgrade-staleness`
(worktree `/home/poorya/IdeaProjects/vim-coach-bugproof`, branched from `origin/main` @ `8ca03ca`
with the in-progress `.ideavimrc` feature changes replayed on top).

## Symptom (as reported)

In WebStorm the "Add to .ideavimrc" / "Apply" button was missing even on a tip that should
carry a config. IdeaVim was installed and `.ideavimrc` existed. Restarting the IDE did not help.
Running **Vim Coach: Refresh Tips** made the button appear.

## Root cause

The conditional remote fetch is keyed **only on the remote content** (GitHub blob ETag / SHA),
**not on the local parser/plugin version**.

- `TipRefresh.checkForUpdates()` does a *conditional* fetch when `hasUsableCachedTips()` is true.
- `RemoteTipSourceServiceImpl` sends `If-None-Match: <etag>` and also short-circuits on
  `isSameSha(...)`. If the remote bytes are unchanged it returns **304 Not Modified**, so the
  parser never re-runs.
- `RefetchVimTipsAction` ("Refresh Tips") calls `refetchTips()` → *unconditional* fetch
  (`conditional = false`) → always re-parses and overwrites the cache.

So when a new plugin version can extract **more** from the **same** remote JSON (here:
`VimTip.config` / `.ideavimrc` snippets), an existing user's startup check 304s on the old ETag
and keeps serving the **stale, under-parsed** cache (tips with `config == null`). The
config-aware parse — and therefore the button — only lands after an unconditional refetch.

Note: `VimTipRepositoryImpl.visibleTips()` filters out config-bearing tips when
`includeConfigTips == false`, and `TipNotifications` sets `includeConfigTips = ideaVimAvailable()`.
That gating is correct and is **not** the bug — the bug is that the config data never reaches the
cache in the first place after an upgrade.

## Evidence (WebStorm2026.1 idea.log, 2026-06-21)

Class names reveal the version boundary (old `...tips.source.*` / `TipLoaderServiceImpl` vs new
`...tips.application.loading.*` / `TipRefresh`):

- `15:29:43` — **old plugin 1.2.2**, conditional fetch → returned 309 → **Saved 309**, but the old
  parser stored them **without** `config`, and saved the current remote ETag/SHA.
- `15:30:22` — `PluginDownloader - downloading plugin Vim Coach version 1.4.0 ... updatedFrom=1.2.2`.
- `15:31:31` and `15:50:50` — **new plugin 1.4.0**, conditional fetch → **304 Not Modified** →
  new config-aware parser never runs → cache stays config-less → no button.
- `15:54:02` — manual **Refresh Tips** → unconditional → 309 re-parsed **with** configs → button appears.

## Who is affected

- **Affected:** existing users who upgrade from a parser that didn't read a field to one that does,
  **while the remote content is unchanged** (cached ETag/SHA still matches → 304). Concretely:
  users upgrading from a pre-config version to config-aware 1.4.0 whose cached blob SHA already
  equals the live remote SHA.
- **Not affected:** fresh installs (empty cache → unconditional full parse); users whose remote
  content changed after their last fetch (new ETag → 200 → re-parse); anyone who manually refreshes.
- **Why it matters beyond this feature:** any *future* change that extracts more from the same
  remote JSON will be invisible to all existing users until the remote content changes or they
  manually refresh.

## Fetch source (important for the stopgap)

`VimTipConfig.GITHUB_API_URL` =
`https://api.github.com/repos/pooryam92/vim-coach/contents/tips/vim_tips_min.json`
(served from the default branch `main`). Overridable via `-Dvimcoach.tip.remote.url=...`.

## Immediate stopgap (no code release needed)

Push **one more content change** to `tips/vim_tips_min.json` on `main` (even adding/editing a
single tip). That bumps the GitHub blob SHA, so affected users' next *conditional* fetch returns
200 instead of 304, the config-aware parser re-runs, and buttons appear — on their **next IDE
session** (startup check runs once per app session, in the background).

Caveat: current victims' cached SHA already equals the live remote SHA, so a *new* commit is
required; re-pushing identical content won't change the SHA. This only fixes the present round —
the next silent parser change reintroduces the bug.

## Recommended fix (preferred, not yet implemented)

Version-stamp the cache and force one unconditional refetch when the stamp changes:

1. Add `schemaVersion: Int` to `TipMetadata`, with a `CURRENT_TIP_SCHEMA_VERSION` constant in code.
2. In `TipRefresh.checkForUpdates()`, use
   `conditional = hasUsableCachedTips() && cachedSchemaVersion == CURRENT_TIP_SCHEMA_VERSION`.
   On mismatch / missing (legacy) stamp → unconditional fetch so the new parser re-runs.
3. Bump the constant whenever the parser/domain starts reading a new field (configs = first bump).

Why this one:
- **Self-healing for current victims:** their metadata has no `schemaVersion` → treated as stale →
  forced refetch the first time they run the fixed build. No manual action.
- **Cheap:** one extra 200 (instead of a 304) per schema change, not per startup; ETag optimization
  preserved otherwise.
- **General:** every future field addition is one constant bump.

Rejected alternatives: always-unconditional-on-startup (wastes GitHub's 60/hr unauth budget);
feature-specific heuristic like "refetch if no tip has a config" (fragile, doesn't generalize).

## The failing test (regression guard)

`src/test/.../integration/application/TipRefreshRefetchIntTest.kt` →
`testCheckForUpdatesSurfacesConfigsAfterPluginUpgrade`.

Reproduces the scenario: cache holds a config-less tip + matching ETag/SHA; `FakeTipSource`
returns `NotModified` on the conditional path and a config-bearing `Success` on the unconditional
path. Asserts the config is surfaced after `checkForUpdates()`.

Run:
```bash
cd /home/poorya/IdeaProjects/vim-coach-bugproof
./gradlew test --tests "*TipRefreshRefetchIntTest.testCheckForUpdatesSurfacesConfigsAfterPluginUpgrade"
```
Currently **FAILS** at the `assertNotNull(...config)` line (bug present). It flips green once the
schema-version fix lands. (If you hit a transient Gradle `EOFException`, re-run with `--rerun-tasks`
— that's build infra, not the test.)

## Key files

- `application/loading/TipRefresh.kt` — `checkForUpdates()` (conditional) vs `refetchTips()` (unconditional).
- `application/loading/infra/remote/RemoteTipSourceServiceImpl.kt` — ETag / `isSameSha` 304 short-circuit.
- `application/loading/infra/config/VimTipConfig.kt` — remote URL.
- `domain/TipMetadata.kt` — where `schemaVersion` would be added.
- `persistence/VimTipRepositoryImpl.kt` — `visibleTips()` config filtering (correct; not the bug).
- `application/notifications/TipNotifications.kt` — `includeConfigTips = ideaVimAvailable()` gating.

## Next steps

1. Review the diagnosis + test.
2. (Optional now) push a tips edit to `main` as the stopgap for current users.
3. Implement the schema-version fix on this branch; the test above turns green.
