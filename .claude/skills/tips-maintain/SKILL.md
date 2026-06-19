---
name: tips-maintain
description: Add, edit, improve, reword, review, or maintain Vim Coach tips, and keep this skill itself up to date. Use when asked to write a new tip, fix or reword an existing tip, add or change a category, regenerate vim_tips_min.json, or work in tips/categories/.
allowed-tools: Bash(node scripts/generate-tips.mjs*) Bash(node scripts/lint-tips.mjs*) Bash(grep*) Bash(git status*)
---

# Maintaining Vim Coach tips

You are the **maintainer** of the Vim Coach tip set: you add, revise, and improve
tips, and you co-edit them with the user on request. Tips are authored by hand in
`tips/categories/<category>.json` (one file per primary category) and compiled
into the single published file `tips/vim_tips_min.json` by
`scripts/generate-tips.mjs`. That script is the harness — `--check` validates
every tip, and a bare run rebuilds the artifact.

**Never edit `tips/vim_tips_min.json` by hand, and never regenerate it unless
explicitly asked.** It is generated, and **CI rebuilds and commits it from the
sources** — leave it out of your commits. Validate with
`node scripts/generate-tips.mjs --check` (writes nothing). Run the bare
generator (which rewrites the artifact) only when the user asks for it. Build
details: `docs/tips/tips-pipeline.md`.

Paths below are relative to the repo root. Everything needed to write or reword a
tip is in this file; open **`reference.md`** for IdeaVim support-checking, the
worked before→after examples, and adding a new category.

> **This skill is self-evolving — that is part of the job, not an afterthought.**
> You own and actively maintain your own files: `SKILL.md`, `reference.md`, the
> helper scripts (`scripts/generate-tips.mjs`, `scripts/lint-tips.mjs`), and any
> new file or script that earns its place. Whenever an interaction teaches a
> durable rule, a stated preference, an IdeaVim/generator quirk, or a smoother
> workflow, **fold it into these files in the same change** — add, edit, split,
> or delete as needed. Any session that teaches something durable should leave
> the skill sharper than you found it. The full procedure is *Evolving this
> skill* at the end; treat it as a standing instruction, not an optional extra.

## How you work

- **Revise in small batches (≤ 2 tips).** For each, show the **whole tip
  before → after** plus a one-line reason, get a go-ahead, *then* edit. Never
  bulk-rewrite silently.
- **Search before adding.** `grep -rn` the keys *and* the behavior across
  `tips/categories/` — the generator only catches identical summaries, so a
  duplicate idea under different wording is yours to catch. If covered, merge or
  drop instead of adding.
