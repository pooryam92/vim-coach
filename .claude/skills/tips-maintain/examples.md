# The style guide — worked examples (before → after)

This file is how tip style is taught: **read it before authoring or rewording
anything.** Each entry shows the **whole tip object** ❌ before and ✅ after,
then *why*. When the user corrects or rejects a wording call, the lesson lands
here as a new entry (or sharpens the one that failed) — this file is the
skill's memory.

## Contents

- Name the family in prose, never dump symbols
- Don't restate the summary in detail line 1
- Explain the effect, not the anatomy — a tip must read cold
- One worked example beats a meta description + letter dump
- Name press-vs-type when an example crosses into Insert mode
- Split by intent, not key count
- Cut lines that don't earn their place
- In a slashed pair, vary one axis — keep the operator fixed
- Operator + motion tips — lead concrete, generalize the open axis
- Verify the claim against IdeaVim source, not Vim lore
- Search existing tips before adding — kill semantic duplicates
- Decode every key in a mnemonic, not just the ends
- Give the decoded words, don't echo the key
- Drop a mnemonic whose decode is obvious — don't fill the slot
- Join symbol pairs with `and` — a slash between glyphs is a pileup
- Theory earns one tip at most — and it must still be tryable

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

### Name press-vs-type when an example crosses into Insert mode

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
*Why:* from the reader's seat `c bar` is one mystery token (they ask "what is c
bar?") — it jams a command and the text you type together. When an example crosses
from a command into Insert-mode typing, name the action — press `c`, type `bar` —
so keystrokes read apart from input.

### Split by intent, not key count

❌ before (one tip cramming two intents):
```json
{
  "category": ["navigation"],
  "summary": "Open, close, or toggle a fold",
  "details": ["za toggles the fold under you", "zo forces open, zc forces closed"]
}
```
✅ after (one tip per intent):
```json
{
  "category": ["navigation"],
  "summary": "Toggle a fold with za",
  "details": ["Opens it if closed, closes if open", "One key covers most fold work"]
}
```
```json
{
  "category": ["navigation"],
  "summary": "Force a fold open or closed",
  "details": ["zo opens the fold, zc closes it", "Works when you know the end state"]
}
```
*Why:* toggle and force are *different intents*, so they are different tips; a
same-intent direction pair (`gj / gk`, `g0 / g$`) stays together as one tip.
Key count alone decides nothing. Display order is random, so each split tip
must stand alone and earn a *distinct* summary — the generator rejects
duplicates.

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

### Decode every key in a mnemonic, not just the ends

❌ before:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Wrap in an HTML tag ysiwt",
  "details": ["t or < prompts for the tag", "ysiw<em> wraps a word in <em>"],
  "mnemonic": "ys you surround, t tag",
  "config": { "name": "Install vim-surround", "lines": ["Plug 'tpope/vim-surround'"] }
}
```
✅ after:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Wrap in an HTML tag ysiwt",
  "details": ["t or < prompts for the tag", "ysiw<em> wraps a word in <em>"],
  "mnemonic": "ys you surround, iw inner word, t tag",
  "config": { "name": "Install vim-surround", "lines": ["Plug 'tpope/vim-surround'"] }
}
```
*Why:* the summary key is `ysiwt` but the mnemonic decoded only `ys` and `t`,
skipping `iw` — the confusing middle. A mnemonic that maps some keystrokes must map
them all; a half-decode leaves the reader guessing the part they most need. Keep it
≤40 chars (drop `=` separators before you drop a keystroke).

### Give the decoded words, don't echo the key — the summary already shows it

❌ before:
```json
{
  "category": ["ideavim", "editing"],
  "summary": "Open the refactor menu gm",
  "details": ["One list for extract, inline, move", "Rename lives here too"],
  "mnemonic": "gm = go menu (refactor)",
  "config": { "name": "Map refactor", "lines": ["nmap gm <Action>(Refactorings.QuickListPopupAction)"] }
}
```
✅ after:
```json
{
  "category": ["ideavim", "editing"],
  "summary": "Open the refactor menu gm",
  "details": ["One list for extract, inline, move", "Rename lives here too"],
  "mnemonic": "go menu (refactor)",
  "config": { "name": "Map refactor", "lines": ["nmap gm <Action>(Refactorings.QuickListPopupAction)"] }
}
```
*Why:* the summary already shows `gm` and the renderer prepends `Mnemonic:`, so
`gm =` restates the key twice before the hook lands. Author just the decoded words —
`go menu` already maps g→go, m→menu on its own. This doesn't contradict "decode
every key": the letters still all map, you just drop the redundant `key =` echo.

### Drop a mnemonic whose decode is obvious — don't fill the slot

❌ before:
```json
{
  "category": ["mappings"],
  "summary": "Use mode-specific maps in ~/.ideavimrc",
  "details": ["Use nmap, imap, or vmap for the target mode", "Normal, Insert, and Visual maps should stay separate"],
  "mnemonic": "n/i/v = normal/insert/visual"
}
```
✅ after:
```json
{
  "category": ["mappings"],
  "summary": "Use mode-specific maps in ~/.ideavimrc",
  "details": ["Use nmap, imap, or vmap for the target mode", "Normal, Insert, and Visual maps should stay separate"]
}
```
*Why:* `n`/`i`/`v` → normal/insert/visual is self-evident to anyone reading a maps
tip — the hook teaches nothing, and here it even restates detail line 2 word for
word. A mnemonic earns its line only when the keys wouldn't otherwise stick; when
the decode is obvious or already stated, delete it rather than fill the slot.

### Join symbol pairs with `and` — a slash between glyphs is a pileup

❌ before:
```json
{
  "category": ["navigation"],
  "summary": "Move by paragraphs { / }",
  "details": ["{ jumps back a paragraph, } forward", "Blank lines are the boundaries"]
}
```
✅ after:
```json
{
  "category": ["navigation"],
  "summary": "Move by paragraphs { and }",
  "details": ["{ jumps back a paragraph, } forward", "Blank lines are the boundaries"]
}
```
*Why:* ` / ` is the default joiner for letter-key pairs (`gj / gk`), but between
bracket/symbol glyphs it reads as three symbols in a row. `and` keeps the pair
readable. (A proposed "consistency" edit normalizing `and` back to ` / ` was
rejected for exactly this reason.)

### Theory earns one tip at most — and it must still be tryable

❌ before (a grammar tip with nothing to press):
```json
{
  "category": ["editing"],
  "summary": "Change/delete with operator + motion",
  "details": ["d{motion} deletes text", "c{motion} changes text"]
}
```
✅ after (tip deleted; its rule folds into one concrete host):
```json
{
  "category": ["editing"],
  "summary": "Change/delete a word cw / dw",
  "details": ["cw retypes the word, dw removes it", "Same d/c works with any motion"]
}
```
*Why:* `{motion}` placeholders give the reader nothing to try, and rewording
around them keeps failing. When a concept has no tryable form, delete the
standalone tip and fold its rule into exactly *one* concrete host — never echo
it across every sibling. Practical tips lead; theory gets at most one
consolidated line.
