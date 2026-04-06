# Vim Tips Reference

Use this document when adding, revising, or reviewing tips in
[tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json).

## Working File

- Main tip file: `tips/vim_tips.json`
- Root shape: one JSON object with a `tips` array
- Each array item is one user-facing tip

## Local Support Data

Use the generated KSP JSON files in `tips/` as the first local support check for
whether a command, Ex command, Vimscript function, or IdeaVim extension exists.

- Engine command data:
  - `tips/ksp-generated/engine_commands.json`
  - `tips/ksp-generated/engine_ex_commands.json`
  - `tips/ksp-generated/engine_vimscript_functions.json`
- Frontend command data:
  - `tips/ksp-generated-frontend/frontend_commands.json`
  - `tips/ksp-generated-frontend/frontend_ex_commands.json`
  - `tips/ksp-generated-frontend/frontend_vimscript_functions.json`
- Extension data:
  - `tips/ksp-generated/ideavim_extensions.json`
  - `tips/ksp-generated-frontend/ideavim_extensions.json`

Use them like this:

- Check `engine_*` files for core Vim engine support.
- Check `frontend_*` files for frontend-only commands such as `:buffer`,
  `:buffers`, `:files`, `:ls`, `:help`, `:read`, and `:actionlist`.
- Check the extension JSON files before claiming a plugin-backed IdeaVim
  extension exists.
- If the KSP data confirms existence but not user-facing behavior, use Vim docs
  for semantics and IdeaVim docs/source for plugin setup or runtime details.

## Tip Goals

A good tip should be:

- easy to scan
- easy to act on
- correct for IdeaVim
- easy to find through categories
- focused on one concrete command or workflow

Prefer tips that teach an action the user can try immediately over abstract
taxonomy or reference-style wording.

## Tip Format

Each tip uses this shape:

```json
{
  "category": [
    "motion"
  ],
  "summary": "Command-first summary",
  "details": [
    "What it does",
    "Why or when to use it"
  ]
}
```

Field rules:

- `category`
  - array of category names
  - use one primary category by default
  - add a second or third category only when it genuinely helps discoverability
- `summary`
  - one short command-first line
  - should tell the user what to do or what they gain
- `details`
  - short factual lines
  - explain what the command does, context, caveats, or a quick example

Invalid content:

- blank summaries
- blank detail lines
- duplicate tip entries that only differ by category
- unsupported claims about IdeaVim behavior

## Writing Style

Use these rules when writing or revising tip text:

- Keep summaries command-first and concrete.
- Prefer user outcome over Vim taxonomy.
- Keep details short and factual.
- Make mode or prompt context explicit when it matters.
- Prefer one strong tip over two near-duplicates.
- Merge duplicate teaching points by using multiple categories instead of repeating the tip.
- Keep wording practical, not encyclopedic.
- Prefer verb-first summaries over label-style summaries.
- Use the same pair phrasing across similar tips, such as `next/previous`,
  `before/after`, and `top/bottom`.
- In user-facing text, prefer `command line` or `command-line` over
  `cmdline`. Reserve `cmdline` for category names or Vim terms that already use
  it.
- When mode context matters, name modes consistently as `Normal mode`,
  `Insert mode`, and `Visual mode`.
- Prefer action-oriented summaries like `Open help with :help` over taxonomy
  labels like `Built-in help :help`.
- Prefer consistent separators and avoid incidental formatting drift such as
  trailing spaces in summaries.
- Keep prose lines wrapped to a reasonable width in docs and review notes.
- When a summary or detail line starts feeling dense, split it into multiple
  lines instead of packing more clauses into one string.
- Do not use semicolons to join prose in summaries or detail lines. Split the
  thought into separate lines instead, unless `;` is the actual Vim key or part
  of the command being taught.
- For IdeaVim and plugin-backed tips, prefer the user outcome in the summary and
  keep the plugin or option name in the details when possible.
- Avoid config-authoring summaries unless the configuration step is itself the
  main user-facing workflow.

Examples:

