---
name: tips-evolve
description: Use when the user asks whether the current session revealed durable improvements for the tips-maintain skill, or asks to apply/clear the tips-evolve backlog. Reviews the session, dedupes against the existing skill, proposes where and how each improvement should land (or parks findings in docs/tips/tips-evolve-backlog/ to pick up later), and waits for approval before editing.
---

# Evolve the tips-maintain skill

The user invoked this to ask: **did this session teach anything durable that
`tips-maintain` should absorb?**

Review the conversation so far and answer honestly. Proposing no changes is a
valid and common outcome.

This command is the approval gate for `tips-maintain`'s standing charge to keep
itself up to date. Run it in two phases.

Do **not** edit files in Phase 1. Present findings, then stop. Move to Phase 2
only after the user explicitly approves one or more candidates.

When a session surfaces more than fits one sitting, findings can be parked in a
backlog (see [Backlog](#backlog) below) instead of being forced through now.
Phase 2 can apply them inline or pick them up cold from the backlog later.

## Phase 1 — Review, propose, then stop

Scan the session for durable, generalizable signals only:

* a tip or workflow correction that reveals a general rule,
* a newly stated preference or constraint,
* an IdeaVim quirk or generator/script behavior worth recording,
* a repeated manual step a script or check should own,
* a problem in the skill itself: stale, wrong, duplicated, missing, too long,
  badly placed, or structurally awkward guidance.

One-off fixes and conversation-only details do **not** qualify. Say so plainly.

For each qualifying candidate, report:

1. **Learning / problem** — one line describing what changed or what was exposed.
2. **Evidence** — the specific session moment or existing-skill passage that supports it.
3. **Why it qualifies** — why this is durable and generalizable, not a one-off.
4. **How it should land** — the important part.

For the fourth — the integration strategy, not just a destination — decide which
fits:

* **no change** — the skill already covers it well (the most common answer);
* **fold** into an existing section, or **replace** wording now wrong or too weak;
* **merge** overlapping guidance, or **delete** stale or redundant text;
* **move** detail from `SKILL.md` to a focused file (`examples.md`,
  `config-kinds.md`, `checking-support.md`, `categories.md`);
* **split or restructure** a section whose shape no longer fits;
* turn prose into a **script/check** when the work is deterministic;
* save as **memory** when it's personal taste, not skill guidance.

Name the exact target and what the change would touch, replace, merge, move, or
remove.

Always dedupe against the existing skill first: never propose what it already
covers, and refine a weak passage rather than adding a parallel one.

Sketch intent only — no final edits yet. If nothing qualifies, say so plainly;
don't invent improvements to look productive. Either way, stop after presenting —
Phase 2 begins only when the user picks candidates.

If the user wants to defer — or Phase 1 yields more than fits one sitting — write
findings to the [backlog](#backlog) instead of holding them in the conversation.

## Backlog

`docs/tips/tips-evolve-backlog/` is a scratch queue for findings to apply
later. Each file is one finding, self-contained enough to pick up cold in a fresh
conversation — its originating chat will be gone.

Layout:

* `README.md` — index. One line per finding: priority, slug link, target
  file(s). Point to `did-not-qualify.md`.
* `<priority>-<slug>.md` — one finding. Record only the **problem/observation**,
  not a solution. Open with **target file(s)** and **priority**, then:
  **Learning / problem** (the observation — what changed or what was exposed),
  **Evidence** (`file:line` or the session moment), **Why it qualifies**. Do
  **not** write a fix, integration strategy, or wording sketch — the solution is
  decided fresh in Phase 2, when the skill's current shape is in view. A solution
  parked now is likely stale by the time it is picked up.
* `did-not-qualify.md` — findings deliberately rejected, with the reason, so a
  later review does not re-litigate them.

A backlog file must stand alone: name targets relative to the repo root and
include enough evidence to act without the original conversation. Write a finding
here when Phase 1 yields several candidates, when the user asks to defer, or when
a finding is better worked in a focused session.

## Phase 2 — Rewrite only approved candidates

Apply only the candidates the user approved. Skip everything else. Candidates may
come from this session's Phase 1 or from the [backlog](#backlog) — when the user
points at a backlog file (or asks to clear the backlog), treat it as the source
of truth and read it cold; do not assume the originating session's context. A
backlog finding records only the problem/observation, so work out its integration
strategy now against the skill's current shape — propose how it should land, get
approval, then apply it like any other candidate.

For each approved candidate:

1. Show the exact **before → after** change before writing it.
2. Explain briefly why the edit improves the skill’s shape.
3. Make the edit.
4. Re-validate the skill as needed.
5. If the candidate came from the backlog, delete its file and drop its line from
   `README.md` once applied — git history preserves it. A rejected backlog item
   moves to `did-not-qualify.md` rather than lingering as an open finding.

Integrate; do not accrete. The goal is a sharper skill, not a longer one.

Do not default to appending bullets. Work the learning into the structure that
already exists: rewrite the affected passage, merge it with overlapping guidance,
and cut anything the edit makes redundant.

When a section has outgrown its shape — too long, covering multiple concerns, or
burying important behavior — restructure it. Split it, move detail to
a focused file, collapse repeated guidance, or replace prose with a script/check.
A good evolution may leave the file the same length or shorter.

Personal taste belongs in memory, not the skill.

After editing, perform a shape check:

* Did this remove or merge any guidance it made redundant?
* Did it preserve or improve the skill’s trigger clarity?
* Did it keep `SKILL.md` lean, moving detail elsewhere when appropriate?
* Did it avoid converting one session’s preference into a general rule?
* Did it leave deterministic checks to scripts where possible?

If the edit only made the skill longer without making it clearer or more
actionable, revise the edit before finalizing.

## Skill-authoring bars

Hold these bars while editing:

* **Descriptions front-load when to trigger.** Lead with the use case and trigger
  phrases, not internal file layout. The description is the auto-loading signal
  and may be truncated in listings.
* **The body is recurring token cost.** Once loaded, it stays in context. State
  what to do; cut repeated mandates and narration.
* **Use progressive disclosure.** Keep `SKILL.md` lean. Move detailed reference,
  examples, and edge cases to focused files opened on demand — the model
  `tips-maintain` already uses (`examples.md`, `config-kinds.md`,
  `checking-support.md`, `categories.md`); add a new focused file rather than
  growing `SKILL.md`.
* **Prefer scripts over prose for deterministic work.** Checks or transforms that
  a generator/linter can own should not be reasoned through manually every time.
* **List routine commands in `allowed-tools`.** Avoid unnecessary permission
  prompts for the normal loop.
* **Avoid machine-specific absolute paths.** Keep paths relative to the repo root.
