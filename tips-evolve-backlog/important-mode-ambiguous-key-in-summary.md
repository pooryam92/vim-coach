# Mode-ambiguous keys must name the mode in the summary, not just a detail

**Target file:** `.claude/skills/tips-maintain/SKILL.md` (Wording rules, the "Name modes explicitly" bullet)
**Priority:** IMPORTANT (two direct user corrections in one session)

## Learning / problem

When a key does different things in different modes, naming the mode only in a
detail line isn't enough — the *summary* must say which mode, or the title doesn't
tell the reader which behavior the tip teaches. Examples of overloaded keys:
`Ctrl-w` (delete-word in Insert / window-prefix in Normal), `O` (open-line in
Normal / swap-corner in Visual block), `I`/`A` (insert vs block-insert), `u`, `s`.

## Evidence

Two independent corrections in one session, same phrasing both times:
- `Ctrl-w` Insert tip → user: *"not clear it was in what mode"* → summary moved to
  `Delete a word in Insert mode Ctrl-w` (`tips/categories/insert.json`).
- `O` block tip → user: *"not clear it was in what mode"* → summary moved to
  `Swap corner in Visual block O` (`tips/categories/visual.json`).

Existing guidance, `SKILL.md` Wording rules:
> **Name modes explicitly when context helps:** `Normal mode`, `Insert mode`,
> `Visual mode`.

This is too vague — "when context helps" doesn't name the trigger (key overloaded
across modes) and doesn't say the *summary* specifically must carry it.

## Why it qualifies

General, repeatable rule with a concrete trigger (a key whose meaning depends on
mode) that recurs across the whole keyboard. The same fix applied both times.
Two hits in one session is a pattern, not a one-off.

## How it should land

*Refine the existing bullet — do not add a new one.*

❌ before:
> - **Name modes explicitly when context helps:** `Normal mode`, `Insert mode`,
>   `Visual mode`.

✅ after:
> - **Name the mode in the summary when the key is mode-ambiguous.** A key that
>   means different things per mode (`O`, `I`, `A`, `Ctrl-w`) must say which mode
>   in the *summary*, not just a detail — else the title doesn't tell the reader
>   which behavior it teaches (`Swap corner in Visual block O`, not
>   `Swap block corner O`). Name any mode explicitly when context helps.

*Why:* keeps the original "name modes explicitly" point but front-loads the
recurring trigger and pins it to the summary, which is where both corrections
landed. Before applying, re-read the surrounding rules — if a nearby bullet
already pins mode to the summary, merge instead of replacing.
