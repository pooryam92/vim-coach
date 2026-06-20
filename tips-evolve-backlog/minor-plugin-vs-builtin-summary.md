# Plugin tips that overlap a built-in should state the difference in the summary

**Target file:** `.claude/skills/tips-maintain/SKILL.md` (Wording rules)
**Priority:** MINOR (borderline — may already be covered; decide on apply)

## Learning / problem

When a plugin duplicates something a built-in almost does (ReplaceWithRegister's
`gr` vs. plain `p`), the summary should carry *why the plugin is worth enabling* —
the differentiator — not just the action. Otherwise the tip reads like a synonym
for a built-in the reader already has.

## Evidence

Session updating the ReplaceWithRegister tip (`tips/categories/plugins.json`). The
user pushed three times — "clearly state the use of this plugin," "thats the
difference between this plugin and normal paste" — and the value only landed once
the summary moved from `Paste a yank over a word griw` →
`Paste over a word, keep yank griw`. The "keep yank" delta vs. clobbering `p` is
the whole reason to install it.

## Why it qualifies (and why it's borderline)

Recurring shape: several bundled plugins (ReplaceWithRegister, paragraph-motion,
surround) earn their place only by improving on a built-in, and the summary should
encode that delta. **Borderline** because SKILL.md's existing rule — "For
IdeaVim/plugin tips, put the user outcome in the summary" — arguably already
covers it (the differentiator *is* the outcome). Risk: restating existing guidance
for a nuance a careful author already applies.

## How it should land

*Refine, do not add.* Sharpen the one existing sentence in SKILL.md's Wording
rules rather than introduce a new bullet.

❌ before:
> For IdeaVim/plugin tips, put the user outcome in the summary, the
> plugin/option name in `config`/details (`Add surroundings ysiw)`, not
> `Surround text with vim-surround`).

✅ after (add the trailing clause only):
> For IdeaVim/plugin tips, put the user outcome in the summary, the
> plugin/option name in `config`/details (`Add surroundings ysiw)`, not
> `Surround text with vim-surround`). When the plugin improves on a built-in,
> make the summary carry that difference (`Paste over a word, keep yank griw`,
> not `Paste a yank over a word griw`).

*Why:* folds into the existing sentence, no new section, no added recurring
structure. Before applying, re-read the surrounding rule — if it already reads as
covering this, reject to `did-not-qualify.md` instead.
