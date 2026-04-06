# Vim Tips Reference

Use this document when adding, revising, or reviewing tips in [tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json).

## Working File

- Main tip file: `tips/vim_tips.json`
- Root shape: one JSON object with a `tips` array
- Each array item is one user-facing tip

## Tip Goals

A good tip should be:

- easy to scan
- easy to act on
- correct for IdeaVim
- easy to find through categories
- focused on one concrete command or workflow

Prefer tips that teach an action the user can try immediately over abstract taxonomy or reference-style wording.

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

Examples:

- Good: `Repeat last substitution with &`
- Worse: `Use the substitute repeat command`
- Good: `Replace a selection with p`
- Good: `Run a substitute only on matching lines with :g`

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
- Use `plugin` only for tips that depend on an IdeaVim plugin or extension being enabled.
- Plugin-backed tips should usually also keep their functional category, such as `motion`, `editing`, `visual`, or `windows`.
- Use `ideavim` for IdeaVim-specific behavior that is not necessarily tied to a plugin.
- Do not create ad hoc category names without updating the docs and the broader taxonomy deliberately.

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

Also check nearby `usr_*.txt` chapters when a section feels thin. The user manual often gives better beginner and workflow-oriented material than the reference pages alone.

### IdeaVim support sources

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
- Prefer real runtime behavior when available.
- Bang forms like `:e!` and `:q!` may still be valid even when only the base command is indexed.
- Some useful pattern and mapping behaviors are syntax inside supported commands, not standalone commands.
- Be conservative when support is ambiguous.

## Review Checklist

When reviewing existing tips, look for:

- bad or inconsistent categories
- duplicate teaching points
- misleading details
- tips that are too broad or overloaded
- setup advice that does not belong as a tip
- tips that should be merged, split, retagged, or deleted

## Validation

Validate the JSON after edits:

```bash
jq . tips/vim_tips.json
```
