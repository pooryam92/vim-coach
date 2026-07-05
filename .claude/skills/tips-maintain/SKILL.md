---
name: tips-maintain
description: Add, edit, improve, reword, review, or maintain Vim Coach tips, decide which new tips are most worth adding (coverage gaps + value), and keep this skill itself up to date. Use when asked to write a new tip, fix or reword an existing tip, find what's missing or worth adding, improve tip coverage, add or change a category, regenerate vim_tips_min.json, or work in tips/categories/. Not for the plugin's Kotlin/UI code or the tip-rendering pipeline.
allowed-tools: Bash(node scripts/generate-tips.mjs*) Bash(node scripts/lint-tips.mjs*) Bash(node .claude/skills/tips-maintain/coverage.mjs*) Bash(grep*) Bash(git status*)
---

# Maintaining Vim Coach tips

**The one rule everything serves: a tip is short, readable at a glance, has
everything it needs in itself, and teaches one real (Idea)Vim move the reader
can try right now and get better.** The skill's intent is that reader value —
not tip count, not completeness of reference. Two working tests apply it:

- **Density** — each tip must be high-leverage and IdeaVim-true, with a payoff
  visible on the first try. Growth is not success; when a change won't raise
  density, cut instead of add.
- **The reader's seat** — the reader gets one tip *alone*, in random order, in a
  240px balloon (~30–35 chars per line; 2–3 body lines read cleanest). Read every
  summary and detail cold: it must say *which* behavior it teaches and *how* to
  try it on the spot. Wording defects are invisible from the author's seat.

Tips live in `tips/categories/<category>.json` (one file per primary category),
compiled into `tips/vim_tips_min.json` by `scripts/generate-tips.mjs`.

**Style is taught by example, not rules: read [examples.md](examples.md) before
authoring or rewording anything.** Each entry is a whole tip before → after with
the principle it embodies. Everything else on-demand lives in one companion,
[reference.md](reference.md): proving IdeaVim supports a claim, `config`
blocks and the .ideavimrc button, adding/renaming/removing a category.

## Every change — work this checklist

1. **Search first.** Grep the keys *and* the behavior across `tips/categories/`.
   The generator blocks only *identical* summaries — semantic duplicates are
   yours to catch. A category is its *rendered* set (primary + secondary tags):
   check it with `grep -rn '"<cat>"' tips/categories/`, never one file alone.
2. **Verify support** against the IdeaVim submodule (reference.md →
   "Checking IdeaVim support") —
   don't carry over upstream-Vim behavior IdeaVim doesn't replicate.
3. **Propose before editing.** Show each whole tip before → after with a
   one-line reason and get a go-ahead. Agree shape first (how many tips, the
   split axis) before polishing words. Two rejected rewords → stop guessing
   single variants; offer 2–3 concrete options inline.
4. **Edit** `tips/categories/<primary>.json` — a tip lives in the file named by
   its first category.
5. **Validate:** `node scripts/generate-tips.mjs --check` must pass (it is the
   source of truth — run it, don't reason about it). Then
   `node scripts/lint-tips.mjs` — advisory; eyeball each hit.
6. **`git status --short`** — only intended files changed. Never commit
   `tips/vim_tips_min.json`: CI regenerates it (regenerate locally only on
   explicit request; build details: docs/tips/tips-pipeline.md).

## Tip shape

```json
{
  "category": ["plugins", "editing"],
  "summary": "Make a word camelCase crc",
  "details": ["crc turns foo_bar into fooBar", "Cursor can sit anywhere in the word"],
  "mnemonic": "coerce case",
  "config": { "name": "Install vim-abolish", "lines": ["Plug 'tpope/vim-abolish'"] }
}
```

Hard constraints:

- `category` — first entry is primary and **must match the file name**; add a
  2nd/3rd only when it genuinely aids discovery.
- `summary` — ≤ 35 chars, command-first; at most one key or one clean pair
  (`gj / gk`). 3+ keys: name the outcome, map each key in the details.
- `details` — one balloon line ≈ 35 chars; lint flags past 35 (it would wrap).
  Prefer 2 details, 3 at most (lint flags a 4th). Numbered steps only
  for an irreducibly multi-step move.
- `mnemonic` — optional, **omitted by default**; ≤ 40 chars; only when the
  decoded words make the keys stick; skip on 3+-detail tips. See examples.md.
- `config` — optional; read reference.md → "Config tips" before authoring or
  reviewing one.
- `advanced` — optional boolean, **omitted by default**. Add `"advanced": true`
  only to hide a tip from newcomers' default rotation; opted-in users still see
  it (and its `Vim Coach ◆` title). The generator emits it only when `true` and
  rejects any non-boolean value. See "Tagging a tip advanced" below.
- **Renaming a summary resets that tip's hide preference** (the hide key hashes
  the trimmed summary) — reword only when it's a real improvement.

