# Distinguish keys pressed from text typed in worked examples

**Target file:** `.claude/skills/tips-maintain/examples.md`
**Priority:** MINOR

## Learning / problem

When a worked example runs a Normal-mode command straight into the literal text
you type (`c bar`), the reader can't tell what's a keystroke vs. what's typed —
it reads as one opaque token.

## Evidence

In the multiple-cursors tip session, the detail line `"Then c bar replaces every
foo"` shipped and the user immediately asked **"what is c bar?"**. The fix was
`"Then c, type bar, Esc — all to bar"` — spelling out press-vs-type.

## Why it qualifies

Generalizable to any tip whose example crosses from a Normal-mode command into
Insert-mode typing (`c`, `i`, `a`, `r{char}`, `:s` replacement text). A recurring
class, not specific to multiple-cursors. The existing SKILL.md rule "Every
keystroke shown must do something when typed" is about *bare text objects* — a
different failure — so this is not already covered.

## How it should land

Add a new before→after entry to `examples.md`, beside the existing "One worked
example beats a meta description + letter dump" entry. Use the actual case:

❌ before:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Edit every copy of a word Alt-n",
  "details": ["Alt-n selects foo, repeat for more", "Then c bar replaces every foo"]
}
```
✅ after:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Edit every copy of a word Alt-n",
  "details": ["Alt-n selects foo, repeat for more", "Then c, type bar, Esc — all to bar"]
}
```
*Why:* `c bar` jams a command and the text you type into one mystery token. When an
example crosses from a command into Insert-mode typing, name the action — "press
c, type bar" — so the reader can tell keystrokes from input.

Touches only `examples.md`; adds nothing to the recurring `SKILL.md` cost.
