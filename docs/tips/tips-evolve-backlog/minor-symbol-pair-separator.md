# Symbol/bracket key-pairs read worse joined with `/`

**Target file:** `.claude/skills/tips-maintain/SKILL.md` (Wording rules — the "Consistent pair phrasing" bullet)
**Priority:** MINOR (a wording refinement; possibly memory as personal taste)

## Learning / problem

The default pair joiner in summaries is ` / ` (`gj / gk`, `zo / zc`). But when
*both* keys are bracket/symbol glyphs, the slash creates a three-symbol pileup
(`{ / }`, `( / )`) the user finds noisy and harder to read than `and`
(`{ and }`, `( and )`). The skill's "Consistent pair phrasing" rule is silent on
this case, so it reads as if ` / ` is always preferred — which led to a proposed
edit that the user rejected.

## Evidence

This session I proposed normalizing `Move by paragraphs { and }` and
`Move by sentences ( and )` to ` / ` for "consistency." User rejected:
*"like the and too many symbols is not nice."* Both tips already use `and` in
`tips/categories/navigation.json`, so the data already follows the unstated
convention.

`SKILL.md` Wording rules currently:
> Consistent pair phrasing — next/previous, before/after, top/bottom. In a
> slashed pair, vary one axis and keep the operator fixed.

## Why it qualifies

Generalizable to any symbol-pair summary, and recording it prevents repeating the
exact wrong "consistency" edit. Decide in Phase 2 whether it lands as a clause in
the existing rule or as a personal-taste memory.
