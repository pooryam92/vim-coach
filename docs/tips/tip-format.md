# Tip Format

How to write a single tip object in `tips/categories/<category>.json`. See
[README](README.md) for the overall loop and [display-and-wording.md](display-and-wording.md)
for how it renders.

## By example

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
[Wording](display-and-wording.md#wording-good-vs-worse).

## Field rules

- `category` — array; first entry is primary and must match the file name. One
  category by default; add a 2nd/3rd only when it genuinely helps discoverability.
- `summary` — one command-first line; says what to do or what you gain. **≤ 35
  chars** (see [Display](display-and-wording.md#display)).
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

## Config-tip kinds (additive)

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
name (see [Wording](display-and-wording.md#wording-good-vs-worse)).

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
