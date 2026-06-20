# A summary carries at most one key/pair — 3+ keys or a symbol cluster move to details

**Target file:** `.claude/skills/tips-maintain/SKILL.md` (the `summary` field rule; merge with the "Never dump alternative keys as a symbol list" Wording bullet)
**Priority:** IMPORTANT (four direct user corrections in one session)

## Learning / problem

When a summary lists three or more keys, or a dense chord/backtick cluster, strip
the keys out: name the *outcome* in the summary and map each key in the details. A
clean two-key pair (`g0 / g$`, `gj / gk`) is fine; triples and clusters are not.

## Evidence

Four separate corrections this session, each phrased "too many symbols in the summary":
- `Maximize a split Ctrl-w _ / Ctrl-w |` → `Maximize a split's height or width`
  (`tips/categories/windows.json`)
- `` Select the last change `[v`] `` → `Select the last change you made`
  (`tips/categories/visual.json`)
- `Open/close folds zo / zc / za` → `Open, close, or toggle a fold`
  (`tips/categories/navigation.json`)
- `Screen-line start/end g0 / g^ / g$` → reworked, keys pulled to details
  (`tips/categories/navigation.json`)

Existing guidance, `SKILL.md` Wording rules:
> **Never dump alternative keys as a symbol list** — name the family in prose
> (`Any bracket or quote works too`) and keep at most one concrete example.

This only covers **alternative/synonym** keys, and reads as about *detail* lines.
The session cases were **distinct-action** keys in *summaries* (`_`=height,
`|`=width; `zo`/`zc`/`za` are three different actions) — not covered.

## Why it qualifies

A consistent standard applied across four unrelated tips in one session — a
pattern, not a one-off. Generalizes to any future multi-key tip. The 2-key-pair
boundary is corroborated by accepted existing tips (`gj / gk`, `g0 / g$`,
`zt zz zb`).

## How it should land

*Refine, don't add.* Sharpen the `summary` field-rule bullet so it pins the
key-count limit, and merge the overlapping "alternative keys" Wording bullet into
it so the two don't restate each other.

Sketch (summary field rule):
> When it ends with the keys it teaches, attach them with a **single space** —
> never `-`, `:`, `→`, or `(…)`. Carry **at most one key or one clean pair**; a
> summary with 3+ keys or a chord/backtick cluster (`Ctrl-w _ / Ctrl-w |`,
> `` `[v`] ``, `zo / zc / za`) should name the *outcome* instead and map each key
> in the details. This holds whether the keys are alternatives or distinct
> actions.

Before applying, re-read both bullets — fold the "alternative keys" guidance in
rather than leaving a near-duplicate.
