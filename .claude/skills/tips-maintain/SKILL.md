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
  balloon up to ~300px wide — but one line past ~43 chars clamps the whole body
  to 240px (~35 chars), wrapping every long line in it. So aim for ≤ 35 chars a
  line and 2–3 body lines (the thresholds are estimates for a 13px font). Read
  every summary and detail cold: it must say *which* behavior it teaches and
  *how* to try it on the spot. Wording defects are invisible from the author's
  seat.

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
2. **Verify support *and* release** against the IdeaVim submodule (reference.md →
   "Checking IdeaVim support") — don't carry over upstream-Vim behavior IdeaVim
   doesn't replicate, and don't teach a key that only exists on `master`. The
   submodule runs ahead of the marketplace build, so "the source supports it" is
   half the check: reference.md → "Is the key actually released?" is the other
   half, and it is not optional when mining a changelog. Skipping it once already
   shipped two tips readers could not use.
3. **Propose before editing.** Show each whole tip before → after with a
   one-line reason and get a go-ahead. Agree shape first (how many tips, the
   split axis) before polishing words. Two rejected rewords → stop guessing
   single variants; offer 2–3 concrete options inline.
4. **Edit** `tips/categories/<primary>.json` — a tip lives in the file named by
   its first category.
5. **Validate:** `node scripts/generate-tips.mjs --check` must pass (it is the
   source of truth — run it, don't reason about it). It is strict on source
   shape — unknown keys, a bad `config`, duplicate details, a `Plug` line
   without `plugins` all fail (docs/tips/tips-pipeline.md → Validation) — but
   never on length. Then `node scripts/lint-tips.mjs` — advisory, and the only
   length check; eyeball each hit.
6. **`git status --short`** — only intended files changed. Never commit
   `tips/vim_tips_min.json`: CI regenerates it (regenerate locally only on
   explicit request; build details: docs/tips/tips-pipeline.md). A modified
   `external/ideavim` is expected pointer churn if you refreshed the submodule —
   nothing builds from it; leave it out of the commit (reference.md →
   "Checking IdeaVim support").

## Tip shape

```json
{
  "category": ["plugins", "editing"],
  "summary": "Make a word camelCase crc",
  "details": ["crc turns foo_bar into fooBar", "Cursor can sit anywhere in the word"],
  "config": { "name": "Install vim-abolish", "lines": ["Plug 'tpope/vim-abolish'"] }
}
```

Hard constraints:

- `category` — first entry is primary and **must match the file name**; add a
  2nd/3rd only when it genuinely aids discovery.
- `summary` — ≤ 35 chars, command-first; at most one key or one clean pair
  (`gj / gk`). 3+ keys: name the outcome, map each key in the details.
- `details` — one balloon line ≈ 35 chars; lint flags past 35 (it would wrap),
  and a line past ~43 first (it clamps the whole balloon).
  Prefer 2 details, 3 at most (lint flags a 4th). Never number steps —
  line order already reads as the sequence.
- `config` — optional; read reference.md → "Config tips" before authoring or
  reviewing one.
- `advanced` — optional boolean, **omitted by default**. Add `"advanced": true`
  only to hide a tip from newcomers' default rotation; opted-in users still see
  it (and its `Vim Coach · Advanced` title). The generator emits it only when `true` and
  rejects any non-boolean value. See "Tagging a tip advanced" below.
- `mode` — optional string, **omitted by default**. One of `insert`, `visual`,
  `command` (Normal is the default and stays absent — never tag it). Names the
  mode the reader must be in to press the keys; it renders as a dimmed
  `Vim Coach · Insert mode` title label (informational only — it does *not*
  hide, gate, or de-duplicate anything). The generator rejects any other value.
  See "Tagging a tip's mode" below.
- **Renaming a summary resets that tip's hide preference** (the hide key hashes
  the trimmed summary) — reword only when it's a real improvement.

### Tagging a tip advanced

`advanced` hides a tip from the default rotation for newcomers; users opt in
from settings. There is no fixed rubric — it **emerges from doing**. Bias hard
toward normal: over-tagging shrinks newcomers' default pool, which is the harm.
Tag a few at a time, and when a pattern for "too advanced for a newcomer's first
week" starts to repeat, write it down here as the rubric forms. So far:

- **Count the category's ratio before tagging** — the settled convention is
  already in the rendered set (primary + secondary tags), and it differs sharply
  by category. Count it, don't recall it:
  ```bash
  node -e 'const fs=require("fs"),c=process.argv[1],d="tips/categories/";const s=fs.readdirSync(d).flatMap(f=>JSON.parse(fs.readFileSync(d+f)).tips).filter(t=>t.category.includes(c));console.log(c,s.filter(t=>t.advanced).length+"/"+s.length)' pattern
  ```
  `plugins` sits near zero: needing a `config` block is *itself* the opt-in, so
  a plugin tip is normal even when its concept is deep (YankRing's
  paste-cycling stayed normal on this rule). `pattern` runs high: a `:s`/search
  **flag** riding inside a pattern (`gc`, `//`, `\c`, `\<\>`, `/n` — e.g.
  `Confirm each :s replace with gc`) is reliably past a newcomer's first week.
  Match the siblings you're landing beside rather than scoring the tip in
  isolation.
- **Foundational base moves stay normal even when their variants are
  advanced** — macros (`qa` / `@a`), a named register (`"ayy`), `:action`, the
  Ctrl-v block. Hiding the base leaves newcomers seeing only its variants.
  Untag a family together, never one member.

### Tagging a tip's mode

`mode` labels the non-Normal mode the reader presses the keys in, so a tip read
cold isn't mistaken for a Normal-mode move. Tag only when the mode is not
Normal *and* not already obvious from the summary:

- Set it when the whole tip lives in one non-Normal mode — an Insert-mode
  register paste (`Ctrl-r "`), a Visual-mode operator, a `:` command-line edit.
  Use `command` for command-line (`:`) tips; the label shortens it to
  `Command mode`.
- Leave it off for Normal-mode tips (the default) and for a move that *enters* a
  mode from Normal (`ciw`, `v`, `:s`) — the reader starts in Normal, so the tip
  is a Normal-mode move even though it ends elsewhere.
- `mode` is the machine label; it does not replace naming the mode in the
  wording when a key is mode-ambiguous (see the wording quick list) — do both
  where it helps.

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

Adding well is mostly saying no. **Open-ended review:** triage
[docs/tips/tip-feedback.md](../../../docs/tips/tip-feedback.md) first — a
reader's report outranks any audit hunch; delete an entry once it's acted on.
Then map the gap:
`node .claude/skills/tips-maintain/coverage.mjs` (`--plugins`, `--all`) diffs
IdeaVim's real surface (the `external/ideavim` submodule) against tip text.
Advisory and textual — a miss is a candidate, not a verdict (plugins are matched
through their `Plug` aliases, so a plugin miss is usually real). When mining a release,
fast-forward the submodule first (`git -C external/ideavim fetch --tags origin
&& git -C external/ideavim merge --ff-only origin/master`).

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
