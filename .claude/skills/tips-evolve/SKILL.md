---
name: tips-evolve
description: Use when the user asks whether the current session revealed durable improvements for the tips-maintain skill. Reviews the session, dedupes against the existing skill, proposes where and how each improvement should land, and waits for approval before editing.
disable-model-invocation: true
------------------------------

# Evolve the tips-maintain skill

The user invoked this to ask: **did this session teach anything durable that
`tips-maintain` should absorb?**

Review the conversation so far and answer honestly. Proposing no changes is a
valid and common outcome.

This command is the approval gate for `tips-maintain`'s standing charge to keep
itself up to date. Run it in two phases.

Do **not** edit files in Phase 1. Present findings, then stop. Move to Phase 2
only after the user explicitly approves one or more candidates.

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

Before proposing a candidate, ask whether the learning is better handled as:

* a refinement to existing skill guidance,
* a deletion or merge of existing guidance,
* detail moved to `reference.md`,
* a script/check instead of prose,
* memory because it is personal taste,
* or no change because the existing skill already covers it well.

“How it should land” must explain the integration strategy, not merely name a
destination. Decide whether the change should:

* fold into an existing section,
* replace wording that is now wrong or too weak,
* merge overlapping guidance,
* delete stale or redundant text,
* move detail from `SKILL.md` to `reference.md`,
* split or restructure a section whose shape no longer fits,
* become a script/check instead of prose,
* or be saved as memory if it is personal taste rather than skill guidance.

Name the target touched, such as `SKILL.md`, `reference.md`, a script, or memory.
Also name what the change would touch, replace, merge, move, or remove.

Before proposing a candidate, dedupe it against the existing skill. If the skill
already covers the point well, do not propose a duplicate. If the skill covers it
poorly, propose a refinement to the existing passage instead of adding a new one.

Do not write final edits in Phase 1. Sketch intent only.

If nothing qualifies, say that plainly and stop. Do not invent improvements to
look productive.

End Phase 1 by stopping for approval. Do not continue until the user chooses
which candidates, if any, to apply.

## Phase 2 — Rewrite only approved candidates

Apply only the candidates the user approved. Skip everything else.

For each approved candidate:

1. Show the exact **before → after** change before writing it.
2. Explain briefly why the edit improves the skill’s shape.
3. Make the edit.
4. Re-validate the skill as needed.

Integrate; do not accrete. The goal is a sharper skill, not a longer one.

Do not default to appending bullets. Work the learning into the structure that
already exists: rewrite the affected passage, merge it with overlapping guidance,
and cut anything the edit makes redundant.

When a section has outgrown its shape — too long, covering multiple concerns, or
burying important behavior — restructure it. Split it, move detail to
`reference.md`, collapse repeated guidance, or replace prose with a script/check.
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
  examples, and edge cases to `reference.md`.
* **Prefer scripts over prose for deterministic work.** Checks or transforms that
  a generator/linter can own should not be reasoned through manually every time.
* **List routine commands in `allowed-tools`.** Avoid unnecessary permission
  prompts for the normal loop.
* **Avoid machine-specific absolute paths.** Keep paths relative to the repo root.
