# Category Reference

The current category list and rules for choosing categories. For the broader
user-perspective rationale, see
[categories-user-perspectives.md](../categories-user-perspectives.md).

Current user-facing categories (this is also the order tips appear in the
published file). The canonical source of this order is the `categoryOrder`
array in `scripts/generate-tips.mjs`; this list and the one in
[tips-pipeline.md](../tips-pipeline.md) mirror it — see **Changing the category
set** below before editing any of them.

- `editing`
- `motion`
- `scroll`
- `insert`
- `change`
- `undo`
- `repeat`
- `visual`
- `cmdline`
- `options`
- `pattern`
- `map`
- `windows`
- `tabpage`
- `fold`
- `ideavim`
- `plugin`

Category notes:

- Use `pattern` instead of `search`.
- Use `cmdline` when entering `:` is a meaningful part of the tip, especially
  for Ex-only workflows such as file commands, ranged edits, `:action`,
  `:source`, split/tab Ex commands, and command-line history or editing.
- Do not add `cmdline` just because a tip mentions `:set` or `:map`. Keep
  `options` or `map` as the main discoverability category unless the command
  line itself is the workflow being taught.
- For Ex commands that directly change, copy, move, open, save, or close text or
  files, prefer `editing` plus `cmdline` together.
- Keep similar Ex workflows categorized consistently. If `:m` or `:t.` is
  discoverable through `editing`, then closely related commands such as
  `:t {address}` or bulk-edit uses of `:normal` usually should be too.
- Use `options` for tips whose main teaching point is a setting or toggle,
  including IdeaVim-specific `:set` options.
- Use `editing` for text-changing actions and file or buffer workflows.
- Remove `editing` when a stronger category already covers discovery well, such
  as pure `motion`, pure `options`, or `windows` tips whose main point is
  layout rather than editing.
- Use `plugin` only for tips that depend on an IdeaVim plugin or extension being enabled.
- Plugin-backed tips should usually also keep their functional category, such
  as `motion`, `editing`, `visual`, or `windows`.
- For text-object tips, use `editing` by default. Use `visual` when the summary
  is explicitly about selecting text.
- Use `ideavim` for IdeaVim-specific behavior that is not necessarily tied to a plugin.
- Do not create ad hoc category names without updating the broader taxonomy
  deliberately. Adding, removing, or renaming a category touches several places
  at once — see **Changing the category set** below.

## Changing the category set

The category set is coupled across code and docs. When you add, remove, or
rename a category, update all of these so they stay in sync:

1. **`scripts/generate-tips.mjs`** — the `categoryOrder` array. This is the
   source of truth for ordering; a category not listed here is appended
   alphabetically (see [tips-pipeline.md](../tips-pipeline.md)).
2. **`tips/categories/<name>.json`** — the authoring file. Adding a category
   means a new file; removing one means migrating or deleting its tips first
   (a tip's first category must match its file name).
3. **This file (`categories.md`)** — the list above and any category notes.
4. **[tips-pipeline.md](../tips-pipeline.md)** — the mirrored order in its
   "Ordering" section.
5. **[categories-user-perspectives.md](../categories-user-perspectives.md)** —
   the user-perspective rationale, if the change affects it.

After any change, run `node scripts/generate-tips.mjs` to confirm the sources
still validate.
