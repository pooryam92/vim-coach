# Discovery: Category Restructure (user-facing taxonomy)

Proposal from a category review taken from the user's seat (2026-07-05).
Status: **agreed shape, not started**. Corpus at time of analysis: 307 tips,
14 categories.

## Problem

Categories surface to the user in exactly one place: the settings screen, as a
checkbox list rendering the raw JSON slug (`VimCoachSettingsConfigurable`).
They never appear on the balloon. Three defects from that seat:

1. **Insider jargon on checkboxes** — `pattern`, `cmdline`, `registers`,
   `ideavim`, `mappings` don't map to what a newcomer wants to filter.
   `cmdline` on screen also violates the skill's own "spell out abbreviations
   in user-facing text" rule.
2. **Toggle sizes are wildly uneven** — unchecking `mappings` removes 5 tips;
   unchecking `navigation` removes ~a fifth of the corpus (57 primary).
3. **No real choice over folding** — the ten `z`-command tips are a
   love-it-or-never-use-it topic buried inside `navigation`; opting out of
   fold tips today means losing all motion tips.

## Proposed changes

Net 14 → 14 categories; smallest checkbox goes 5 → 7 tips, biggest 57 → ~47.

### 1. Structural (tips JSON only — tips-maintain work)

- **Merge `options` (12) + `mappings` (5) → `config` (17)** — one user
  intent: "make IdeaVim yours via .ideavimrc". Retag secondary `options` /
  `mappings` tags to `config` everywhere; the eight `ideavim`-primary tips
  carrying `options` as secondary (`ideajoin`, `ideamarks`, clipboard,
  `trackactionids`…) then render in `config` with no move, leaving `ideavim`
  purely "use IDE features from Vim keys".
- **Split `folds` (~10) out of `navigation` (57 → ~47)** — `zf`, `za`,
  `zo/zc`, `zA`, `zj/zk`, `zd`, `zM/zR`, `zr/zm`, plus the Visual `zf` tip.
  Navigation keeps motions, jumps, marks, scrolling.

Coupled updates per reference.md → "Adding or changing a category":
category files, SKILL.md category list + picking rules,
`docs/discover/config-tips-roadmap.md` if affected, then
`node scripts/generate-tips.mjs --check`.

### 2. Display labels (plugin Kotlin/bundle — separate step, own tests)

Checkboxes show raw slugs today. Add a bundle-backed slug → label map in the
settings UI, falling back to the raw slug for unknown categories (categories
arrive dynamically from tip data). Agreed labels:

| slug | label |
|---|---|
| navigation | Moving around |
| folds *(new)* | Folding |
| editing | Editing text |
| plugins | Plugin extras |
| pattern | Search & replace |
| visual | Selecting (Visual mode) |
| ideavim | IDE features from Vim |
| windows | Splits & tabs |
| files | Files & buffers |
| config *(merged)* | Customize IdeaVim |
| cmdline | Command-line (:) |
| insert | Insert mode |
| registers | Copy & paste |
| repeat | Repeat & macros |

## Considered and rejected

- Splitting `pattern` into search vs. substitute — one intent; `cgn`/`gn`
  bridge both.
- Merging `repeat` (7) into `editing` — distinct, recognizable, growable.
- Splitting text objects out of `editing` — cuts across editing/visual/
  plugins; jargon on a checkbox.
- Dissolving `plugins` — not a topic, but a setup-cost filter users want;
  nearly every plugin tip already carries its functional category as
  secondary.
- Renaming other slugs in the data — the label layer gets the benefit without
  migration.

## Caveats

- Preferences store a **disabled** list keyed by slug: users who disabled
  `options`/`mappings` will see `config` enabled by default once (bounded,
  one-time). Same for `folds` vs. a disabled `navigation`.
- `advanced` tagging is orthogonal and untouched.