- Good: `Repeat last substitution with &`
- Worse: `Use the substitute repeat command`
- Good: `Replace a selection with p`
- Good: `Run a substitute only on matching lines with :g`
- Good: `Next/previous tab gt / gT`
- Better than: `Next and previous tab gt / gT`
- Good: `Browse command-line history Ctrl-p / Ctrl-n`
- Better than: `Browse cmdline history Ctrl-p / Ctrl-n`

### Lessons From The Phrasing Pass

- The file was already structurally consistent, but summary wording drifted over
  time even when the underlying tip quality was fine.
- The most common drift was small wording variation across near-identical
  concepts, such as `next/previous` versus `next and previous`.
- Mode names need to be intentional. Use `Insert mode`, `Visual mode`, and
  `Normal mode` only when the mode context helps the user act on the tip.
- Command-line tips are easier to scan when `command line` or `command-line` is
  spelled out in user-facing text instead of `cmdline`.
- Option tips read better when phrased as outcomes or actions, such as `Show
  line numbers with number`, instead of noun labels like `Line numbers`.
- A useful cleanup heuristic is to scan for label-style summaries, mixed pair
  phrasing, mode-name drift, and formatting noise before looking for deeper
  content issues.

## Category Reference

Current user-facing categories:

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
- Do not create ad hoc category names without updating the docs and the broader
  taxonomy deliberately.

## Resources

Use Vim docs for meaning and teaching value. Use IdeaVim docs and source for support checks.

### Vim docs

- Reference manual: https://vimhelp.org/
- User manual table of contents: https://vimhelp.org/usr_toc.txt.html

Useful starting pages by category:

- `editing` -> `editing.txt`
- `motion` -> `motion.txt`
- `scroll` -> `scroll.txt`
- `insert` -> `insert.txt`
- `change` -> `change.txt`
- `undo` -> `undo.txt`
- `repeat` -> `repeat.txt`
- `visual` -> `visual.txt`
- `cmdline` -> `cmdline.txt`
- `options` -> `options.txt`
- `pattern` -> `pattern.txt`
- `map` -> `map.txt`
- `windows` -> `windows.txt`
- `tabpage` -> `tabpage.txt`
- `fold` -> `fold.txt`

Also check nearby `usr_*.txt` chapters when a section feels thin. The user
manual often gives better beginner and workflow-oriented material than the
reference pages alone.

### IdeaVim support sources

- Local generated support data in `tips/ksp-generated/` and
  `tips/ksp-generated-frontend/`
- https://github.com/JetBrains/ideavim
- https://github.com/JetBrains/ideavim/wiki
- https://github.com/JetBrains/ideavim/tree/master/annotation-processors
- https://github.com/JetBrains/ideavim/tree/master/vimscript-info
- https://github.com/JetBrains/ideavim/tree/master/src
- https://github.com/JetBrains/ideavim/tree/master/vim-engine

## Support Checklist

Before keeping a tip, check:

- Is the underlying command or behavior clearly supported by IdeaVim?
- Is the summary honest about mode, prompt, or plugin requirements?
- If it is plugin-backed, is it tagged with `plugin`?
- If it is IdeaVim-specific but not plugin-backed, is `ideavim` enough?
- Is the tip still useful if the user sees it without extra repo context?

Guidance:

- Use Vim docs as the content source, not as proof of IdeaVim support.
- Start with the local KSP-generated JSON files before checking external docs.
- Prefer real runtime behavior when available.
- Bang forms like `:e!` and `:q!` may still be valid even when only the base command is indexed.
- Some useful pattern and mapping behaviors are syntax inside supported
  commands, not standalone commands.
- Be conservative when support is ambiguous.

## Review Checklist

When reviewing existing tips, look for:

- bad or inconsistent categories
- duplicate teaching points
- misleading details
- tips that are too broad or overloaded
- setup advice that does not belong as a tip
- config-authoring tips that belong in docs more than in the rotating tip set
- summaries that lead with plugin or option names when the user outcome would be
  clearer
- tips that should be merged, split, retagged, or deleted

## Validation

Validate the JSON after edits:

```bash
jq . tips/vim_tips.json
```