- **Verify support before keeping a claim** (see `reference.md` → "Checking
  IdeaVim support"). Don't carry over upstream-Vim behavior IdeaVim doesn't
  replicate.
- **When improving, scan for:** bad/duplicate categories, misleading or
  overloaded details, plugin/option-name-first summaries, label-style summaries,
  mixed pair phrasing, stray `-`/`:` key separators, bare text objects shown as
  commands, and tips that only make sense after another (random order breaks
  them). `node scripts/lint-tips.mjs` surfaces most of these mechanically (it is
  advisory — eyeball its hits).
- **Always close the loop** (below). An edit isn't done until `--check` has
  rerun clean.

## The loop (every change)

1. Edit the right `tips/categories/<primary-category>.json`. A tip lives in the
   file named by its **first** category.
2. Validate (writes nothing):
   ```bash
   node scripts/generate-tips.mjs --check
   ```
   Prints `validated <N> tips` on success; exits non-zero on a validation error
   (it names the offending file + tip). Treat it as the source of truth for
   validation — run it rather than reasoning about whether a tip is valid. Then
   run `node scripts/lint-tips.mjs` for the advisory quality checks.
3. Confirm only what you intended changed:
   ```bash
   git status --short
   ```

Commit **only the source change** — `tips/vim_tips_min.json` is regenerated and
committed by CI, so it should not appear in your commit. Regenerate it yourself
(bare `node scripts/generate-tips.mjs`) only when the user explicitly asks.

## Tip object shape

Plain tip:

```json
{
  "category": ["navigation"],
  "summary": "Jump to matching bracket %",
  "details": [
    "Cursor on ( [ { jumps to its pair",
    "Works in Normal and Visual mode"
  ]
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
  file name**. One category by default; add a 2nd/3rd only when it genuinely aids
  discovery.
- **`summary`** — one command-first line; what to do or what you gain. **≤ 35
  chars.** When it ends with the keys it teaches, attach them with a **single
  space** — never a `-`, `:`, `→`, or `(…)`.
- **`details`** — short factual lines (≤ 35 chars each): what it does, context, a
  caveat, or a quick example. Two short lines beat one wrapped line. Blank lines
  are stripped silently — don't add them.
- **`config`** (optional) — object `{ "name": ..., "lines": [...] }` for the
  `.ideavimrc` snippet the button appends. `lines` are **enable** lines only,
  written verbatim — never usage mappings (`ysiw)` is usage, not config). `name`
  is the button label, used verbatim; omit it for a generic `Apply`. Legacy array
  form `["<line>", ...]` is still accepted.

### The three additive config kinds

Config tips must be one of these (each line is unique + order-independent, so the
button can safely append it):

1. **Install a plugin** — one `Plug '<github-alias>'` line. Primary category
   `plugins` + a functional secondary. Aliases:
   `external/ideavim/doc/IdeaVim Plugins.md`. Use `Plug`, not the legacy
   `set <plugin>` form.
2. **Tune a built-in option** — e.g. `set scrolloff=5`, `hlsearch`. Primary
   `options`.
3. **IDE-bridge `set`** — e.g. `set ideajoin`, `set idearefactormode=keep`.
   Primary `ideavim`.

Anything that claims keys or sets shared state is *positional* and not shippable
yet — don't author it.

## Wording rules

- **Command-first, concrete user outcome over Vim taxonomy.** Verb-first, not a
  noun label.
- **Keys attach with a plain space only** (enforced across the whole set) — never
  a `-`/`:`/`→`/`(…)` separator. `with` is allowed only as genuine prose
  (`Show line numbers with number`), not as a separator.
- **Every keystroke shown must do something when typed.** A bare text object
  (`iw`, `ac`, `ii`, `ai`, `am`) does nothing alone — lead with an operator.
- **Each tip stands alone — in knowledge, not just sequence.** Display order is
  random, so a tip may never assume the reader just saw another — *and* may not
  assume they already grasp the concept it teaches. Fold a dependent point into
  its host tip, or split an overloaded tip into self-contained ones (only when
  each earns a distinct summary).
- **Spell out abbreviations in user-facing text.** Write `command-line` (and
  `command line`), not `cmdline` — `cmdline` is the category slug only. Short
  forms like `char`, `msg`, `prev` are fine only when spelling out would wrap.
- **Name modes explicitly when context helps:** `Normal mode`, `Insert mode`,
  `Visual mode`.
- **For IdeaVim/plugin tips, put the user outcome in the summary, the
  plugin/option name in `config`/details.**
- **Don't restate the summary in detail line 1** — it's the most-read line; spend
  it on the mechanic, value, or a mnemonic. For a jargon-heavy command, give a
  typeable example plus its plain-words result (`:v/foo/d deletes lines lacking
  foo`), not the syntax anatomy (`v = non-matching lines, d = delete`) — naming
  tokens still leans on the concept a newcomer lacks. Read each line cold: if it
  only parses once you know the concept, rewrite it.
- **Never dump alternative keys as a symbol list** — name the family in prose
  (`Any bracket or quote works too`) and keep at most one concrete example.
- **Consistent pair phrasing** — `next/previous`, `before/after`, `top/bottom`.
  In a slashed pair, vary one axis and keep the operator fixed.

| Good | Worse | Why |
|---|---|---|
| `Repeat last substitution with &` | `Use the substitute repeat command` | command-first, concrete outcome |
| `Next/previous tab gt / gT` | `Next and previous tab gt / gT` | consistent pair phrasing |
| `Show line numbers with number` | `Line numbers` | action/outcome, not a noun label |
| `Add surroundings ys{motion}` | `Add surroundings - ys{motion}` | keys attach with a space, never a dash |
| `Add surroundings ys{motion}` | `Surround text with vim-surround` | outcome + keys in summary; plugin name lives in `config` |
| `Act on a class dac / cic` | `Select a class ac` | `ac` alone does nothing; `dac`/`cic` act |

Display context: tips render in a 240px IntelliJ balloon (~30–35 chars/line, only
~2 wrapped body lines visible). Keep summaries to one line.

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
code+docs change — see `reference.md`.

## Self-checks

```bash
node scripts/lint-tips.mjs
```

`lint-tips.mjs` is advisory (never gates, always exits 0). It reports
over-length summaries/details, possible stray separators, legacy-array config
tips, and possible duplicate keys taught twice — the soft checks the generator
skips. Some hits are false positives by design (a `-` that is part of the keys
like `Ctrl-w + / -`; a key like `%` that legitimately appears in several tips),
so eyeball each before acting.

## Gotchas (verified)

- **`tips/vim_tips_min.json` is generated, and CI regenerates + commits it.**
  Edit `tips/categories/*.json`; validate with `--check`; do **not** regenerate
  or commit the published file unless asked. Hand edits to it are overwritten.
- **Duplicate summaries hard-fail** the generator, across *all* files — not just
  within one. The same idea under a *different* summary slips through, so search
  first.
- **First category must match the file name**, or the generator fails.
- **Renaming a summary resets the user's hide preference** for that tip (the hide
  key is a hash of the trimmed summary). Reword only when it's a real improvement.

## Evolving this skill

The standing mandate is in the callout at the top; this is the procedure for it.
Run it whenever a change earns it, or on demand via the `/tips-evolve` command.

1. **Recognize the trigger.** Watch for (a) the user correcting a tip or workflow
   in a way that reveals a *general* rule, not a one-off; (b) a new stated
   preference or constraint; (c) an IdeaVim quirk or generator/script behavior
   worth recording; (d) a repeated manual step that a script or check could own.
2. **Classify before acting.** Durable + generalizable → fold in; one-off → skip
   (don't bloat the skill). A must-fire wording/format/workflow rule → this
   `SKILL.md`; an example, edge case, or support finding → `reference.md`; a
   mechanical check or repeated chore → a helper script. Adding a new file is
   fine when it pulls real weight; prefer extending an existing one otherwise.
3. **Apply it, don't just note it.** Show the exact change — the rule or tool
   **plus the before→after (or interaction) that prompted it** — and a one-line
   reason. For a small, unambiguous edit the user just asked for, apply it
   directly; for anything judgment-heavy, get a quick go-ahead first. Dedupe
   against what's already here; delete rules that turn out wrong.
4. **It rides the same change** as the work that taught it, so the rule/tool and
   the tip edits commit together.

Use the agent's own **memory** (not this file) only for the user's *personal*
workflow taste that isn't a project rule (e.g. preferred batch size); project
authoring rules belong in the skill.
