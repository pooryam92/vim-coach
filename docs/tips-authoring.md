# Vim Tips Authoring Guide

Use this document when adding, revising, or reviewing **tip content** in
[tips/categories/](../tips/categories). For how those sources become the
published file and how CI keeps them in sync, see
[tips_pipeline.md](tips-pipeline.md).

## Where Tips Live

- Authoring files: `tips/categories/<primary-category>.json` — **edit these**
- Each authoring file is one JSON object with a `tips` array
- Each array item is one user-facing tip
- A tip lives in the file named by its **first** category. A tip whose first
  category is `motion` goes in `tips/categories/motion.json`.
- The published `tips/vim_tips_min.json` is **generated** from these files and is
  never edited by hand — see [tips_pipeline.md](tips-pipeline.md).

## Adding or editing a tip

1. Pick the tip's primary category and open `tips/categories/<category>.json`.
2. Add or edit a tip object in that file's `tips` array (see **Tip Format**).
   The first entry in `category` must match the file name.
3. Regenerate and validate the published file:
   ```bash
   node scripts/generate-tips.mjs
   ```
4. Commit the category file you changed. Committing the regenerated
   `tips/vim_tips_min.json` too keeps the diff self-contained, but it is not
   required — CI regenerates and commits it from the sources either way.

If the script reports an error, fix the named tip and rerun it. See
[tips_pipeline.md](tips-pipeline.md) for the exact checks it runs.

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
  - the first entry is the primary category and must match the file name
  - use one primary category by default
  - add a second or third category only when it genuinely helps discoverability
- `summary`
  - one short command-first line
  - should tell the user what to do or what they gain
  - aim for 35 characters or fewer so it fits on one line in the notification
- `details`
  - short factual lines
  - explain what the command does, context, caveats, or a quick example
  - each line should also aim for 35 characters or fewer
- `config` (optional)
  - array of `.ideavimrc` line(s) the tip refers to
  - when present, the tip notification shows an **Add to .ideavimrc** button that
    appends these line(s) to the user's `.ideavimrc` (creating it if needed)
  - hold the **enable** line(s) only, not usage mappings (e.g. `ysiw)` is how you
    *use* surround, not config)
  - for IdeaVim plugins, author the `Plug '<github-alias>'` form, not the legacy
    `set <plugin>` form, which IdeaVim flags via `UsePlugSyntaxInspection`. Aliases
    live in `external/ideavim/doc/IdeaVim Plugins.md`
  - lines are written verbatim (order and duplicates preserved); only blank lines
    are dropped
  - multi-line config is fine, e.g.
    `["Plug 'bkad/CamelCaseMotion'", "let g:camelcasemotion_key = '<leader>'"]`

Invalid content (the generator rejects these — see
[tips_pipeline.md](tips-pipeline.md)):

- blank summaries
- a tip with no detail lines
- a `summary` that repeats one already used by another tip, in any file
- a first `category` that does not match the file name

Avoid as a matter of style:

- blank detail lines (they are stripped silently, so they just add noise)
- unsupported claims about IdeaVim behavior

## Notification Display

Tips appear inside an IntelliJ balloon notification. The app name "Vim Coach" is
the static balloon title. Both `summary` and `details` are rendered in the body
as HTML — `summary` in bold at the top, `details` lines below separated by line
breaks.

The IntelliJ balloon body area is styled at 240 px wide
(`BalloonLayoutConfiguration.MaxWidthStyle`). At the default IDE font, this fits
roughly **30–35 characters per line** before text wraps. The summary renders in
bold, which is slightly wider. The default balloon view shows about 2 wrapped
lines of body text; anything beyond that requires the user to scroll.

Practical constraints:

- Keep summaries to **≤ 35 characters** so the bold summary line stays on one
  line. Wrapping onto two lines pushes detail lines out of the default view.
- Keep each detail line short for the same reason. Two or three short lines are
  better than one long wrapped line.
- Abbreviated forms like `char`, `msg`, and `prev` are acceptable in summaries
  when spelling them out would push the summary over one line.
- Omit filler words like `with` before a command name when the command already
  makes the relationship clear. `Replace one character r{char}` reads fine
  without `with`.

## Tip Preference Identity

User preferences such as hiding a tip are tied to a deterministic hash of each
tip's trimmed `summary` value. This avoids adding an author-managed ID field,
but it means changing a tip title creates a new identity for that tip and may
reset any existing user preference for it.

## Writing Style

Use these rules when writing or revising tip text:

- Keep summaries command-first and concrete.
- Keep summaries to 35 characters or fewer. See the Notification Display section
  for why this matters.
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

Current user-facing categories (this is also the order tips appear in the
published file):

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
  taxonomy deliberately. A new category also means a new
  `tips/categories/<name>.json` file (and an entry in the generator's category
  order — see [tips_pipeline.md](tips-pipeline.md)).

## Checking IdeaVim Support

Use Vim docs for meaning and teaching value. Use IdeaVim docs and source to
confirm a command or behavior is actually supported.

### Local support data

Use the generated KSP JSON files in the IdeaVim submodule as the first local
support check for whether a command, Ex command, Vimscript function, or IdeaVim
extension exists.

To fetch the submodule after cloning this repo:

```bash
git submodule update --init external/ideavim
```

To keep the local submodule checkout focused on generated KSP files:

```bash
git -C external/ideavim sparse-checkout init --cone
git -C external/ideavim sparse-checkout set \
  src/main/resources/ksp-generated \
  vim-engine/src/main/resources/ksp-generated
```

To refresh it to the latest tracked IdeaVim `master` commit:

```bash
git submodule update --remote external/ideavim
```

- Engine command data:
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/engine_commands.json`
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/engine_ex_commands.json`
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/engine_vimscript_functions.json`
- Frontend command data:
  - `external/ideavim/src/main/resources/ksp-generated/frontend_commands.json`
  - `external/ideavim/src/main/resources/ksp-generated/frontend_ex_commands.json`
  - `external/ideavim/src/main/resources/ksp-generated/frontend_vimscript_functions.json`
- Extension data:
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/ideavim_extensions.json`
  - `external/ideavim/src/main/resources/ksp-generated/ideavim_extensions.json`

Use them like this:

- Check `engine_*` files for core Vim engine support.
- Check `frontend_*` files for frontend-only commands such as `:buffer`,
  `:buffers`, `:files`, `:ls`, `:help`, `:read`, and `:actionlist`.
- Check the extension JSON files before claiming a plugin-backed IdeaVim
  extension exists.
- If the KSP data confirms existence but not user-facing behavior, use Vim docs
  for semantics and IdeaVim docs/source for plugin setup or runtime details.

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

The `external/ideavim` submodule is the source of truth — read it directly
instead of browsing GitHub. The recommended sparse checkout (see **Local
support data**) only includes the `ksp-generated` JSON, which is enough for most
support checks. To read the full source, widen the sparse-checkout set, for
example:

```bash
git -C external/ideavim sparse-checkout add \
  annotation-processors vimscript-info src vim-engine
```

- Generated support data: `external/ideavim/.../ksp-generated/`
- Annotation processors: `external/ideavim/annotation-processors`
- Vimscript info: `external/ideavim/vimscript-info`
- Frontend source: `external/ideavim/src`
- Engine source: `external/ideavim/vim-engine`

For setup and usage docs not in the source tree, see the
[IdeaVim wiki](https://github.com/JetBrains/ideavim/wiki).

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
