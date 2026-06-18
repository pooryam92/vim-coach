# Categories

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

## Picking categories

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

## Changing the category set

Coupled across code + docs — update all together: (1)
`tips/categories/<name>.json` (add = new file; remove = migrate/delete its tips
first); (2) the table above + picking rules; (3)
[../discover/config-tips-roadmap.md](../discover/config-tips-roadmap.md) if it
affects the config roadmap. Ordering needs no change — categories sort alphabetically
automatically. Then run `node scripts/generate-tips.mjs` to confirm it validates.
