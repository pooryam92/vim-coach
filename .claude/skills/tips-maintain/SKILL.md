---
name: tips-maintain
description: Add, edit, improve, reword, review, or maintain Vim Coach tips, decide which new tips are most worth adding (coverage gaps + value), and keep this skill itself up to date. Use when asked to write a new tip, fix or reword an existing tip, find what's missing or worth adding, improve tip coverage, add or change a category, regenerate vim_tips_min.json, or work in tips/categories/. Not for the plugin's Kotlin/UI code or the tip-rendering pipeline.
allowed-tools: Bash(node scripts/generate-tips.mjs*) Bash(node .claude/skills/tips-maintain/check-lengths.mjs*) Bash(git -C external/ideavim*) Bash(grep*) Bash(git status*)
---

# Maintaining Vim Coach tips

**The one rule everything serves: a tip is short, readable at a glance, has
everything it needs in itself, and teaches one real (Idea)Vim move the reader
can try right now and get better.** Two working tests apply it:

- **Density** — each tip is high-leverage and IdeaVim-true, with a payoff
  visible on the first try. Growth is not success; when a change won't raise
  density, cut instead of add.
- **The reader's seat** — the reader gets one tip *alone*, in random order, in a
  balloon that opens **collapsed to the title and summary**; details sit behind
  a chevron most readers never click. So the summary carries the tip by itself,
  and every line must say *which* behavior and *how* to try it on the spot. Read
  each line cold — wording defects are invisible from the author's seat.

Tips live in `tips/categories/<category>.json` (one file per primary category),
compiled into `tips/vim_tips_min.json` by `scripts/generate-tips.mjs`.

**Read [examples.md](examples.md) before authoring or rewording anything** —
style is taught by whole-tip before → after pairs, grouped like the wording
list below. Open the other companions only when their need comes up:

| File | Open when |
|---|---|
| [reference.md](reference.md) | proving IdeaVim supports or has released a key; authoring a `config` block; adding/renaming a category |
| [tagging.md](tagging.md) | setting or reviewing `advanced` or `mode` |
| [balloon.md](balloon.md) | a length or layout call — measured widths, fonts, the `config.name` budget |

## Every change — work this checklist

1. **Search first.** Grep the keys *and* the behavior across `tips/categories/`
   — the generator blocks only *identical* summaries; semantic duplicates are
   yours to catch. A category is its rendered set (primary + secondary tags):
   `grep -rn '"<cat>"' tips/categories/`, never one file alone.
2. **Verify support *and* release** in the `external/ideavim` submodule —
   reference.md → "Checking IdeaVim support" and "Is the key actually
   released?". The submodule runs ahead of the marketplace build; skipping the
   release half once shipped two tips readers could not use.
3. **Propose before editing.** Show each whole tip before → after with a
   one-line reason and get a go-ahead. Agree shape (how many tips, the split
   axis) before polishing words. After two rejected rewords, stop guessing
   single variants — offer 2–3 concrete options inline.
4. **Edit** `tips/categories/<primary>.json` — the file named by the tip's
   first category.
5. **Validate.** `node scripts/generate-tips.mjs --check` must pass — run it,
   don't reason about it (rules: docs/tips/tips-pipeline.md → Validation). Then
   `node .claude/skills/tips-maintain/check-lengths.mjs` — advisory, lists lines
   over the length targets.
6. **`git status --short`** — only intended files changed. Never commit
   `tips/vim_tips_min.json` (CI regenerates it; regenerate locally only on
   request). `M external/ideavim` after a submodule refresh is expected — leave
   it out of the commit.

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

- `category` — first entry is primary and matches the file name; add a 2nd/3rd
  only when it genuinely aids discovery.
- `summary` — command-first; at most one key or one clean pair (`gj / gk`).
  3+ keys: name the outcome, map each key in the details. **Renaming a summary
  resets that tip's hide preference** (the hide key hashes it) — reword only
  for a real improvement.
- `details` — prefer 2, 3 at most.
- `mnemonic` — optional, **omitted by default**; only when the decoded words
  make the keys stick; skip on 3-detail tips (examples.md → "Mnemonics").
- `config` — optional; read reference.md → "Config tips" first.
- `advanced`, `mode` — optional, **omitted by default**; read tagging.md first.

