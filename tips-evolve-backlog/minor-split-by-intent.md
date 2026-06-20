# Split a multi-key tip by intent, not by key count

**Target file:** `.claude/skills/tips-maintain/SKILL.md` (Wording rules, the "Each tip stands alone … split an overloaded tip" bullet)
**Priority:** MINOR (a refinement; resolved two splitting calls in one session)

## Learning / problem

When deciding whether to split a tip that teaches several keys, split by *intent*,
not by key count. Keys serving **different intents** become separate tips; keys
that are the **same intent, different direction** stay together as one pair.

## Evidence

Two splitting decisions in one session, both resolved by intent-sameness:
- Folds split into `Toggle a fold with za` (toggle) + `Force a fold open or
  closed` (`zo`/`zc`) — toggle and force are *different intents*.
- `g0`/`g$` kept together (`Wrapped line start or end`) because both are the same
  intent — "go to the edge"; then `g^` split off into its own tip as a different
  target (first non-blank). (`tips/categories/navigation.json`)

Existing guidance, `SKILL.md` Wording rules:
> Fold a dependent point into its host, or split an overloaded tip into
> self-contained ones (only when each earns a distinct summary — the generator
> rejects duplicates).

That gate is purely mechanical (distinct summary exists). It doesn't say *when*
splitting is the right call semantically.

## Why it qualifies

A reusable decision rule that resolved two distinct calls in one session, and the
same logic generalizes to any key-family tip (`0`/`^`/`$`, `i`/`a` text objects,
`</>` indent, etc.). Durable.

## How it should land

*Refine the existing bullet — fold in one clause, no new section.*

Sketch (append to the "split an overloaded tip" sentence):
> … into self-contained ones — split when the keys serve **different intents**
> (toggle vs force: `za` vs `zo`/`zc`), but keep a **same-intent direction pair**
> as one tip (`g0`/`g$` = go to the edge). Each split tip must still earn a
> distinct summary; the generator rejects duplicates.

*Why:* keeps the mechanical gate but adds the missing semantic one, so future
splits don't fragment a coherent pair or bundle two intents.
