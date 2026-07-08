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
- Reach for the community mnemonic, not a homemade key-echo
- One word per key — cut filler words with no keystroke behind them
- Drop a mnemonic whose decode is obvious — don't fill the slot
- Join symbol pairs with `and` — a slash between glyphs is a pileup
- Theory earns one tip at most — and it must still be tryable
- A bare register id is a hidden cross-tip dependency
- Anchor a family mnemonic in the real hook — and name the key a shape depicts
- Merge a set-and-use pair when neither half stands alone
- Cut a command you can't try cold — doubly so when the IDE already does it
- Prefer the `mode` label over a "Works in X mode" detail line

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

### Reach for the community mnemonic, not a homemade key-echo

❌ before:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Rename an HTML tag cst",
  "details": ["cst<div> makes <span> into <div>", "Edits the open and close tag at once"],
  "mnemonic": "cs = change surround, t = tag",
  "config": { "name": "Install vim-surround", "lines": ["Plug 'tpope/vim-surround'"] }
}
```
✅ after:
```json
{
  "category": ["plugins", "editing"],
  "summary": "Rename an HTML tag cst",
  "details": ["cst<div> makes <span> into <div>", "Edits the open and close tag at once"],
  "mnemonic": "change surrounding tag",
  "config": { "name": "Install vim-surround", "lines": ["Plug 'tpope/vim-surround'"] }
}
```
*Why:* `cst` already has a reading the whole vim-surround community shares —
*"change surrounding tag"* (tpope's own docs, every cheatsheet). Reach for that
before minting your own gloss. It also happens to satisfy the two mnemonic
rules at once: it decodes all three keys (c→change, s→surrounding, t→tag) and
echoes none of them (no `cs =`/`t =`). When a plugin key has an idiomatic name,
use it — a homemade key-echo is both less memorable and less standard.

### One word per key — cut filler words with no keystroke behind them

❌ before:
```json
{
  "category": ["windows"],
  "summary": "Next/previous tab gt / gT",
  "details": ["gt goes to the next tab", "gT goes to the previous tab"],
  "mnemonic": "go to tab"
}
```
✅ after:
```json
{
  "category": ["windows"],
  "summary": "Next/previous tab gt / gT",
  "details": ["gt goes to the next tab", "gT goes to the previous tab"],
  "mnemonic": "go tab"
}
```
*Why:* the keys are `g` and `t`, so the mnemonic should be exactly two words —
g→go, t→tab. "go to tab" reads more naturally as English, but the "to" maps to no
keystroke, so it dilutes the hook the reader is trying to pin to the keys. This is
the mirror of "decode every key": don't map *more* words than there are keys.
Reach for the tight word-per-key form other users already share.

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

### Anchor a family mnemonic in the real hook — and name the key a shape depicts

❌ before (invented hook, drifting through cute options like `z zips`/`z = zzz`):
```json
{
  "category": ["navigation"],
  "summary": "Fold all / unfold all zM / zR",
  "details": ["zM closes all folds in view", "zR opens all folds in view"],
  "mnemonic": "z zips code shut; M more, R reduce"
}
```
✅ after:
```json
{
  "category": ["navigation"],
  "summary": "Fold all / unfold all zM / zR",
  "details": ["zM closes all folds in view", "zR opens all folds in view"],
  "mnemonic": "z = a folded page; M more, R reduce"
}
```
*Why:* two lessons. **Decode the shared prefix, not just the distinguishing
letter** — every fold command is `z`-prefixed, so the mnemonic must say what `z`
buys, then `M`/`R`. **And anchor it in the documented hook, not an invention:**
Vim's own `usr_28` says *"z looks like a folded piece of paper viewed from the
side"* — reach for the real mnemonic other users share before minting `zip`/`zzz`.
This is also the exception to "don't echo the key": a *shape* hook must name the
key it depicts (`z = a folded page`), because unlike a letter→word decode
(`g`→go), the glyph doesn't map from the letter on its own.

### Merge a set-and-use pair when neither half stands alone

❌ before (two adjacent tips; the jump tip leans on the set tip a reader may never see):
```json
{
  "category": ["navigation"],
  "summary": "Drop a mark before exploring with ma",
  "details": ["ma sets a mark before you jump elsewhere", "`a returns to the exact position later"],
  "mnemonic": "m = mark"
}
```
```json
{
  "category": ["navigation"],
  "summary": "Jump to a mark with `a / 'a",
  "details": ["`a jumps to the exact marked position", "'a jumps to the marked line"]
}
```
✅ after (one tip carrying the whole loop):
```json
{
  "category": ["navigation"],
  "summary": "Set and jump to a mark ma / `a",
  "details": ["ma tags the current spot as mark a", "`a jumps back exactly, 'a to the line"]
}
```
*Why:* display order is random, so a "jump to a mark" tip seen alone leaves the
reader asking *"what is a mark? how do I make one?"* — it depends on a sibling
that may never appear. When two tips split the *set* and *use* halves of one
feature and neither stands alone, don't duplicate the shared prerequisite across
both — merge them into a single tip that carries the whole loop (what it is → how
to make it → how to use it). Merge beats cross-reference when the halves are that
entangled.

### Cut a command you can't try cold — doubly so when the IDE already does it

❌ before (payoff needs a file path already sitting under the cursor):
```json
{
  "category": ["cmdline", "files"],
  "summary": "Insert filename in : Ctrl-r Ctrl-f",
  "details": ["Inserts the file under cursor", "Handy for file Ex commands"],
  "mnemonic": "r = register, f = file"
}
```
✅ after: tip deleted.

*Why:* a tip must be tryable *cold* — dropped into a random balloon mid-edit, the
reader can act on it right now. `Ctrl-r Ctrl-f` does something only when a file
path happens to sit under the cursor, so most of the time there's nothing to try
(contrast its `Ctrl-r Ctrl-w` sibling — every buffer has a word under the
cursor). It also loses on reach: when you *do* have a path in the text,
IntelliJ's own `Ctrl-B` / Cmd-click is the move people already reach for. Fails
teachability *and* reach → delete, don't reword. No rewording rescues a command
that's fundamentally context-bound and redundant with a stronger IDE-native path.

### Prefer the `mode` label over a "Works in X mode" detail line

❌ before (a hand-written mode line — what shipped *before* the `mode` label existed):
```json
{
  "category": ["cmdline"],
  "summary": "Jump to ends of : Ctrl-b / Ctrl-e",
  "details": ["Ctrl-b jumps to the start", "Ctrl-e jumps to the end", "Works in Command-line mode"]
}
```
✅ after (the rendered title carries the mode; drop the prose line):
```json
{
  "category": ["cmdline"],
  "summary": "Jump to ends of : Ctrl-b / Ctrl-e",
  "details": ["Ctrl-b jumps to the start", "Ctrl-e jumps to the end"],
  "mode": "command"
}
```
*Why:* once the `mode` field renders as a dimmed `Vim Coach · Command mode`
title label, a `Works in X mode` detail line is redundant boilerplate — it
burns a balloon line to say what the label now says for free. Set `mode` and
delete the line. This **supersedes** the earlier call to give the mode its own
trailing detail line — that was the best fix only *before* the label shipped;
the earlier alternative of an `In X mode,` prefix stays rejected (it wraps line
1 and buries the keystroke). The mode word survives in the *wording* only when
it's teaching payload or grammar — leaving Insert (`Leave Insert mode with Esc`),
the momentary-Normal dip of `Ctrl-o` (`One Normal command, back to Insert`) —
never when it just names where the keys live. And tag only the true press mode:
a move that *enters* a mode from Normal (`ma`, `v`, `:s`, `i`/`a`/`o`) is a
Normal tip and stays untagged, even when it lives in `insert.json`/`visual.json`.

### A mapping/config idiom is not a tip — it's general advice

❌ rejected candidates (pitched to fill the thin `mappings` category):
```json
{ "category": ["mappings"], "summary": "Set a <leader> prefix key",
  "details": ["let mapleader=\" \" makes Space the prefix", "Then map <leader>w to save, etc."] }
{ "category": ["mappings"], "summary": "Map without recursion nnoremap",
  "details": ["nnoremap won't re-trigger other maps", "Prefer it over map for safe bindings"] }
```
✅ after: don't add — no reproducible move exists.

*Why:* both teach how to *write config*, not a keystroke the reader can press
in the balloon and watch work. `<leader>` and `nnoremap` have no on-the-spot
payoff — there's nothing to try, only advice to absorb later in an .ideavimrc.
That fails the core rule (a tip teaches one real move the reader can try right
now). A thin category is not a reason to add — density beats count. When mining
a category for gaps, discard any candidate whose payoff is "understand this for
when you edit your config" rather than "press this and see it happen." Config
belongs in a tip only as the enabling `config` block *under* a tryable move, not
as the lesson itself.
