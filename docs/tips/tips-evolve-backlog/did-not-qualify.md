# Did not qualify (recorded for context)

No action recommended. Kept so a future review doesn't re-litigate these.

## False-confidence proposals — not a skill gap

This session proposed tips that already existed or were already excluded:
- `g;` / `g,` and `gi` already exist in `tips/categories/navigation.json`.
- multiple-cursors is already deferred in `config-kinds.md` (VIM-2178).

The skill already mandates grepping keys + behavior before adding and already
documents the deferral. This was sloppy application of existing guidance, not a
gap in the skill. No change.

## Borderline — lean: no change

`coverage.mjs --plugins` lists deferred plugins (multiple-cursors, easymotion,
which-key) as candidates without flagging that they're deferred. Minor friction,
but `config-kinds.md` is the authoritative gate; duplicating the deferral list
into the script risks drift.

Only worth doing if these false candidates keep costing review time. If pursued:
have `coverage.mjs` read/annotate the deferred list from `config-kinds.md` rather
than hardcoding a copy.
