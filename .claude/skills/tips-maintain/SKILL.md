---
name: tips-maintain
description: Add, edit, improve, reword, review, or maintain Vim Coach tips, and keep this skill itself up to date. Use when asked to write a new tip, fix or reword an existing tip, add or change a category, regenerate vim_tips_min.json, or work in tips/categories/.
allowed-tools: Bash(node scripts/generate-tips.mjs*) Bash(node scripts/lint-tips.mjs*) Bash(grep*) Bash(git status*)
---

# Maintaining Vim Coach tips

You are the **maintainer** of the Vim Coach tip set: you add, revise, improve,
and co-edit tips on request. Tips are authored by hand in
`tips/categories/<category>.json` (one file per primary category) and compiled
into `tips/vim_tips_min.json` by `scripts/generate-tips.mjs`. Paths are relative
to the repo root. Everything to write or reword a tip is in this file; deeper
detail is split into focused files you open **only when that need arises**:
`checking-support.md` (prove IdeaVim supports a claim), `config-kinds.md`
(authoring a `config` block), `examples.md` (worked before→after), and
`categories.md` (add/rename/remove a category).

> **This skill is self-evolving — that is part of the job.** You own and actively
> maintain your own files (`SKILL.md`, the on-demand `*.md` references, the
> helper scripts, and any new file that earns its place). Whenever an interaction
> teaches a durable
> rule, preference, IdeaVim/generator quirk, or smoother workflow, **fold it in
> within the same change** — add, edit, split, or delete. Leave the skill sharper
> than you found it. Full procedure: *Evolving this skill* below.

## How you work

- **Revise in small batches (≤ 2 tips).** For each, show the **whole tip
  before → after** plus a one-line reason, get a go-ahead, *then* edit. Never
  bulk-rewrite silently.
- **Search before adding.** `grep -rn` the keys *and* the behavior across
  `tips/categories/`, and run `node scripts/lint-tips.mjs`. Duplicate **summaries
  hard-fail** the generator across *all* files, but the same idea under a
  *different* summary slips through — that's yours to catch. If covered, merge or
  drop instead of adding.
- **Verify support before keeping a claim** (`checking-support.md`). Don't carry
  over upstream-Vim behavior IdeaVim doesn't replicate.
- **When improving, scan for:** bad/duplicate categories, misleading or
  overloaded details, plugin/option-name-first or label-style summaries, mixed
  pair phrasing, stray `-`/`:` key separators, bare text objects shown as
  commands, and tips that only make sense after another (order is random).
  `node scripts/lint-tips.mjs` surfaces most of these (advisory — eyeball hits).
- **Always close the loop.** An edit isn't done until `--check` reruns clean.

## The loop (every change)

1. Edit the right `tips/categories/<primary-category>.json` — a tip lives in the
   file named by its **first** category (mismatch fails the generator).
2. Validate (writes nothing): `node scripts/generate-tips.mjs --check` — prints
   `validated <N> tips`, exits non-zero naming the offending file + tip. It is
   the source of truth for validity; run it rather than reasoning about it. Then
   `node scripts/lint-tips.mjs` for advisory quality checks.
3. Confirm only what you intended changed: `git status --short`.

**Commit only the source change.** `tips/vim_tips_min.json` is **generated, and
CI rebuilds + commits it** — leave it out of your commits; hand edits to it are
overwritten. Regenerate it yourself (bare `node scripts/generate-tips.mjs`) only
when the user explicitly asks. Build details: `docs/tips/tips-pipeline.md`.

## Tip object shape

```json
{
  "category": ["navigation"],
  "summary": "Jump to matching bracket %",
  "details": ["Cursor on ( [ { jumps to its pair", "Works in Normal and Visual mode"]
}
```

Config-backed tip (renders an **Add to .ideavimrc** button):

```json
{
  "category": ["plugins", "editing"],
  "summary": "Add surroundings ys{motion}",
  "details": ["ysiw) wraps a word in parens", "ys$\" quotes to end of line"],
  "config": { "name": "Install vim-surround", "lines": ["Plug 'tpope/vim-surround'"] }
}
```

### Field rules

- **`category`** — array; the **first** entry is primary and **must match the
  file name**. One by default; add a 2nd/3rd only when it genuinely aids
  discovery.
- **`summary`** — one command-first line; what to do or what you gain. **≤ 35
  chars.** When it ends with the keys it teaches, attach them with a **single
  space** — never `-`, `:`, `→`, or `(…)`.
- **`details`** — short factual lines (≤ 35 chars each): what it does, context, a
  caveat, or a quick example. Two short lines beat one wrapped line. Blank lines
  are stripped — don't add them.
- **`config`** (optional) — `{ "name": ..., "lines": [...] }` for the `.ideavimrc`
  snippet the button appends. Must be one of the **three additive kinds**
  (`config-kinds.md`). `name` is the button label, verbatim — add it **only when
  it's a meaningful label**; otherwise omit it and the button reads a generic
  `Apply`, which is fine. Don't convert a legacy array tip (`["<line>", ...]`,
  still accepted) to the object form just to add a `name` — a labelless `Apply`
  is not a defect to clean up.

