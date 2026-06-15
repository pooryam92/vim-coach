# Vim Tips Authoring Guide

Use these documents when adding, revising, or reviewing **tip content** in
[tips/categories/](../../tips/categories). For how those sources become the
published file and how CI keeps them in sync, see
[tips-pipeline.md](../tips-pipeline.md).

This guide is split so you only need to load the part relevant to your task.

## Pick what you need

- **[workflow.md](workflow.md)** — where tip files live, the exact steps to add
  or edit a tip, and the support/review checklists. Start here for any change.
- **[writing-tips.md](writing-tips.md)** — everything about one tip: the JSON
  shape and field rules (`category`, `summary`, `details`, `config`), the wording
  rules with good/worse examples, and how tips render in the IntelliJ balloon.
- **[categories.md](categories.md)** — the current category list and the rules
  for choosing primary and secondary categories.
- **[checking-support.md](checking-support.md)** — how to confirm a command or
  plugin is actually supported, plus reference sources: local KSP data, Vim docs,
  and the IdeaVim submodule.

## Common tasks

- **Adding a tip:** [workflow.md](workflow.md) → [writing-tips.md](writing-tips.md)
  → [categories.md](categories.md).
- **Reviewing tips:** [workflow.md](workflow.md) → [categories.md](categories.md)
  → [writing-tips.md](writing-tips.md).
- **Verifying support:** [checking-support.md](checking-support.md) →
  [workflow.md](workflow.md).

## Tip Goals

A good tip should be:

- easy to scan
- easy to act on
- correct for IdeaVim
- easy to find through categories
- focused on one concrete command or workflow

Prefer tips that teach an action the user can try immediately over abstract
taxonomy or reference-style wording.
