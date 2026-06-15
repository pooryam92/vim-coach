# Plan: High-Value Config-Backed Tips

How to grow the **"Add to .ideavimrc"** feature from "enable a plugin" into a
curated set of one-click config recipes, and how to keep authoring them
sustainable. Read alongside [tips-authoring/](tips-authoring/README.md),
[tips-pipeline.md](tips-pipeline.md), and
[features/ideavimrc-button.md](features/ideavimrc-button.md).

## Where we are today

- The `config` field is wired end-to-end: a tip with `config` lines shows an
  **Add to .ideavimrc** button that appends them, dedups exact lines, opens the
  file at the new lines, and offers **Reload now**.
- Only **13 tips use `config`**, all in `tips/categories/plugin.json`, all in
  the legacy `set <plugin>` form.
- No non-plugin config tips exist yet (no options, no mappings, no navigation
  recipes), even though that is where most day-to-day value lives.

So the feature is built; the **content** is the gap.

## The opportunity: three buckets of config-backed tips

Treat config tips as three distinct authoring buckets, each with its own
conventions.

### Bucket A — Plugin activation (exists, needs cleanup)

One-line enables for bundled IdeaVim extensions.

```json
{ "config": ["Plug 'tpope/vim-surround'"] }
```

- Migrate the 13 existing tips from `set surround` to the
  `Plug '<github-alias>'` form the authoring guide already requires
  (`tips-authoring/writing-tips.md`, `config` field). IdeaVim flags `set <plugin>` via
  `UsePlugSyntaxInspection`.
- Source of truth for aliases: `external/ideavim/doc/IdeaVim Plugins.md`.

### Bucket B — Useful options (new, highest ratio of value to risk)

Single `set`/`let` lines that improve everyday behavior. Low risk because they
are self-contained and idempotent-ish. Candidates:

- `set scrolloff=5` — keep context around the cursor
- `set ignorecase` + `set smartcase` — smart search casing (ship as a pair)
- `set incsearch` / `set hlsearch` — live + highlighted search
- `set clipboard+=unnamedplus` and `set clipboard+=ideaput` — OS clipboard + IDE
  paste (there is already a non-config tip for this in `ideavim.json` to upgrade)
- `set ideajoin` — IDE-smart `J`
- `set idearefactormode=keep` — sane mode after refactors
- `set number` / `set relativenumber`

### Bucket C — Code-navigation & action mappings (new, highest user "wow")

Map Vim keys to IDE actions via `<Action>(...)`. This is the category the user
called out, and it is what makes IdeaVim feel like more than Vim.

```json
{ "config": [
    "nmap <leader>r <Action>(RenameElement)",
    "nmap gr <Action>(FindUsages)",
    "nmap gi <Action>(GotoImplementation)",
    "nmap <leader>b <Action>(ShowNavBar)"
] }
```

Caveats that must shape the conventions below:

- These **assign keys**, so collision/override risk is real (unlike Bucket B).
- They depend on a `<leader>` being set; either avoid `<leader>` or include a
  `let mapleader=" "` line and accept the dedup caveat.
- This conflicts with the current authoring rule "hold the **enable** line(s)
  only, not usage mappings" (`tips-authoring/writing-tips.md`, `config` field). That rule was written for
  plugins; it needs a carve-out for action mappings, which *are* the config.

## Step 1 (first) — Reconsider the categories

This is the agreed first step. The current 17 categories mirror **Vim's
help-file structure** (`motion.txt`, `change.txt`, `undo.txt`, …) — reference
taxonomy, not user-task taxonomy. That contradicts the project's own rule to
"prefer user outcome over Vim taxonomy" (`tips-authoring/writing-tips.md`), and it shows:

| Symptom | Evidence |
|---------|----------|
| Tiny dead buckets | `undo` 2, `tabpage` 5, `map` 6 tips — too small to filter by |
| Dumping ground | `editing` holds 61 tips, far more than any other |
| No navigation home | code-nav/jumps scatter across `motion`, `ideavim`, `editing` |
| Help-file framing | `change` / `repeat` / `scroll` are manual sections, not user goals |

Two facts that lower the cost of changing this:

- **Recategorizing does not reset user preferences.** Tip identity is hashed from
  `summary`, not category (`TipHash`, `tips-authoring/writing-tips.md`
  "Tip Preference Identity"). Only the published file's group ordering changes.
- The change is almost entirely **data**: rename/merge `tips/categories/*.json`
  files and update the `categoryOrder` list in `scripts/generate-tips.mjs`
  (`tips-pipeline.md:34`). No plugin code changes.

### Proposed taxonomy (17 → ~12)

Keep one **functional axis**; keep config-actionability as the `config` **field**
(not a category — it already marks actionable tips, a parallel category would
duplicate the signal). Recommended moves:

| New category | From | Rationale |
|--------------|------|-----------|
| `navigation` | `motion` + `scroll` | one movement surface; home for code-nav `<Action>` tips (Bucket C) |
| `editing`    | `editing` + `change` | text changes & file/buffer ops (still large — see split note) |
| `insert`     | `insert` | keep |
| `visual`     | `visual` | keep (selection) |
| `pattern`    | `pattern` | keep slug; user-facing text says "search" |
| `cmdline`    | `cmdline` | keep slug; user-facing text says "command line" |
| `history`    | `undo` + `repeat` | merge two tiny buckets into "undo/redo/repeat" |
| `windows`    | `windows` + `tabpage` | one layout surface; absorbs 5-tip `tabpage` |
| `fold`       | `fold` | keep |
| `options`    | `options` | natural home for Bucket B option configs |
| `mappings`   | `map` | keep; secondary home for Bucket C |
| `ideavim`    | `ideavim` | IDE-specific behavior not tied to a plugin |
| `plugin`     | `plugin` | keep |