## Wording rules

- **Command-first, concrete outcome over Vim taxonomy.** Verb-first, not a noun
  label (`Show line numbers with number`, not `Line numbers`).
- **Keys attach with a plain space only** — never a `-`/`:`/`→`/`(…)` separator
  (`Add surroundings ys{motion}`, not `… - ys{motion}`). `with` is allowed as
  genuine prose, not as a separator.
- **Every keystroke shown must do something when typed.** A bare text object
  (`iw`, `ac`, `ii`, `ai`, `am`) does nothing alone — lead with an operator
  (`Act on a class dac / cic`, not `Select a class ac`).
- **Each tip stands alone — in knowledge, not just sequence.** Order is random,
  so a tip can't assume the reader just saw another, nor that they grasp the
  concept it teaches. Fold a dependent point into its host, or split an
  overloaded tip into self-contained ones (only when each earns a distinct
  summary — the generator rejects duplicates).
- **Spell out abbreviations in user-facing text.** `command-line`, not `cmdline`
  (that's the category slug only). `char`/`msg`/`prev` are fine only when
  spelling out would wrap.
- **Name modes explicitly when context helps:** `Normal mode`, `Insert mode`,
  `Visual mode`.
- **For IdeaVim/plugin tips, put the user outcome in the summary**, the
  plugin/option name in `config`/details (`Add surroundings ys{motion}`, not
  `Surround text with vim-surround`).
- **Don't restate the summary in detail line 1** — the most-read line; spend it
  on the mechanic, value, or mnemonic. For a jargon-heavy command give a typeable
  example + plain-words result (`:v/foo/d deletes lines lacking foo`), not the
  syntax anatomy. Read each line cold: if it only parses once you know the
  concept, rewrite it.
- **Never dump alternative keys as a symbol list** — name the family in prose
  (`Any bracket or quote works too`) and keep at most one concrete example.
- **Consistent pair phrasing** — `next/previous`, `before/after`, `top/bottom`.
  In a slashed pair, vary one axis and keep the operator fixed.

Display: tips render in a 240px IntelliJ balloon (~30–35 chars/line, ~2 wrapped
body lines visible). Worked before→after examples: `examples.md`.

Reword only when it's a real improvement — **renaming a summary resets the user's
hide preference** for that tip (the hide key hashes the trimmed summary).

## Categories (14)

`navigation` (motion/scroll/fold) · `editing` (change text/undo) · `registers`
(yank/paste/registers) · `visual` (selecting) · `insert` (typing while
inserting) · `repeat` (repeat/automate) · `pattern` (search & replace) ·
`cmdline` (driving the IDE from `:`) · `files` (open/switch/save/close) ·
`windows` (splits & tabs) · `options` (tune behavior) · `mappings` (reshape the
keyboard) · `ideavim` (IDE-bridge, not plugin-specific) · `plugins` (needs an
IdeaVim plugin enabled).

Picking: one primary; secondaries only when they aid discovery. `cmdline` only
when entering `:` *is* the point — not just because a tip mentions `:set`/`:map`
(keep `options`/`mappings`). Text objects → `editing` by default, `visual` when
the summary is about selecting. `plugins` only when a plugin must be enabled
(usually keep the functional category too). Adding a *new* category is a coupled
code+docs change — see `categories.md`.

## Self-checks

`node scripts/lint-tips.mjs` is advisory (never gates, always exits 0). It
reports over-length summaries/details, possible stray separators, and **possible
duplicate tips** — the soft checks the generator skips. The duplicate check pairs
tips that share a config line (outside `plugins`, where enable lines are shared
by design) or share both trailing keys and ≥2 topic words; it catches the same
behavior under *different* summaries, which the generator's identical-summary
check cannot. Some hits are false positives by design (a `-` that's part of the
keys like `Ctrl-w + / -`; intentional siblings like `gu / gU` in two modes) — so
eyeball each before acting.

## Evolving this skill

The standing mandate is the callout at the top; this is its procedure. Run it
whenever a change earns it.

1. **Recognize the trigger:** (a) the user corrects a tip/workflow in a way that
   reveals a *general* rule; (b) a new stated preference or constraint; (c) an
   IdeaVim quirk or generator/script behavior worth recording; (d) a repeated
   manual step a script or check could own.
2. **Classify before acting.** Durable + generalizable → fold in; one-off → skip
   (don't bloat). A must-fire wording/format/workflow rule → this `SKILL.md`; a
   worked example → `examples.md`; a support finding → `checking-support.md`; a
   config-shape rule → `config-kinds.md`; a category-procedure change →
   `categories.md`; a mechanical check or repeated chore → a helper script. New
   files are fine when they pull weight; prefer extending an existing one.
3. **Apply it, don't just note it.** Show the exact change — the rule/tool **plus
   the before→after that prompted it** — and a one-line reason. Apply small
   unambiguous edits directly; get a quick go-ahead for anything judgment-heavy.
   Dedupe against what's here; delete rules that turn out wrong.
4. **It rides the same change** as the work that taught it, so rule/tool and tip
   edits commit together.
</content>
</invoke>
