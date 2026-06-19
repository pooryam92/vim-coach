---
name: tips-evolve
description: Reflect on the current session and propose updates to the tips-maintain skill.
disable-model-invocation: true
---

# Evolve the tips-maintain skill

The user invoked this to ask: **did this session teach anything durable that the
`tips-maintain` skill should absorb?** Review the conversation so far and answer
honestly — proposing nothing is a valid, common outcome.

Apply the criteria in `tips-maintain` → *Evolving this skill* (the standing
mandate and its 4-step procedure). Do not restate them here; open that section if
it isn't already in context.

Run this in **two phases**. Do not edit any file in phase 1 — present, then stop
and wait. Only after the user agrees do you move to phase 2.

## Phase 1 — Report what you learned, then stop

1. **Scan this session** for durable, generalizable signals only:
   - a tip/workflow correction that reveals a *general* rule (not a one-off),
   - a newly stated preference or constraint,
   - an IdeaVim quirk or generator/script behavior worth recording,
   - a repeated manual step a script or check could own,
   - a *problem* in the skill itself surfaced this session (stale, wrong,
     duplicated, or missing guidance).
   One-off fixes and conversation-only details do **not** qualify — say so.

2. **List each candidate** with: a one-line description of the learning/problem,
   the one-line reason tied to the moment that prompted it, and — crucially —
   **how it should land**, not just where. A candidate is rarely "add a rule";
   more often it's *fold into an existing section*, *replace wording that's now
   wrong*, *compact two overlapping passages into one*, *delete something stale*,
   or *restructure / split a file* because the shape no longer fits. Name the
   target (`SKILL.md`, `reference.md`, a script, or memory if it's personal taste)
   and what it touches or removes. Dedupe against what the skill already says.
   **Do not write the edits yet** — sketch intent, not final text.

3. **If nothing qualifies, say that plainly** and stop — don't manufacture
   changes to look productive.

**Then stop and wait for the user to choose** which candidates to act on. This
gate is the whole point of the command.

## Phase 2 — Rewrite (only after the user agrees)

Apply only the approved candidates. For each, show the exact **before → after**
before writing it, then make the edit and re-validate the skill as needed. Skip
anything the user didn't approve. Personal taste flagged for memory goes to
memory, not the skill.

**Integrate, don't accrete.** The goal is a sharper skill, not a longer one.
Don't default to appending a bullet — work the learning *into* the structure that
already exists: rewrite the affected passage, merge it with what it overlaps, and
cut whatever it makes redundant in the same edit. When a section has outgrown its
shape — too long, covering two things, or buried — restructure it: split it, move
it to `reference.md`, or collapse it. A good evolution often leaves the file the
same length or shorter. If it only ever grows, you're accreting, not evolving.

**Hold these skill-authoring bars while editing** (from Anthropic's official skill
guidance — <https://code.claude.com/docs/en/skills>; re-read it if unsure):

- **`description` front-loads *when to trigger*.** Lead with the use case and
  trigger phrases, not internal file layout. It's the only signal for auto-loading
  and is truncated at ~1,536 chars in the listing.
- **The body is a recurring token cost** — once loaded it stays in context across
  turns. State what to do, not why; cut restated mandates and narration.
- **Progressive disclosure.** Keep `SKILL.md` lean (well under 500 lines); push
  detailed reference, examples, and edge cases to `reference.md`.
- **Prefer scripts over prose for deterministic work** — a check or transform the
  generator/linter can own shouldn't be reasoned out by hand each time.
- **List routine commands in `allowed-tools`** so the loop doesn't prompt for
  permission.
- **No machine-specific absolute paths** — keep paths relative to the repo root.
