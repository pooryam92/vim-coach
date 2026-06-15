# Workflow & Checklists

Where tip content lives, the steps to add or edit a tip, and the checklists to
run before keeping one. For the JSON shape and wording, see
[writing-tips.md](writing-tips.md). For the build/CI details, see
[tips-pipeline.md](../tips-pipeline.md).

## Where Tips Live

- Authoring files: `tips/categories/<primary-category>.json` — **edit these**
- Each authoring file is one JSON object with a `tips` array
- Each array item is one user-facing tip
- A tip lives in the file named by its **first** category. A tip whose first
  category is `motion` goes in `tips/categories/motion.json`.
- The published `tips/vim_tips_min.json` is **generated** from these files and is
  never edited by hand — see [tips-pipeline.md](../tips-pipeline.md).

## Adding or Editing a Tip

1. Pick the tip's primary category and open `tips/categories/<category>.json`
   (see [categories.md](categories.md)).
2. Add or edit a tip object in that file's `tips` array (see
   [writing-tips.md](writing-tips.md)). The first entry in `category` must match
   the file name.
3. Verify the command or behavior is actually supported by IdeaVim (see
   [checking-support.md](checking-support.md)) and run the **Support Checklist**
   below.
4. Regenerate and validate the published file:
   ```bash
   node scripts/generate-tips.mjs
   ```
5. Commit the category file you changed. Committing the regenerated
   `tips/vim_tips_min.json` too keeps the diff self-contained, but it is not
   required — CI regenerates and commits it from the sources either way.

If the script reports an error, fix the named tip and rerun it. See
[tips-pipeline.md](../tips-pipeline.md) for the exact checks it runs.

## Support Checklist

Before keeping a tip, check:

- Is the underlying command or behavior clearly supported by IdeaVim?
- Is the summary honest about mode, prompt, or plugin requirements?
- If it is plugin-backed, is it tagged with `plugin`?
- If it is IdeaVim-specific but not plugin-backed, is `ideavim` enough?
- Is the tip still useful if the user sees it without extra repo context?

Guidance (see [checking-support.md](checking-support.md) for how to verify):

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
