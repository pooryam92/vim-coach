# Adding or changing a category

Open only when adding, renaming, or removing a category. The 14 current
categories and picking rules are in `SKILL.md`.

Coupled across code + docs — update together:

1. `tips/categories/<name>.json` — adding a category = a new file (its name is the
   category); removing one = migrate or delete its tips first.
2. The category table + picking rules in `SKILL.md` (and the 14-count).
3. `docs/discover/config-tips-roadmap.md` if it affects the config roadmap.

Ordering needs no change — categories sort alphabetically automatically. Then run
`node scripts/generate-tips.mjs` to confirm it validates.
