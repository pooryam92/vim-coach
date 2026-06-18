# Vim Tips Authoring

Everything for **adding** and **improving** tip content in
[tips/categories/](../../tips/categories): tip format + examples, categories, and
support checking. The published file is generated from these sources — see
[tips-pipeline.md](tips-pipeline.md). Not-yet-shippable config work (positional
maps, leader convention, key-aware dedup) is tracked in
[../discover/config-tips-roadmap.md](../discover/config-tips-roadmap.md).

A good tip: one concrete command, correct for IdeaVim, easy to scan, easy to act
on now, findable by category.

## Where tips live & the loop

- Edit `tips/categories/<primary-category>.json` — one object with a `tips`
  array, one item per tip. A tip lives in the file named by its **first**
  category (first category must match the file name).
- Never hand-edit the generated `tips/vim_tips_min.json`.
- After any change: `node scripts/generate-tips.mjs` (fix the named tip if it
  errors, rerun), then commit the category file.

**Add a tip:** pick category → write the object → confirm IdeaVim supports it →
regenerate + commit.
**Improve tips:** scan for the issues under [Improving tips](#improving-tips) →
fix wording/categories/support → regenerate + commit.

---

## Tip format (by example)

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

Config-backed tip (shows an **Add to .ideavimrc** button):

```json
{
  "category": ["plugins", "editing"],
  "summary": "Add surroundings ys{motion}",
  "details": [
    "ysiw) wraps a word in parens",
    "ys$\" quotes to end of line"
  ],
  "config": {
    "name": "Install vim-surround",
    "lines": ["Plug 'tpope/vim-surround'"]
  }
}
```

Note what the summary does **not** say: the plugin name `vim-surround` lives in
`config`, not the summary. Lead with the user outcome + keys (`Add surroundings
ys{motion}`), never the plugin name (`Surround text with vim-surround`) — see
[Wording](#wording-good-vs-worse).

**Field rules**

- `category` — array; first entry is primary and must match the file name. One
  category by default; add a 2nd/3rd only when it genuinely helps discoverability.
- `summary` — one command-first line; says what to do or what you gain. **≤ 35
  chars** (see [Display](#display)).
- `details` — short factual lines: what it does, context, a caveat, or a quick
  example. Aim ≤ 35 chars each; two short lines beat one wrapped line.
- `config` (optional) — an object `{ "name": ..., "lines": [...] }` describing
  the `.ideavimrc` snippet the button appends (only when IdeaVim is installed and
  the file already exists; never creates it).
  - `lines` — the `.ideavimrc` line(s), written verbatim (order/duplicates
    preserved; blanks dropped). **Enable** lines only, not usage mappings
    (`ysiw)` is *usage*, not config).
  - `name` (optional) — the button label, used **verbatim** (e.g. `Install
    vim-surround` → a button reading exactly that). Omit it to get the generic
    `Apply` label. Not limited to plugins — any named snippet can use it.
  - For plugins author `Plug '<github-alias>'`, not the legacy `set <plugin>`
    form (IdeaVim flags it via `UsePlugSyntaxInspection`). Aliases:
    `external/ideavim/doc/IdeaVim Plugins.md`.
  - The legacy array form `"config": ["<line>", ...]` is still accepted (treated
    as `lines` with no `name`), but new tips should use the object form.

**The generator rejects** (see [tips-pipeline.md](tips-pipeline.md)): a blank
summary; a tip with no details; a `summary` duplicated by any other tip in any
file; a first `category` that doesn't match the file name. Blank detail lines are
stripped silently — don't add them.

### Config-tip kinds (additive)

These three config kinds are **additive** — each `config.lines` entry is unique
and order-independent, so the **Add to .ideavimrc** button can safely append it
to an existing file today (exact-match dedup, no leader, no key collisions).
Author config tips in one of these three forms; anything that claims keys or sets
shared state is *positional* and not shippable yet (see
[../discover/config-tips-roadmap.md](../discover/config-tips-roadmap.md)).

**1. Install a plugin** — a single official-plugin entry in `Plug` form.

```json
"config": {
  "name": "Install vim-surround",
  "lines": ["Plug 'tpope/vim-surround'"]
}
```

Use the `Plug '<github-alias>'` form, not the legacy `set <plugin>` form. Aliases
come from `external/ideavim/doc/IdeaVim Plugins.md`. Keep `plugins` as the first
category, add a functional secondary (`editing`, `navigation`, `pattern`,
`files`), and teach one concise usage in the summary/details — not the plugin
name (see [Wording](#wording-good-vs-worse)).

**2. Tune built-in behavior** — a Vim option toggle/setting.

```json
"config": {
  "name": "Keep 5 lines of context",
  "lines": ["set scrolloff=5"]
}
```

Examples: `ignorecase smartcase`, `hlsearch`, `number`. Primary category
`options`.

**3. IDE-bridge options** — an IdeaVim-specific `set` that wires Vim to the IDE.

```json
"config": {
  "name": "Use IDE join",
  "lines": ["set ideajoin"]
}
```

Examples: `set ideajoin`, `set idearefactormode=keep`, `set
clipboard+=unnamedplus`. Primary category `ideavim`.

In all three, `lines` holds **enable** lines only — never usage mappings (`ysiw)`
is usage, not config). See [Append Planning](../features/ideavimrc-button.md) for
how the button matches and appends a snippet.

## Display

Tips show in an IntelliJ balloon. Title is the static "Vim Coach"; `summary`
renders **bold** on top, `details` below, all as HTML. The body is 240px wide
(`BalloonLayoutConfiguration.MaxWidthStyle`) — roughly **30–35 chars/line** before
wrapping, and only ~2 wrapped body lines show before the user must scroll. So keep
the summary on one line and details short, or details get pushed out of view.
Abbreviations (`char`, `msg`, `prev`) are fine when spelling out would wrap; drop
filler like `with` when the command already makes the relationship clear
(`Replace one character r{char}`).

**Keystroke separator (REQUIRED — no exceptions):** when a summary ends with the
keys it teaches, attach them with a **single space** and nothing else. Do **not**
use a dash, colon, arrow, or parentheses as a separator. The whole tip set relies
on this so the keys form a scannable command-first column; a stray ` - ` makes the
tip an outlier (it appears in zero other categories).

| Use this | Never this |
|---|---|
| `Add surroundings ys{motion}` | `Add surroundings - ys{motion}` |
| `Change surroundings cs` | `Change surroundings: cs` |
| `Surround a selection S` | `Surround a selection (S)` |
| `Wrap in an HTML tag ysiwt` | `Wrap in an HTML tag → ysiwt` |

`with` stays allowed only as genuine prose linking words (`Show line numbers with
number`), never as a substitute separator when a plain space reads cleaner
(`Delete surroundings ds`, not `Delete surroundings with ds`).

**Identity:** a user hiding a tip is keyed to a hash of its trimmed `summary`.
Renaming a summary resets any existing user preference for it — reword when it's
an improvement, but know the cost.

## Wording: Good vs. Worse

| Good | Worse | Why |
|---|---|---|
| `Repeat last substitution with &` | `Use the substitute repeat command` | command-first, concrete outcome |
| `Next/previous tab gt / gT` | `Next and previous tab gt / gT` | consistent pair phrasing |
| `Browse command-line history Ctrl-p / Ctrl-n` | `Browse cmdline history …` | spell out `command-line` in user text |
| `Show line numbers with number` | `Line numbers` | action/outcome, not a noun label |
| `Open help with :help` | `Built-in help :help` | verb-first, not a taxonomy label |
| `Add surroundings ys{motion}` | `Add surroundings - ys{motion}` | keys attach with a space, never a dash ([separator rule](#display)) |
| `Add surroundings ys{motion}` | `Surround text with vim-surround` | outcome + keys in summary; plugin name lives in `config`/details |

- Command-first, concrete, user outcome over Vim taxonomy. Verb-first over labels.
- Attach trailing keys with a plain space, never a `-`/`:`/`(…)` separator — see
  the [enforced separator rule](#display).
- Make mode/prompt context explicit only when it helps; name modes `Normal mode`
  / `Insert mode` / `Visual mode`.
- Reuse pair phrasing across similar tips: `next/previous`, `before/after`,
  `top/bottom`. Prefer `command line` in user text; reserve `cmdline` for the slug.
- One strong tip over two near-duplicates — merge overlap with multiple
  categories. Split a dense line; no semicolons joining prose (unless `;` is the
  key taught).
- For IdeaVim/plugin tips put the user outcome in the summary, the plugin/option
  name in details. Avoid config-authoring summaries unless config *is* the workflow.

---

## Categories

Primary category = the file the tip lives in. Categories are emitted
alphabetically by name (tip selection is random at runtime, so order is
cosmetic — see [tips-pipeline.md](tips-pipeline.md#ordering)).

| Category | Job (← old slugs) |
|---|---|
| `navigation` | move through code (motion + scroll + fold) |
| `editing` | change text (+ change + undo) |
| `registers` | manage what you've copied (split from editing) |
| `visual` | select precisely |
| `insert` | type faster while inserting |
| `repeat` | repeat / automate an edit |
| `pattern` | search & replace (use instead of `search`) |
| `cmdline` | drive the IDE from `:` |
| `files` | open, switch, save, close files (split from editing) |
| `windows` | splits & tabs, layout only (+ tabpage) |
| `options` | tune behavior |
| `mappings` | reshape the keyboard (was `map`) |
| `ideavim` | IDE-bridge behavior, not plugin-specific |
| `plugins` | needs an IdeaVim plugin/extension enabled |

**Picking categories**

- Use one primary; add secondaries only when they genuinely aid discovery.
- `navigation` for any cursor movement (word/line/search motions, scroll, folds).
- `editing` for text-changing actions; `registers` for yank/paste/registers
  (`"a`, `"0`, `:put`); `files` for file/buffer workflows. Drop `editing` when a
  stronger category (pure `navigation`/`options`/`windows`) already covers it.
- `cmdline` when entering `:` is the point (Ex-only workflows: file commands,
  ranged edits, `:action`, `:source`). **Not** just because a tip mentions `:set`
  or `:map` — keep `options`/`mappings` as the main category then. Ex commands
  that edit text in place → `editing` + `cmdline`; that open/switch/save/close
  files → `files` + `cmdline`.
- `options` when the teaching point is a setting/toggle (incl. IdeaVim `:set`).
- text objects → `editing` by default, `visual` when the summary is about
  selecting.
- `plugins` only when a plugin must be enabled — usually keep the functional
  category too. `ideavim` for IdeaVim-specific behavior not tied to a plugin.
- Keep similar Ex workflows categorized consistently (`:m`, `:t.`, `:normal`).

### Changing the category set

Coupled across code + docs — update all together: (1)
`tips/categories/<name>.json` (add = new file; remove = migrate/delete its tips
first); (2) the table above + picking rules; (3)
[../discover/config-tips-roadmap.md](../discover/config-tips-roadmap.md) if it
affects the config roadmap. Ordering needs no change — categories sort alphabetically
automatically. Then run `node scripts/generate-tips.mjs` to confirm it validates.

---

## Checking support

Vim docs give *meaning*; IdeaVim data/source proves *support*. Be conservative
when ambiguous. Bang forms (`:e!`, `:q!`) can be valid even when only the base
command is indexed; some pattern/mapping behaviors are syntax inside supported
commands, not standalone commands.

**Before keeping a tip:** command/behavior clearly supported by IdeaVim? Summary
honest about mode/prompt/plugin requirements? Plugin-backed → tagged `plugins`?
IdeaVim-specific but not a plugin → is `ideavim` enough? Useful with no repo
context?

**Local KSP data** (first check) — generated JSON in the IdeaVim submodule. Fetch
+ focus the checkout:

```bash
git submodule update --init external/ideavim
git -C external/ideavim sparse-checkout init --cone
git -C external/ideavim sparse-checkout set \
  src/main/resources/ksp-generated \
  vim-engine/src/main/resources/ksp-generated
git submodule update --remote external/ideavim   # refresh to latest master
```

- `engine_*` (core engine): `commands`, `ex_commands`, `vimscript_functions` under
  `external/ideavim/vim-engine/src/main/resources/ksp-generated/`.
- `frontend_*` (frontend-only: `:buffer`, `:ls`, `:help`, `:read`, `:actionlist`…)
  under `external/ideavim/src/main/resources/ksp-generated/`.
- `ideavim_extensions.json` (both paths) — check before claiming a plugin exists.

**Vim docs** — reference https://vimhelp.org/, user manual
https://vimhelp.org/usr_toc.txt.html. Category → page: `editing`→editing.txt,
`navigation`→motion.txt/scroll.txt/fold.txt, `pattern`→pattern.txt,
`cmdline`→cmdline.txt, `options`→options.txt, `visual`→visual.txt,
`mappings`→map.txt, `windows`→windows.txt/tabpage.txt. Check nearby `usr_*.txt`
chapters for workflow-oriented material.

**Full IdeaVim source** — the submodule is the source of truth (don't browse
GitHub). Widen the sparse checkout when you need more than KSP JSON:

```bash
git -C external/ideavim sparse-checkout add \
  annotation-processors vimscript-info src vim-engine
```

Setup/usage docs not in the tree: [IdeaVim wiki](https://github.com/JetBrains/ideavim/wiki).

---

## Improving tips

Scan existing tips for:

- bad/inconsistent categories or duplicate teaching points
- misleading details, or tips too broad/overloaded
- setup or config-authoring advice that belongs in docs, not the tip set
- summaries leading with a plugin/option name when the user outcome is clearer
- label-style summaries, mixed pair phrasing, mode-name drift, formatting noise
- dash/colon/paren key separators in summaries — keys attach with a space only
  (search the sources for ` - ` to catch strays)
- tips to merge, split, retag, or delete

Use the [Wording table](#wording-good-vs-worse) and [Categories](#categories)
rules. Remember the summary-hash [identity](#display) cost before rewording.
