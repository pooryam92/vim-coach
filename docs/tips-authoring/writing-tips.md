# Writing a Tip: Format, Style & Display

Everything about a single tip: its JSON shape, how to word it, and how it
renders in the IDE. The three are tied together — the display constraints are
*why* the wording rules exist — so they live in one file.

For where tips live and the edit steps, see [workflow.md](workflow.md). For
category choices, see [categories.md](categories.md).

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
    (see [categories.md](categories.md))
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
    appends these line(s) to the user's `.ideavimrc` (only when IdeaVim is
    installed and the file already exists — it is never created for the user)
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
[tips-pipeline.md](../tips-pipeline.md)):

- blank summaries
- a tip with no detail lines
- a `summary` that repeats one already used by another tip, in any file
- a first `category` that does not match the file name

Avoid as a matter of style:

- blank detail lines (they are stripped silently, so they just add noise)
- unsupported claims about IdeaVim behavior

## How Tips Display

Tips appear inside an IntelliJ balloon notification. The app name "Vim Coach" is
the static balloon title. Both `summary` and `details` are rendered in the body
as HTML — `summary` in bold at the top, `details` lines below separated by line
breaks.

The IntelliJ balloon body area is styled at 240 px wide
(`BalloonLayoutConfiguration.MaxWidthStyle`). At the default IDE font, this fits
roughly **30–35 characters per line** before text wraps. The summary renders in
bold, which is slightly wider. The default balloon view shows about 2 wrapped
lines of body text; anything beyond that requires the user to scroll.

Practical constraints (these drive the wording rules below):

- Keep summaries to **≤ 35 characters** so the bold summary line stays on one
  line. Wrapping onto two lines pushes detail lines out of the default view.
- Keep each detail line short for the same reason. Two or three short lines are
  better than one long wrapped line.
- Abbreviated forms like `char`, `msg`, and `prev` are acceptable in summaries
  when spelling them out would push the summary over one line.
- Omit filler words like `with` before a command name when the command already
  makes the relationship clear. `Replace one character r{char}` reads fine
  without `with`.

### Tip preference identity

User preferences such as hiding a tip are tied to a deterministic hash of each
tip's trimmed `summary` value. This avoids adding an author-managed ID field,
but it means changing a tip title creates a new identity for that tip and may
reset any existing user preference for it.

## Writing Style

Use these rules when writing or revising tip text:

- Keep summaries command-first and concrete.
- Keep summaries to 35 characters or fewer (see **How Tips Display** for why).
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

### Lessons from the phrasing pass

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
