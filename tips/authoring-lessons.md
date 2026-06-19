# Tip Authoring — Lessons (example first)

Patterns we applied while revising the plugin tips. Each: ❌ before → ✅ after,
then the rule. Full wording reference: `../docs/tips/display-and-wording.md`.

## 1. Name the category in prose, never dump symbols
❌ `Works for ) ] } " ' too`
✅ `Any bracket or quote works too`

❌ `Works for . ; : | and more too`
✅ `Any separator works — semicolon, pipe, more`

A row of bare symbols reads like keyboard mashing. Name the family in words;
keep at most one concrete example.

## 2. Don't restate the summary in detail line 1
❌ summary `Edit the next () pair cin)` + detail `cin) seeks ahead to the next pair`
✅ detail `Works even with the cursor outside it`

The first detail is the most-read line. Spend it on the *value* (you don't have
to navigate there), not a rephrase of the summary.

## 3. One worked example beats a meta description + letter dump
❌
```
cr + a letter picks the case
c camel, s snake, m Mixed, u UPPER
e.g. foo_bar →crc fooBar
```
✅ `crc turns foo_bar into fooBar`

`→crc` mashes a key into an arrow; the letter list is a dump. A single
`input → output` example teaches the whole thing.

## 4. Split a dense tip into standalone single-concept tips
Random display order means a tip can't lean on another. One idea per tip.

❌ one tip cramming next + previous + generalization
✅ `Edit the next () pair cin)` and `Edit the previous () pair cil)` as two tips

❌ one `crc` tip listing every case style
✅ four tips: `crc` camelCase, `crs` snake_case, `crm` MixedCase, `cru` UPPER_CASE

Only split when each piece earns a **distinct summary** (the generator rejects
duplicate summaries).

## 5. Cut lines that don't earn their place
❌ trailing `cr + a letter sets the style` on every case tip
✅ (removed)

If the summary already shows the full key and a detail shows the result, a vague
"others exist" line adds nothing — drop it.

## 6. In a paired title, vary one axis — keep the operator fixed
❌ `Act on an argument cia / daa`
✅ `Act on an argument cia / caa`

A `x / y` summary should contrast a single dimension. `cia / daa` mixes two
operators (c, d) *and* two text objects (i, a), so the reader can't tell which
change matters. Hold the operator steady and let the slash show the text-object
difference — here `i` (just the argument) vs `a` (also its comma). Name the
other operators in a detail line: `Pairs with d, y and other operators`.

## 7. Operator + motion tips: lead concrete, generalize the open axis
These tips have two axes — an **operator** (c/d/y/>/=, or a plugin one like ys,
cx, cr) and a **motion/text-object** (iw, ap, `)`, `$`). One axis is the feature
being taught; the other is the open slot. Unify them this way:

**Summary — lead with one concrete, runnable combo.** Never a bare operator
(`cx` does nothing) or a `{motion}` placeholder.
❌ `Swap two regions cx`   ❌ `Swap regions cx{motion}`
✅ `Swap two regions cxiw`

**A slashed pair in the summary may only contrast the text-object scope (i vs a),
never the operator** (lesson 6).
✅ `Delete word text diw / daw`   (i vs a — same operator)
❌ `Act on a block by indent dii / cii`   (d vs c — operator dumped into title)
✅ `Act on a block by indent dii` + a coverage line

Exceptions that may keep two operators in the title:
- a deliberate **antonym pair**, where the two directions *are* the axis —
  e.g. `Indent/outdent a block >ip / <ip`;
- a **delete/change pair** over a fixed motion, where the d-vs-c symmetry is
  the teaching — e.g. `Delete/change to line start d0 / c0`, `Change/delete a word cw / dw`.

Also: never lead a summary with a bare **text object** (`is`, `ia`) — it does
nothing alone. Lead with an operator on it: `cis`, `cia`.

**Last detail — generalize the open axis in prose, with ≤1 example.** Pick one of
two fixed phrasings by which axis is open:
- open axis = motion → `Takes any motion, like cxx`
- open axis = operator → `Pairs with any operator — c, d, y`

This replaces the scattered variants (`Works with any operator, even > and =`,
`Pairs with c, y, > and other operators`, `Use other motions too, such as >}`)
with exactly two.

## 8. Verify the claim against IdeaVim source, not Vim lore
❌ `gd jumps to definition, gD jumps to declaration`
✅ confirm in `external/ideavim` first — both keys bind to the *same*
   `GotoDeclarationAction`, so they're identical here

These tips target **IdeaVim**, where many keys are remapped or collapsed onto IDE
actions. Check the source (an action's `@CommandOrMotion` keys, an option in
`IjOptions.kt`) before asserting behavior; don't carry over upstream-Vim
semantics that IdeaVim doesn't replicate.

## 9. Search existing tips before adding — kill semantic duplicates
❌ adding `Repeat search and place line n then zz` when
   `Recenter search results with nzz` already teaches it
✅ grep the concept first; drop or merge if it's covered

The generator only rejects **identical summaries** — the same idea under a
different summary slips through. Search both the keys *and* the behavior across
`categories/` before writing a new tip.

## Workflow
- Revise in small batches (≤2), show whole-tip before → after + short reason,
  get a go-ahead, then edit.
- After editing: `node scripts/generate-tips.mjs` regenerates `vim_tips_min.json`.
