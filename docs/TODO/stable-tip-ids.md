# TODO: Generated Tip Ids

Move tip identity out of the plugin and into the generator. The generator emits
an `id` for every tip in `vim_tips_min.json`, and the plugin only reads it.
`TipHash` (`fromTip` and `fromContent`), the stored hashes, and every test and
doc that mentions them are removed.

## Decisions

- **The id is a hash of the content.** It is the first 8 hex characters of
  SHA-256 over the normalized summary and details, keeping field boundaries the
  way `fromContent` does. It's computed by `generate-tips.mjs` and written only
  to the generated file. Sources and authors never see it.
- **Any edit to the summary or the details makes it a new tip** for both
  rotation and exclusions. Changing the mnemonic, config, category, `advanced`
  or `mode` keeps the id. This is a behaviour change for exclusions: today a
  detail edit keeps a tip hidden.
- **The generator fails on duplicate ids.** That is a hash collision; reword
  one of the two tips.
- **No fallback.** The parser drops tips with no id or a duplicate id. That
  replaces `dropDuplicateSummaries`.
- **Exclusions and rotation are keyed by id.** Rename the hash-named fields and
  API (`hiddenTipHashes` → `hiddenTipIds`).
- **Existing exclusions reset once on upgrade.** This is accepted. Note it in
  the CHANGELOG, together with the new detail-edit behaviour.

## Gotchas

- **Changing the id formula resets every user's exclusions and rotation.** Pin
  it with a golden value for a fixed tip, as `TipHashUnitTest` does today. That
  test moves to the generator side.
- **Ship in two steps.**
  1. Generator emits `id`. Older plugins ignore it.
  2. The plugin switch. Release it only after the min file on `main` has ids,
     or the new parser drops every remote tip.
- **Land before PR #92 is released**, or rotation resets once.
- **The upgrade refetch always re-parses.** It sends empty metadata, so neither
  the ETag nor the SHA check can skip it. If it fails (offline, rate limit), the
  old cache still holds tips without ids. The tip store drops those tips on
  load (keeping categories), so the existing "No tips found." fallback shows
  (agreed). The plugin
  version isn't stamped, so it retries next session.
- **Different plugin versions on synced machines** each ignore the other's
  exclusions field and drop it on save, so they wipe each other's exclusions.
  Accepted.
- **The tips-maintain skill warns** that renaming a summary resets its hide
  preference. Change that to: any summary or detail edit makes it a new tip.
