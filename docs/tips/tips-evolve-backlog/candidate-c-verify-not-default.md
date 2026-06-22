# Candidate C — verify a `set` option isn't already the default

**Priority:** MINOR. Small guardrail.

**Target file:** `.claude/skills/tips-maintain/checking-support.md`.

---

## Learning

An option-backed tip whose `config` sets a value that's already the default ships
a no-op.

## Evidence

- `set ideavimsupport=dialog` was a proposed tip this session, but `dialog` is the
  default:
  `external/ideavim/src/main/java/com/maddyhome/idea/vim/group/IjOptions.kt:121`.
  Dropped.

## Why it qualifies

Generalizable authoring check for any option-backed tip, not specific to this
option.
