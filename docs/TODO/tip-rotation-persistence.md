# PRD: Persistent Tip Rotation

Status: **Done** — D1–D4 built 2026-10-02

## Goal

Rotation progress survives IDE restarts: every eligible tip is shown once
before any repeats, edited tips come back as new, and when everything has been
seen rotation silently starts again. Today the shown set is in-memory, so with
~280 tips most users never finish a cycle.

## Decisions

- **Per-machine** state in its own non-roaming file. Not synced via settings.
- **Silent reset** — no "you've seen every tip" message.
- **Content key, not stable ids.** Key = hash of summary and details only.
  Editing either makes the tip "new" again — that is wanted. Mnemonic, config,
  `category`, `advanced` and `mode` are excluded so retagging (e.g.
  the parked category restructure, or `f36b706` adding `mode` to every visual
  tip) doesn't reset rotation.
- **Exclusions are out of scope** and stay on `TipHash.fromTip` (summary hash),
  including whether an edited tip should leave the excluded list.
- **Show counts, not a shown set.** Persist `key → timesShown` plus the last
  shown key. Draw = random pick among the filtered pool's lowest-count tips,
  excluding the last shown tip when the pool has more than one. This removes
  today's per-pool reset logic: cycles, filter changes and new tips all fall
  out of "lowest count wins".
- **Deficit cap.** Effective count = `max(stored, poolMax − 1)`. Without it, a
  long-time user who re-enables a category, opts into advanced tips, or restores
  an excluded tip gets a long run of only those tips until their counts catch
  up. With it, joining tips count as "not yet shown this cycle": they mix in
  with the tips still unseen this cycle, so the worst case is one pass through
  the joining tips, not several.
  This also covers opting into advanced tips — no separate priority burst.
  A show stores **effective + 1**, not stored + 1; otherwise a joining tip
  stays capped and keeps winning until its stored count catches up.
- **No "Reset rotation" control** in settings.

## Gotchas

- **Prune against the whole tip cache, not the filtered pool** — otherwise
  disabling a category or excluding a tip wipes its progress.
- **An empty cache must not prune** (tips not loaded yet).
- **Compute the key after parser normalization** (trimmed, blank lines dropped).
- **Changing the key definition for existing fields resets all rotation once.**
  Self-heals via pruning; mention it in the CHANGELOG when it happens.
- **No-repeat needs a fallback.** If the last shown tip is the only
  lowest-count candidate (e.g. the pool shrank to A=3 just shown, B=7; the cap
  lifts A to 6), excluding it leaves nothing. Then pick from the rest of the
  pool regardless of count; repeat only when the pool has a single tip.
- Fallback tips ("No tips found.", "No tips match…") are never recorded.
- **Next tip** and **Show Vim Tip** count as shows, same as startup/periodic.

## Compatibility

- Upgrade: no file → empty state → one fresh cycle, same as a restart today. No
  migration.
- Downgrade: older plugins ignore the separate file; re-upgrading picks it up
  and pruning drops stale keys.
- No changes to tip JSON, the generator, or `PersistentSettingsStore`.
- State is per IDE install (separate config dirs), like every other setting.

## Release

CHANGELOG entry that supersedes the 1.5.1 line "The cycle resets when the IDE
restarts". Update `architecture/overview.md`, which cites rotation as the
example of in-memory application state.

## Deliverables

D1–D3 are independent and change no behaviour; D4 switches it on. Each lands
with its tests.

### D1 — Content key ✅

`TipHash.fromContent(tip)` beside `fromTip`, per the key decision.

Tests (`TipHashUnitTest`):
- Editing the summary or a detail line changes the key.
- Changing only mnemonic, config, `category`, `advanced` or `mode` keeps the key.
- Field boundaries are kept: summary `ab` + detail `c` ≠ summary `a` +
  detail `bc`.
- **Golden value** for one fixed tip, so an accidental key-definition change
  (which resets everyone's rotation) fails the build.

### D2 — Pick core ✅

Pure `pickNext(pool, counts, lastShownKey, random)` → `TipPick(tip, key,
countToStore)`: lowest-count draw, deficit cap, no-repeat fallback. Not wired.

Tests (seeded `Random`, counts fed back after each pick):
- 2n draws from a stable pool: every tip exactly twice.
- Never the same tip twice in a row while the pool has ≥ 2 tips.
- Single-tip pool returns it every time; empty pool returns null.
- Deficit cap: old tips at 5, joining tips at 0 → each joining tip once before
  any old tip repeats.
- Fallback: A = 3 last shown, B = 7 → returns B.

### D3 — Rotation store ✅

Non-roaming `PersistentTipRotationStore` (counts + last shown key) and
`TipRotationRepository`, registered in `plugin.xml`. Unused yet.

Tests:
- Fresh store: empty counts, no last shown key.
- A second repository over the same store sees what the first wrote.
- `@State` storage is `RoamingType.DISABLED` (guards the per-machine decision).
- `PluginWiringIntTest` resolves the store and repository.

### D4 — Switch over ✅

`SelectNextTip` uses D1–D3: prune against the cache (never when empty), record
every real show; delete `TipRotation` and its tests. Docs and CHANGELOG per
Release.

Tests (`SelectNextTipRotationUnitTest`, real store):
- Restart: show k of n, build a new `SelectNextTip` over the same store → the
  next n − k draws are exactly the unseen tips.
- Edit a seen tip's details in the cache → drawn before the cycle completes.
- Category-only retag → count kept, not drawn early.
- Disable then re-enable a category, and exclude then restore a tip → counts
  kept, not pruned.
- Refresh that removes a tip → its key is gone after the next draw.
- Empty cache, or everything filtered out → store untouched, fallback tip shown.
- Existing filtering tests still pass unchanged.