**Length targets** (advisory — `check-lengths.mjs` reports them, nothing
enforces them; derived in balloon.md): summary and detail lines **max 42
chars** (one balloon line), mnemonic **max 32** (its `Mnemonic: ` label shares
the line). Exceed one only when the wording truly needs it.

## Wording quick list

Each is the reader's-seat test made concrete; examples.md has the worked pair
under the same group.

- **Summary** — verb-first, concrete outcome; a typeable form over a
  placeholder (`griw`, not `gr{motion}`; `{count}`/`{char}` only for genuinely
  variable args). Keys attach with a plain space — never `-` `:` `→` `(…)`.
  Every keystroke shown must do something when typed (`Act on a class dac`,
  not `Select a class ac`). In a pair, vary one axis and keep the operator
  fixed, and keep the phrasing consistent (`next/previous`, `before/after`);
  symbol pairs join with `and` (`{ and }`), letter pairs with ` / `.
  When a plugin overlaps a built-in, the summary carries the differentiator.
- **Details** — line 1 spends itself on value, never a restatement of the
  summary. Show the effect, not the anatomy: one `input → output` example beats
  a letter or symbol dump. A transform names both ends. A motion tip names its
  use-site (`Perfect for ct) edits`). Name press-vs-type when typing crosses
  into Insert mode, and give setup and use their own lines. No
  Useful/Handy/Great openers, no numbered steps. Spell out abbreviations
  (`command-line`, not `cmdline`).
- **Mnemonics** — decode every key, one word per key, no `key =` echo; prefer
  the community reading; drop one whose decode is obvious.
- **Truth** — confirm a claim in IdeaVim source, not Vim lore: two keys earn a
  slashed pair only if they reach different handlers *and* results; a parsed
  `!` may be ignored; a `config` that sets the default is a no-op.
- **Shape** — each tip stands alone in knowledge, not just sequence. Split by
  *intent*, not key count (toggle `za` vs force `zo`/`zc`; `gj / gk` stays one
  pair). Merge a set-and-use pair when neither half stands alone. Sharpen a
  host's vague line instead of minting a sibling.

## Finding what to add — or cut

Adding well is mostly saying no. **Open-ended review:** triage
[docs/tips/tip-feedback.md](../../../docs/tips/tip-feedback.md) first — a
reader's report outranks any audit hunch; delete an entry once it's acted on.
Then map gaps against IdeaVim's real surface — the submodule's `CHANGES.md` and
`doc/IdeaVim Plugins.md` — grepping `tips/categories/` for each candidate key
or plugin. A miss is a candidate, not a verdict. When mining a release,
fast-forward the submodule first (reference.md → "Checking IdeaVim support").

Score candidates on four axes; a tip earns its place by winning at least 3:
**reach** (how many users hit it) · **leverage** (keystrokes/mouse trips saved)
· **IdeaVim fit** (IDE-bridge, plugin power, differs-from-upstream) ·
**teachability** (tryable on the spot, cold, in one balloon line).

The same axes prune: an existing tip losing on 3 is a removal candidate, and
pure deletion is a legitimate density win. Niche-but-standalone stays; cut only
redundant-with-a-stronger-sibling or actively counterproductive. Two gaps a
grep can't see: a command cluster taught only through its flags with no
foundational tip (29 tips taught `:s` trimmings before `:%s/foo/bar/g` itself
existed), and theory — a concept earns at most one tip and it must still be
tryable; otherwise fold one line into a concrete host. Advice about *writing*
config (`<leader>`, `nnoremap`) is not a tip — nothing to press.

Present a ranked shortlist (one-line rationale each, plus what you dropped),
get a go-ahead, then author survivors through the checklist.

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

## Keep this skill current

This skill evolves from use. When the user corrects a call, or a check catches
something the skill should have prevented, fold the lesson in during the same
session — sharpen the entry that failed before adding a new one:

| Lesson | Lands in |
|---|---|
| a wording or shape call | examples.md — a before → after pair under its group, plus a quick-list line here if it's a new rule |
| an IdeaVim truth or release trap | reference.md, with the command that proves it |
| a balloon layout or length fact | balloon.md (re-measure, don't estimate) |
| an `advanced` / `mode` pattern | tagging.md |
| a manual step that keeps recurring | propose a small script beside `check-lengths.mjs` |

There is no separate backlog — these files are the skill's memory.