Net: adds `navigation`, merges `scroll`→nav, `undo`+`repeat`→`history`,
`tabpage`→`windows`. Renames are avoided — the guide deliberately keeps terse
slugs (`pattern`, `cmdline`) while using friendly words in tip text.

**Bolder alternative (defer unless you want it):** a full user-task redesign
(e.g. `navigate`, `edit`, `select`, `search`, `refactor`, `setup`, `plugins`)
that abandons Vim-help framing entirely. Higher churn, bigger payoff for a future
"browse tips by goal" UI. Not recommended as the first move.

### Split-or-keep judgement calls (confirm)

- `editing` stays largest even after merging `change` in. Optionally split off
  `registers` (yank/paste/registers) to relieve it. **Lean: keep for now**, split
  later if it stays unwieldy.
- `mappings` vs `options` vs `navigation` as the home for Bucket C action
  mappings: lead with `navigation` (functional) + `mappings` (secondary).

Once the taxonomy is locked, the three config buckets slot in:
A → `plugin`, B → `options`/`ideavim`, C → `navigation`(+`mappings`).

## Other decisions

### Decision — Provenance comments in appended config (CONFIRMED: yes)

Each appended block is **prefixed with a marker comment**, e.g.

```
" surround — added by Vim Coach
Plug 'tpope/vim-surround'
```

Why: users can trace what was added, and a stable marker is the foundation for
smarter dedup and future removal/undo. Implementation: `IdeaVimRcAppendPlan` (the
only place that builds insert text) emits the comment; the exact-line dedup must
treat the marker so re-adding stays idempotent. This supersedes the
verbatim-lines contract in `ideavimrc-button.md` — update that doc to match.
Open sub-question: one marker per block vs. per line, and exact marker wording.

### Decision — `<leader>` for Bucket C mappings (DEFERRED)

Per your call, settle this later. Until then, author Bucket C with **non-leader
mappings only** (e.g. `gr`, `gi`) so no `mapleader` assumption ships before the
decision is made.

### Decision — Dedup strength

Current dedup is exact-trimmed-line match. That is fine for Bucket A/B enable
lines but weak for Bucket C and for valued options (`set scrolloff=5` vs an
existing `set scrolloff=8` both get appended). Options:

- **Phase 1 (ship now):** keep exact-match; pick config lines that are safe to
  duplicate or harmless if both present. Author around the limitation.
- **Phase 2 (later):** key-aware dedup — detect an existing `set scrolloff` /
  `nmap <leader>r` regardless of value and report `AlreadyPresent` (or warn)
  rather than appending a conflicting second line. Belongs in
  `IdeaVimRcAppendPlan`, stays unit-testable.

## Authoring-guide changes (tips-authoring/)

1. Add a **"Config-backed tips"** section describing Buckets A/B/C and when each
   is appropriate.
2. Replace the blanket "enable lines only, not usage mappings" rule with:
   - plugins → `Plug` enable line only;
   - options → the `set`/`let` line;
   - action mappings → the mapping *is* the config (Bucket C carve-out).
3. Document the provenance-comment convention (now confirmed).
4. Add a **config-tip checklist**: verified against IdeaVim KSP data / `<Action>`
   id exists; safe to append twice or guarded; no `<leader>` assumption (deferred);
   honest summary about what it remaps.
5. Update the **Category Reference** section to the reconsidered taxonomy.
6. Fix the file-creation contradiction: `tips-authoring/writing-tips.md` says configs are
   appended "creating it if needed," but the feature never creates a
   `.ideavimrc` (`ideavimrc-button.md:5`, `AddTipToIdeaVimRc.isAvailable()`).
   Make the guide match the code.

## Code/feature changes (small, optional per phase)

- **Provenance markers (confirmed)** → `IdeaVimRcAppendPlan.of()` builds the
  marker comment; extend its unit tests (`src/test/.../ideavimrc/`) for the
  comment + idempotent re-add. No other call site builds insert text.
- **Phase 2 dedup** → key-aware dedup in the same pure object; add
  `Result.Conflict` or fold into `AlreadyPresent` and surface in `TipIdeaVimRc`.
- **Taxonomy change** → rename/merge `tips/categories/*.json` files + update
  `categoryOrder` in `scripts/generate-tips.mjs`; everything else is data.

No changes needed to the notification/append/reload machinery — it already
handles multi-line config.

## Suggested rollout

1. **Reconsider & migrate categories** (Step 1): lock the new taxonomy, rename/
   merge the source files, update `categoryOrder`, regenerate, update the
   authoring guide's Category Reference. Pure data + docs.
2. **Migrate Bucket A** to `Plug` form; regenerate; verify the button still
   dedups. (Content + the one form fix.)
3. **Author Bucket B** options into `options.json` / `ideavim.json` — 8–12 tips,
   exact-match-safe lines. Highest value for least risk; ships under current code.
4. **Ship provenance markers** in `IdeaVimRcAppendPlan` + tests; update
   `ideavimrc-button.md`.
5. **Lock config conventions** in `tips-authoring/` (Buckets A/B/C, checklist,
   mapping carve-out).
6. **Author Bucket C** navigation/action-mapping tips into the new
   `navigation`/`mappings` files (non-leader mappings only for now).
7. **Phase 2 dedup**: key-aware dedup, once content volume makes the limitation
   bite.

## Settled / outstanding

- **Categories** — reconsider first (Step 1); proposed 17→~12 taxonomy above,
  needs your sign-off on the merges and the split-or-keep calls.
- **Provenance markers** — confirmed yes; wording + per-block/per-line TBD.
- **`<leader>`** — deferred; author non-leader Bucket C mappings until decided.
