# Theory earns at most one tip — and it must still be tryable

**Target file:** `.claude/skills/tips-maintain/SKILL.md` (the opening philosophy paragraph, lines ~9-14; connects to the placeholder Wording rule and the "Finding the best tips to add — or cut" pruning section)
**Priority:** MINOR (borderline — refines the opening philosophy; a stated user preference, partly covered by the existing "not a reference fact" line)

## Learning / problem

Practical, tryable tips are primary — but theoretical/conceptual grounding is
**not banned**. The rule is:

1. **Theory gets at most one tip per concept** — never scattered across siblings
   or repeated as a recurring detail line.
2. **That one tip must still be tryable** — a concrete key to press, not
   `d{motion}`-style placeholders. The original tip's sin is being untryable, not
   being theoretical.
3. **When a concept can only be stated abstractly** (no tryable form exists),
   delete the standalone tip and fold its rule into a concrete host as a single
   detail line — don't keep an untryable grammar tip, and don't repeat the rule
   across every sibling.

## Evidence

This session: `Change/delete with operator + motion` had three `{motion}`
placeholders (`d{motion} deletes text` / `c{motion} changes text`). Rewording it
concrete was attempted (variations A/B/C) and rejected. Resolution: **deleted**
the standalone concept tip and folded `"Same d/c works with any motion"` into the
concrete host `Change/delete a word cw / dw` (`tips/categories/editing.json`) —
on that one host only, not echoed across `d$/c$`, `d0/c0`, `D/C`.

User then stated the general preference: "we want practical tips first but also
give the user the theoretical stuff in 1 tip."

This is the mirror of `minor-base-command-tip-missing.md`: that finding says a
base *command* taught only through flags needs a foundational tip (add the base);
this one says a base *concept* that can't be made tryable should be folded into a
practical tip rather than stand alone. Together: a foundational concept earns its
own tip only if it's concretely tryable.

## Why it qualifies

A directly stated user preference about tip-set philosophy, generalizing to any
conceptual/grammar material (text-object grammar, count grammar, operator+motion
composability). The existing opening says theory is "a reference fact to file
away" — i.e. reads as a flat ban — which is too absolute and doesn't tell the
maintainer how to handle theory that *is* worth one consolidated, tryable tip.

## How it should land

*Refine the opening philosophy paragraph — do not add a section.* Keep the
practical-first north star sharp; add the theory-consolidation allowance right
after the "reference fact to file away" clause.

Sketch (append to the opening paragraph):
> Theory isn't banned — but it earns **at most one tip per concept**, and that
> tip must still be tryable (a concrete key to press, not `d{motion}`-style
> placeholders). When a concept has no tryable form, delete the standalone tip
> and fold its rule into a concrete host as one detail line
> (`"Same d/c works with any motion"` on `cw / dw`) — never an untryable grammar
> tip, never the same rule repeated across siblings.

*Why:* turns the flat "no reference facts" ban into an actionable rule (one tip,
tryable, else delete-and-fold), without blunting practical-first. Before applying,
check the pruning section and the placeholder Wording bullet — fold rather than
restate if either already implies a piece of this.
