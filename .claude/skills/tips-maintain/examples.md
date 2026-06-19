# Worked examples (before → after)

Open when you want the `SKILL.md` wording rules shown in action. Each entry shows
the **whole tip object** ❌ before and ✅ after, then *why*.

### Name the family in prose, never dump symbols

❌ before:
```json
{
  "category": ["navigation"],
  "summary": "Jump to matching bracket %",
  "details": ["Cursor on a bracket jumps to its pair", "Works for ) ] } \" ' too"]
}
```
✅ after:
```json
{
  "category": ["navigation"],
  "summary": "Jump to matching bracket %",
  "details": ["Cursor on a bracket jumps to its pair", "Any bracket or quote works too"]
}
```
*Why:* a row of bare symbols reads like keyboard mashing — name the family, keep
≤1 example.

### Don't restate the summary in detail line 1

❌ before:
```json
{
  "category": ["editing"],
  "summary": "Edit the next () pair cin)",
  "details": ["cin) seeks ahead to the next pair", "Repeat with . on the next pair"]
}
```
✅ after:
```json
{
  "category": ["editing"],
  "summary": "Edit the next () pair cin)",
  "details": ["Works even with the cursor outside it", "Repeat with . on the next pair"]
}
```
*Why:* the first detail is the most-read line — spend it on the *value*, not a
rephrase of the summary.

### Explain the effect, not the anatomy — a tip must read cold

❌ before:
```json
{
  "category": ["pattern", "cmdline"],
  "summary": "Keep only matching lines :v//d",
  "details": ["v = non-matching lines, d = delete"]
}
```
✅ after:
```json
{
  "category": ["pattern", "cmdline"],
  "summary": "Keep only matching lines :v//d",
  "details": [":v/foo/d deletes lines lacking foo", "What's left: only the foo lines"]
}
```
*Why:* token-naming still assumes you know `:g`/global commands. Anatomy *looks*
educational but recycles the jargon; a typeable command + plain-words outcome
teaches a reader who's never seen the concept.

### One worked example beats a meta description + letter dump

❌ before:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Coerce word case cr{x}",
  "details": ["cr + a letter picks the case", "c camel, s snake, m Mixed…"],
  "config": { "name": "Install vim-abolish", "lines": ["Plug 'tpope/vim-abolish'"] }
}
```
✅ after:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Coerce word case cr{x}",
  "details": ["crc turns foo_bar into fooBar", "crs gives snake, crm Mixed"],
  "config": { "name": "Install vim-abolish", "lines": ["Plug 'tpope/vim-abolish'"] }
}
```
*Why:* a single `input → output` example teaches the whole thing; the letter dump
makes the reader assemble it themselves.

### Split a dense tip into single-concept tips

❌ before (one tip cramming both directions):
```json
{
  "category": ["editing"],
  "summary": "Edit the next/previous () pair",
  "details": ["cin) edits the next pair", "cil) edits the previous pair"]
}
```
✅ after (two self-contained tips):
```json
{
  "category": ["editing"],
  "summary": "Edit the next () pair cin)",
  "details": ["Works even with the cursor outside it", "Searches forward to the pair"]
}
```
```json
{
  "category": ["editing"],
  "summary": "Edit the previous () pair cil)",
  "details": ["Reaches back to the pair behind you", "Searches backward to the pair"]
}
```
*Why:* display order is random, so a tip can't lean on another. Only split when
each earns a *distinct* summary — the generator rejects duplicates.

### Cut lines that don't earn their place

❌ before:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Make a word camelCase crc",
  "details": ["crc turns foo_bar into fooBar", "cr + a letter sets the style"],
  "config": { "name": "Install vim-abolish", "lines": ["Plug 'tpope/vim-abolish'"] }
}
```
✅ after:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Make a word camelCase crc",
  "details": ["crc turns foo_bar into fooBar", "Cursor can sit anywhere in the word"],
  "config": { "name": "Install vim-abolish", "lines": ["Plug 'tpope/vim-abolish'"] }
}
```
*Why:* the trailing `cr + a letter sets the style` repeated on every case tip adds
nothing — replace filler with a fact the reader doesn't already have.

### In a slashed pair, vary one axis — keep the operator fixed

❌ before:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Act on an argument cia / daa",
  "details": ["cia changes inside an argument", "daa deletes it with its comma"],
  "config": { "name": "Install argtextobj", "lines": ["Plug 'vim-scripts/argtextobj.vim'"] }
}
```
✅ after:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Act on an argument cia / caa",
  "details": ["i is inside, a takes the comma too", "Pairs with d, y and other operators"],
  "config": { "name": "Install argtextobj", "lines": ["Plug 'vim-scripts/argtextobj.vim'"] }
}
```
*Why:* `cia / daa` mixes two operators *and* two text objects. Hold the operator
steady (`c`) so the slash shows just the text-object difference (`i` vs `a`); name
the other operators in a detail.

### Operator + motion tips — lead concrete, generalize the open axis

❌ before:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Swap two regions cx",
  "details": ["cx marks a region, cx again swaps", "Takes any motion"],
  "config": { "name": "Install vim-exchange", "lines": ["Plug 'tommcdo/vim-exchange'"] }
}
```
✅ after:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Swap two regions cxiw",
  "details": ["cxiw marks a word, cxiw again swaps", "Takes any motion, like cxx"],
  "config": { "name": "Install vim-exchange", "lines": ["Plug 'tommcdo/vim-exchange'"] }
}
```
*Why:* the summary leads with one concrete, runnable combo — never a bare operator
or a `{motion}` placeholder. The last detail generalizes the open axis in prose
with ≤1 example. Antonym pairs (`Indent/outdent a block >ip / <ip`) and
fixed-motion change/delete pairs (`Change/delete a word cw / dw`) may keep two
operators in the title.

### Verify the claim against IdeaVim source, not Vim lore

❌ before:
```json
{
  "category": ["navigation"],
  "summary": "Jump to definition gd / gD",
  "details": ["gd jumps to definition", "gD jumps to declaration"]
}
```
✅ after:
```json
{
  "category": ["navigation"],
  "summary": "Jump to definition gd",
  "details": ["Lands on where the symbol is defined", "gD does the same in IdeaVim"]
}
```
*Why:* upstream-Vim splits the two, but in `external/ideavim` both keys bind to the
same `GotoDeclarationAction` — confirm in the submodule before claiming a
distinction.

### Search existing tips before adding — kill semantic duplicates

❌ before (a second tip teaching what `nzz` already covers):
```json
{
  "category": ["pattern"],
  "summary": "Repeat search then center n zz",
  "details": ["n jumps to the next match", "zz centers the line"]
}
```
✅ after (drop it — this tip already exists):
```json
{
  "category": ["pattern"],
  "summary": "Recenter search results nzz",
  "details": ["n finds the next match", "zz pulls it to screen center"]
}
```
*Why:* the generator only rejects *identical* summaries, so the same idea under
different wording slips through. Grep both the keys *and* the behavior first; drop
or merge instead of adding.
