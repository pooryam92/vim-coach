# IDE-bridge action mapping: prefer the conventional binding over a free slot

**Target file:** `.claude/skills/tips-maintain/config-kinds.md` (the "IDE-bridge action mapping" bullet under shippable config lines)
**Priority:** MINOR (refines existing guidance that now reads too weak)

## Learning / problem

When authoring an IDE-bridge `nmap … <Action>(…)` tip, the binding should match
the established Vim/Neovim convention for that action (the LSP `gd`/`gr`/`gi`
family) even when that key collides with a built-in or an already-taught
mapping — disclosing the shadow in the tip's details rather than substituting an
unconventional free key.

This sits in tension with the current `config-kinds.md` IDE-bridge bullet, which
says to "prefer idiomatic free slots (the `]e`/`[e`… unused `g`-prefix)" and to
"collision-check the keys against those already taught." That phrasing implies a
taught-key collision should be *avoided* — but the session showed convention-match
can outrank collision-avoidance, with the collision *disclosed* instead of dodged.

## Evidence

User: *"for the actions what is the normal bindings in vim/nvim lets use those."*
Result authored into `tips/categories/ideavim.json`: `gr` → `FindUsages` (shadows
ReplaceWithRegister's `gr`, which the set already teaches) and `gi` →
`GotoImplementation` (overrides built-in `gi`, "jump to last insert spot"). Each
tip's second detail line discloses the shadow ("Like nvim gr for references",
"Overrides built-in gi insert spot").

`config-kinds.md` currently:
> **IDE-bridge action mapping** — `nmap <keys> <Action>(ActionId)`. … prefer
> idiomatic free slots (the `]e`/`[e` bracket-pair family, an unused `g`-prefix).

## Why it qualifies

Generalizable to every future action-mapping tip, and it refines an existing
passage rather than adding a new concern. Decide in Phase 2 whether the
collision-check stays (to know *what* to disclose) and how to phrase
"convention beats free slot, disclose the shadow."
