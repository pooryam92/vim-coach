# A category is not one file — reviews must include cross-listed tips

**Target file:** `.claude/skills/tips-maintain/SKILL.md` (the "Search before
adding" bullet under *How you work*)
**Priority:** IMPORTANT

## Learning / problem

When improving or scoring a whole category, reading only
`tips/categories/<category>.json` gives a false picture. Tips whose **primary**
file is another category but that carry the category as a **secondary** also
render in that category. Reviewing the single file misses them — so the reviewer
can both overlook redundancies and propose a duplicate that the file didn't
reveal.

## Evidence

This session, during "improve visual": I proposed and added
`Number a column g Ctrl-a` to `visual.json`. But `editing.json` already had
`Increment sequentially g Ctrl-a` / `Decrement sequentially g Ctrl-x` tagged
`["editing","visual"]`, which already render under Visual. I had to revert the
addition. `node scripts/lint-tips.mjs` did **not** flag it: its duplicate check
needs shared trailing keys **plus ≥2 shared topic words**, and the topic words
differed (`number`/`column` vs `increment`/`sequentially`) even though the
`g Ctrl-a` keys matched.

## Why it qualifies

Not a one-off. `visual`, `editing`, `cmdline`, `insert`, and `navigation`
routinely cross-list via secondary categories, so every future "improve category
X" pass hits the same trap. It is also **distinct** from the existing "grep the
keys before adding" rule: that rule catches a duplicate at add-time *if* you
remember to run it, whereas this is a comprehension trap one stage earlier — the
reviewer reading a file top-to-bottom never sees the full rendered category, so
redundancies and gaps are misjudged before the grep stage is reached.

## How it should land

Refinement, **one tight clause** — not a new section. Fold into the existing
"Search before adding" bullet under *How you work*, extending its scope from
"before adding a tip" to "before reviewing a category." Do **not** append a
standalone paragraph.

Current bullet (approx.):

> **Search before adding.** `grep -rn` the keys *and* the behavior across
> `tips/categories/`, and run `node scripts/lint-tips.mjs`. Duplicate
> **summaries hard-fail** the generator across *all* files, but the same idea
> under a *different* summary slips through — that's yours to catch. If covered,
> merge or drop instead of adding.

Sketch of the added clause (wording to finalize in Phase 2):

> A **category is not one file**: when reviewing or scoring a whole category,
> first enumerate every tip carrying it in *any* `category` position
> (`grep -rln '"<category>"' tips/categories/` to find the files), since
> secondary-category tips render there too — the single `<category>.json` is not
> the full rendered category.

Touches only `SKILL.md`; replaces nothing, merges into one existing bullet, adds
~2 lines of recurring cost for a trap that recurs on every category review.
