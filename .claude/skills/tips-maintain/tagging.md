# Tagging a tip — `advanced` and `mode`

On-demand companion to `SKILL.md`. Open it before adding, removing or reviewing
either flag. Both are optional and **omitted by default**; the generator rejects
a non-boolean `advanced` and a `mode` outside `insert` / `visual` / `command`.

## `advanced`

`"advanced": true` hides a tip from newcomers' default rotation; users who opt
in from settings still see it, under a `Vim Coach · Advanced` title. There is no
fixed rubric — it **emerges from doing**. Bias hard toward normal: over-tagging
shrinks newcomers' default pool, which is the harm. Tag a few at a time, and
when a pattern for "too advanced for a newcomer's first week" starts to repeat,
add it to the list below.

- **Count the category's ratio before tagging** — the settled convention is in
  the rendered set (primary + secondary tags) and differs sharply by category.
  Count it, don't recall it:
  ```bash
  node -e 'const fs=require("fs"),c=process.argv[1],d="tips/categories/";const s=fs.readdirSync(d).flatMap(f=>JSON.parse(fs.readFileSync(d+f)).tips).filter(t=>t.category.includes(c));console.log(c,s.filter(t=>t.advanced).length+"/"+s.length)' pattern
  ```
  Match the siblings you're landing beside rather than scoring the tip alone.
- **`plugins` stays low** — needing a `config` block is *itself* the opt-in, so
  a plugin's headline move is normal even when its concept is deep (YankRing's
  paste-cycling, `ysiw)`, `gcc`, `dam`). Tag only secondary or niche moves —
  `ysiwf`, `gciw`, `cxiw`, `:S`, `cin)`, `]=`/`]+` indent hops.
- **`pattern` runs high** — a `:s`/search **flag** riding inside a pattern
  (`gc`, `//`, `\c`, `\<\>`, `/n`) is reliably past a newcomer's first week.
- **Foundational base moves stay normal even when their variants are
  advanced** — macros (`qa` / `@a`), a named register (`"ayy`), `:action`, the
  Ctrl-v block. Hiding the base leaves newcomers seeing only its variants.
  Untag a family together, never one member.
- **A first-week trap's way out stays normal** — when a normal tip shows a
  newcomer hitting a problem (a delete overwrites the yank `p` pastes), the
  move that avoids it (`"_dd`) is normal too, not only the after-the-fact fix
  (`"0p`).
- **Refinements of a base move are advanced** — `g_` beside `$`, `gp`/`]p`
  beside `p`, `g0`/`g$` beside `gj`/`gk`, `O` in a Visual block, the change
  marks `'[`/`']`. So are manual folds (`zf`), because the IDE already makes
  folds, and Insert-mode completion submodes (`Ctrl-x Ctrl-l`), because the IDE
  popup already handles the common case.

## `mode`

`mode` renders a dimmed `Vim Coach · Insert mode` title label (`command` shows
as `Command mode`) so a tip read cold isn't mistaken for a Normal-mode move. It
is informational only — it does not hide, gate or de-duplicate anything.

- **Tag the press mode** — set it when the whole tip lives in one non-Normal
  mode: an Insert-mode register paste (`Ctrl-r "`), a Visual-mode operator, a
  `:` command-line edit.
- **Never tag Normal**, and never tag a move that *enters* a mode from Normal
  (`ciw`, `v`, `ma`, `:s`, `i`/`a`/`o`) — the reader starts in Normal, even when
  the tip lives in `insert.json` or `visual.json`.
- **The label replaces a `Works in X mode` detail line** — delete that line
  when you set `mode` (examples.md → "Use the `mode` label, not a mode line").
  The mode word stays in the *wording* only when it's teaching payload: leaving
  Insert (`Leave Insert mode with Esc`), the `Ctrl-o` dip (`One Normal command,
  back to Insert`), or a key that means different things per mode.
