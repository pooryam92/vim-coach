# coverage.mjs reports plugins by internal id, not Plug repo name

**Target file(s):** `.claude/skills/tips-maintain/coverage.mjs` (preferred) and/or `.claude/skills/tips-maintain/SKILL.md` (the coverage / candidate-wells guidance)
**Priority:** MINOR (recurring false positive; deterministic)

## Learning / problem

`coverage.mjs --plugins` flags plugins as untaught using IdeaVim's *internal
extension id*, but tips reference the plugin's *Plug repo name* in their `config`
lines. So a plugin that is fully covered shows up as a "miss" whenever the two
names differ — a recurring false positive that has to be cleared by hand on every
coverage run.

## Evidence

This session, the coverage run listed `functextobj` and `textobjindent` as
plugins with no tip. Both are fully covered:
- `functextobj` ↔ `Plug 'kana/vim-textobj-function'`
- `textobjindent` ↔ `Plug 'michaeljsmith/vim-indent-object'`

Cleared only by grepping `Plug '…'` lines across `tips/categories/`. The skill's
general caveat ("a miss is a candidate, not a verdict") exists but does not name
this specific id-vs-repo-name mismatch.

## Why it qualifies

Deterministic and recurring: the same false positives appear on every
`--plugins` run, and likely more bundled plugins have id≠repo-name. The mapping
is known/finite, so this is exactly the kind of deterministic check a script
could own rather than re-reasoning each session.
