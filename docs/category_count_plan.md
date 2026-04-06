# Persist Category Stats And Show Them In Settings

## Summary
Store per-category tip stats in the tip cache and use those stored stats in the settings UI so users can see labels like `editing (12)`. Stats are updated when cached tips are replaced through `saveTips(...)`, which today happens via the source load/refetch flow. Settings reads stored stats only and does not recalculate them.

## Key Changes
- Introduce one persisted category-stat model containing:
  - category name
  - tip count
- Update the tip cache state to persist category stats with the tips.
  - Keep the legacy `categories` field only for deserialization compatibility during transition.
  - Stop using legacy categories as the primary source of truth.
- Update `VimTipService.saveTips(...)` to derive and persist category stats whenever tips are saved.
  - Count each tip once per category it contains.
  - Ignore blank category values and apply the same normalization used for categories today.
- Update `VimTipService` read APIs so consumers can fetch stored category stats.
  - If a caller needs only names, derive them from stored stats in memory.
- Update `VimCoachSettingsScreenState` and `VimCoachSettingsScreenService` to carry structured category entries with `name` and `count`.
- Update the settings UI to render checkbox labels with counts while continuing to persist enabled categories by raw category name.

## Legacy Behavior
- Settings does not backfill or recalculate category stats from legacy cached tips.
- If an older cache has tips but no stored stats yet, settings shows the existing empty-category state until the next successful refresh/refetch saves tips again.
- This is an intentional compatibility tradeoff.

## Public Interfaces / Types
- `VimTipStore.State`
  - Add persisted category stats.
  - Retain legacy `categories` temporarily as compatibility-only state.
- `VimTipStore`
  - Update `setTipCache(...)` to accept category stats.
- `VimTipService`
  - Add or replace the category read API with one returning stored category stats.
- `VimCoachSettingsScreenState`
  - Replace `availableCategories: List<String>` with structured category entries.
- New immutable type
  - `TipCategoryStat` or equivalent.

## Test Plan
- Unit test: `saveTips(...)` persists correct stats for single-category and multi-category tips.
- Unit test: duplicate category names inside one tip count once for that category.
- Unit test: blank/whitespace categories are ignored.
- Unit test: settings state reads stored stats without recomputing from tips.
- Unit test: legacy cache without stored stats does not trigger recomputation and yields the empty-category state.
- UI test: settings renders labels like `basics (2)` and still persists enabled categories by raw name.
- Regression test: when no stored stats exist, the existing “no categories loaded yet” message remains.

## Assumptions
- Stats are persisted, not recomputed on normal settings load.
- Stats change only when `saveTips(...)` replaces cached tips.
- Per-category counts are membership-based.
- Raw category names remain the persisted selection keys.
- Inline label format is `category (N)`.
- Category ordering stays in the current discovered/save-time order.
