# Vim Tips Authoring

Everything for **adding** and **improving** tip content in
[tips/categories/](../../tips/categories). The published file is generated from
these sources — see [tips-pipeline.md](tips-pipeline.md). Not-yet-shippable config
work (positional maps, leader convention, key-aware dedup) is tracked in
[../discover/config-tips-roadmap.md](../discover/config-tips-roadmap.md).

A good tip: one concrete command, correct for IdeaVim, easy to scan, easy to act
on now, findable by category.

## Sections

- [tip-format.md](tip-format.md) — the tip object: examples, field rules, and the
  three additive config-tip kinds.
- [display-and-wording.md](display-and-wording.md) — how a tip renders in the
  balloon and how to word it (length, keystroke separator, usable-keystroke and
  self-contained rules).
- [categories.md](categories.md) — the category set, how to pick one, and how to
  change the set.
- [checking-support.md](checking-support.md) — proving IdeaVim actually supports a
  command before keeping the tip.
- [improving-tips.md](improving-tips.md) — what to scan for when cleaning up
  existing tips.
- [tips-pipeline.md](tips-pipeline.md) — how the generator turns sources into the
  published file.

## Where tips live & the loop

- Edit `tips/categories/<primary-category>.json` — one object with a `tips`
  array, one item per tip. A tip lives in the file named by its **first**
  category (first category must match the file name).
- Never edit the generated `tips/vim_tips_min.json`.
.

**Add a tip:** pick category → write the object → confirm IdeaVim supports it →
regenerate + commit. See [tip-format.md](tip-format.md) and
[checking-support.md](checking-support.md).
**Improve tips:** scan for the issues in [improving-tips.md](improving-tips.md) →
fix wording/categories/support → regenerate + commit.
