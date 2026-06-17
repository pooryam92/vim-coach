# Taxonomy & Config Plan (WIP)

In-flight work: the category migration and the config-tip roadmap. Everything
here is **data + docs only** unless a line is flagged *(needs code)*.

---

## 1. Final categories — 14 (locked 2026-06-16)

Categories are emitted alphabetically by name (order is cosmetic — selection is
random at runtime).

| # | Category | ← from (old) | Job |
|---|---|---|---|
| 1 | `navigation` | motion + scroll + fold | move through code |
| 2 | `editing` | (editing − file-ops) + change + undo | change text |
| 3 | `registers` | *(split from editing)* | manage what you've copied |
| 4 | `visual` | visual | select precisely |
| 5 | `insert` | insert | type faster while inserting |
| 6 | `repeat` | repeat | repeat / automate an edit |
| 7 | `pattern` | pattern | search & replace |
| 8 | `cmdline` | cmdline | drive the IDE from `:` |
| 9 | `files` | *(split from editing)* | open, switch, save, close files |
| 10 | `windows` | windows + tabpage | splits & tabs (layout only) |
| 11 | `options` | options | tune behavior |
| 12 | `mappings` | map | reshape the keyboard |
| 13 | `ideavim` | ideavim | IDE-bridge behavior |
| 14 | `plugin` | plugin | install capabilities |

**Locked decisions:** split `registers` now (~15 tips); `navigation` stays merged
(~58, the core skill); the 17 file/buffer/save-quit tips get their own `files`
category; **no** `windows`→`workspace` rename; slug `mappings` over `map`.

**Migration = rename/merge `tips/categories/*.json`.** No new code; the settings
panel keeps rendering raw slugs. Steps:
1. Merge motion+scroll+fold→`navigation`, change+undo→`editing`, tabpage→`windows`.
2. Split `registers` and `files` out of `editing` (first category = new file name).
3. Rename `map`→`mappings`.
4. Remap any secondary categories pointing at dead slugs (`scroll`, `fold`,
   `tabpage`, `change`, `undo`, `map`); drop `editing` from file-ops that no longer
   change text.
5. `node scripts/generate-tips.mjs` must pass (validates first-category ==
   filename). Tip count unchanged (~291).
6. Update `../README.md` and `../../tips-pipeline.md` to mirror the new order.

**Tensions to fix during migration:**
- Search options (`incsearch`, `hlsearch`, `smartcase`, `wrapscan`) sit in
  `options`, not `pattern` — add `pattern` as a secondary so they're findable.
- `:g` is duplicated across `pattern` and `cmdline` — reconcile to one.
- `.` (repeat last change) = `editing` primary / `repeat` secondary — don't let it
  get deduped against `repeat`.
- `:reg` / `:marks` / `:jumps` live in `cmdline` but their job is
  registers/navigation — secondary-tag them.

*(needs code, deferred)* A friendly **label/description** layer — a
`tips/categories.json` manifest read by `VimCoachSettingsConfigurable.kt` so the
checkbox shows "Command line" instead of `cmdline`. Separate enhancement; not part
of this selection.

---

## 2. Config-tip kinds — what an `.ideavimrc` can hold

Organized by what the user wants. The line that matters for shipping is
**additive vs. positional**.

**Additive — safe to one-click append today** (unique, order-independent lines):
- **Install a capability** — `Plug 'tpope/vim-surround'`. 13 tips today, on the
  legacy `set <plugin>` form; migrate to `Plug`.
- **Tune built-in behavior** — `set scrolloff=5`, `ignorecase smartcase`,
  `hlsearch`, `number`. ~7 tips, each needs a `config` line added.
- **IDE-bridge options** — `set ideajoin`, `idearefactormode=keep`,
  `clipboard+=unnamedplus`. ~5 tips, each needs `config`.

**Positional / opinionated — needs key-aware dedup + a leader decision first**
(claims keys or sets state others depend on; *not* shippable yet):
- **`<Action>` maps** — `nmap gr <Action>(FindUsages)`. 0 today.
- **Reshape the keyboard** — `inoremap jj <Esc>`, `nmap Y y$`. 0 today; collisions.
- **Foundations** — `let mapleader=" "`, `sethandler`. 0 today; these *gate* the
  two above by changing the meaning of other config.

The additive set is ~25 tips of value shippable **before** any dedup or leader work.

**Plugin enable/use split (locked):** each plugin = an **enable tip** in `plugin`
(carries the `Plug` line, drives the button) + a **usage tip** in its functional
category (keystrokes, no config). The 13 current `plugin` tips are all enable tips;
usage tips already live in their homes.

---

## 3. Roadmap — ship additive first

1. **Bucket A** — migrate plugin form `set <plugin>` → `Plug '<alias>'` for all 13;
   apply the enable/use split.
2. **Bucket B** — add a `config` line to the ~12 existing option tips. Highest value
   per effort; exact-match-safe, no leader, no collisions.
3. Provenance markers in `IdeaVimRcAppendPlan` + tests.
4. Lock config conventions in `tips-authoring/`.
5. Decide the `<leader>` convention — gates everything positional.
6. Phase 2 key-aware dedup in `IdeaVimRcAppendPlan` — gates positional config.
7. **Bucket C** — `<Action>` maps, non-leader first.
8. Keyboard + foundations families last.

Steps 1–2 deliver ~25 config-backed tips with zero code or taxonomy changes.

---

## Coupling points (keep in sync)

`tips/categories/<name>.json` (filename == a tip's **first** category) ·
`../README.md` Categories table + notes · `../../tips-pipeline.md`. After any
change: `node scripts/generate-tips.mjs` must pass.
