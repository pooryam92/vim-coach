# .ideavimrc stamp comment — make it more useful (PINNED, undecided)

Status: discussion parked, no code changed yet. Resume from "Open decision".

## Problem
When a tip is appended to `.ideavimrc`, `AddTipToIdeaVimRc.stampFor` writes a
comment above the snippet:

```vim
" Install vim-surround — added by Vim Coach
Plug 'tpope/vim-surround'
```

The stamp is "meh": it just restates the `Plug` line below it and carries no
real information. We want it more useful **but keep the "added by Vim Coach"
attribution** (we want users to know a line came from Vim Coach).

Relevant code:
- `src/main/kotlin/.../ideavimrc/AddTipToIdeaVimRc.kt` — `stampFor(name)`
- `src/main/kotlin/.../ideavimrc/IdeaVimRcAppendPlan.kt` — `findBlockStart`
  matches on the real config `lines` only; the stamp is deliberately excluded
  from the dedup match so re-adding doesn't duplicate.
- `src/main/kotlin/.../domain/TipConfig.kt` — `name`, `lines`
- `src/main/kotlin/.../domain/VimTip.kt` — `summary`, `details`, `category`, `config`

Note: `Install vim-surround` snippet is shared by ~9 surround tips (each its
own `config` block, same `Plug` line).

## Options considered
1. **Fold in the tip `summary`** (derived). Useful for no-`name` setting/mapping
   tips (summary describes exactly those lines). For plugin tips it's the *one
   tip you clicked* → reads as "why I added it", mildly narrowing.
2. **`:h` reference** — rejected: may not resolve in IdeaVim's bundled emulation.
3. **Source URL** — rejected: redundant with the `Plug` slug.
4. **New `comment` field on `TipConfig`** (authored per config). Stamp =
   authored comment + attribution. Editorial control, attribution stays
   automatic, dedup still keyed only on real `lines`. Cost: one schema field +
   parser + tests; comment may be repeated across the 9 shared configs.
5. **Put the comment line inside `lines`** (no schema change). Works verbatim,
   but: (a) attribution becomes two comment lines OR hand-typed per tip (loses
   automatic traceability guarantee); (b) the comment becomes part of the dedup
   match key — rewording it later → duplicate on re-add.

## Open decision
Pick between Option 4 (dedicated `comment` field — current lean) and Option 5
(`lines`-only, keep auto stamp as pure attribution, accept two comment lines +
comment-in-dedup).

## If we build Option 4
1. Add `comment: String?` to `TipConfig` + JSON parser.
2. `stampFor`: comment present → `" <comment> — added by Vim Coach`; else
   fall back to current `name`/generic behavior.
3. Update `IdeaVimRcAppendPlanUnitTest` + `docs/features/ideavimrc-button.md`.
4. Seed `comment` for vim-surround / vim-commentary configs as first examples.