### Tagging a tip advanced

`advanced` hides a tip from the default rotation for newcomers; users opt in
from settings. There is no fixed rubric — it **emerges from doing**. Bias hard
toward normal: over-tagging shrinks newcomers' default pool, which is the harm.
Tag a few at a time, and when a pattern for "too advanced for a newcomer's first
week" starts to repeat, write it down here as the rubric forms. So far:

- `Recall last search with Ctrl-r /` — a register paste inside the `:`/insert
  prompt; niche and mode-specific, not a first-week move.

Wording quick list — each is the reader's-seat test made concrete; worked
versions in examples.md:

- Verb-first, concrete outcome; a typeable form over a placeholder (`griw`, not
  `gr{motion}`; `{count}`/`{char}` only for genuinely variable args).
- Keys attach with a plain space — never a `-` `:` `→` `(…)` separator.
- Every keystroke shown must do something when typed — lead a bare text object
  with an operator (`Act on a class dac`, not `Select a class ac`).
- A transform names both ends — what changes *and* what it becomes.
- Name the use-site — a motion tip's second line points at the edit it sets up
  (`Perfect for ct) edits`), not more motion trivia (`;`/`,` repeat).
- Each tip stands alone — in knowledge, not just sequence; order is random.
- Split by *intent*, not key count: different intents (toggle `za` vs force
  `zo`/`zc`) are separate tips; same intent, different direction (`gj / gk`)
  stays one pair.
- Name the mode when a key is mode-ambiguous; when a plugin overlaps a
  built-in, the summary carries the differentiator.
- Spell out abbreviations in user-facing text (`command-line`, not `cmdline`).
- Pair phrasing stays consistent (`next/previous`, `before/after`); vary one
  axis, keep the operator fixed. Symbol pairs join with `and`, not `/`
  (`{ and }` — a slash between glyphs is a pileup).

## Finding what to add — or cut

Adding well is mostly saying no. When the ask is open-ended, map the gap first:
`node .claude/skills/tips-maintain/coverage.mjs` (`--plugins`, `--all`) diffs
IdeaVim's real surface (the `external/ideavim` submodule) against tip text.
Advisory and textual — a miss is a candidate, not a verdict. Plugin misses
often false-positive: the script matches internal ids while tips carry the Plug
repo name, so grep the `Plug` lines before trusting one. When mining a release,
fast-forward the submodule first (`git -C external/ideavim fetch && git -C
external/ideavim merge --ff-only origin/master`).

Score candidates on four axes; a tip earns its place by winning on at least 3:
**reach** (how many users hit it) · **leverage** (keystrokes/mouse trips saved)
· **IdeaVim fit** (IDE-bridge, plugin power, differs-from-upstream) ·
**teachability** (tryable on the spot, cold, in ≤ 35 chars).

The same axes prune: an existing tip losing on 3 is a removal candidate, and
pure deletion is a legitimate density win. Niche-but-standalone stays; cut only
redundant-with-a-stronger-sibling or actively counterproductive. Two gaps the
script can't see: a command cluster taught only through its flags with no
foundational tip (29 tips taught `:s` trimmings before `:%s/foo/bar/g` itself
was added), and theory — a concept earns at most one tip and it must still be
tryable; if no tryable form exists, fold one line into a concrete host instead.

Present a ranked shortlist (one-line rationale each, plus what you dropped),
get a go-ahead, then author survivors through the checklist above.

## Categories

`navigation` (motion/scroll/fold) · `editing` (change text/undo) · `registers`
(yank/paste/registers) · `visual` (selecting) · `insert` (typing while
inserting) · `repeat` (repeat/automate) · `pattern` (search & replace) ·
`cmdline` (driving the IDE from `:`) · `files` (open/switch/save/close) ·
`windows` (splits & tabs) · `options` (tune behavior) · `mappings` (reshape the
keyboard) · `ideavim` (IDE-bridge, not plugin-specific) · `plugins` (needs an
IdeaVim plugin enabled).

One primary; `cmdline` only when entering `:` *is* the point (a tip mentioning
`:set`/`:map` keeps `options`/`mappings`). Text objects → `editing` unless the
point is selecting (`visual`). `plugins` only when a plugin must be enabled —
usually keep the functional category too. Adding or renaming a category is a
coupled code+docs change: reference.md → "Adding or changing a category".

## When a call gets corrected

When the user rejects or corrects a wording decision, capture it in the same
session as a new before → after entry in examples.md (or sharpen the entry that
failed to prevent it). That file is this skill's memory — there is no separate
backlog.
