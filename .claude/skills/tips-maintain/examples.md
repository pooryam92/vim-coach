# The style guide — worked examples (before → after)

This file is how tip style is taught: **read it before authoring or rewording
anything.** Each entry shows the tip ❌ before and ✅ after — trimmed to the
fields the lesson touches (unchanged `category`/`config`/`mnemonic` are omitted;
the canonical full shape lives in SKILL.md) — then *why*. When the user corrects
or rejects a wording call, the lesson lands here as a new entry (or sharpens the
one that failed) — this file is the skill's memory.

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
- Anchor a family mnemonic in the real hook — and name the key a shape depicts
- Merge a set-and-use pair when neither half stands alone
- Cut a command you can't try cold — doubly so when the IDE already does it
- Prefer the `mode` label over a "Works in X mode" detail line
- A mapping/config idiom is not a tip — it's general advice
- A vaguer rule already in a detail line — sharpen it, don't mint a sibling
- A search-flag tip shows the keystrokes to type, not the concept

### Name the family in prose, never dump symbols

❌ `"details": ["Cursor on a bracket jumps to its pair", "Works for ) ] } \" ' too"]`
✅ `"details": ["Cursor on a bracket jumps to its pair", "Any bracket or quote works too"]`

*Why:* a row of bare symbols reads like keyboard mashing — name the family, keep
≤1 example. (summary: `Jump to matching bracket %`)

### Don't restate the summary in detail line 1

summary: `Edit the next () pair cin)`
❌ `"details": ["cin) seeks ahead to the next pair", "Repeat with . on the next pair"]`
✅ `"details": ["Works even with the cursor outside it", "Repeat with . on the next pair"]`

*Why:* the first detail is the most-read line — spend it on the *value*, not a
rephrase of the summary.

### Explain the effect, not the anatomy — a tip must read cold

summary: `Keep only matching lines :v//d`
❌ `"details": ["v = non-matching lines, d = delete"]`
✅ `"details": [":v/foo/d deletes lines lacking foo", "What's left: only the foo lines"]`

*Why:* token-naming still assumes you know `:g`/global commands. Anatomy *looks*
educational but recycles the jargon; a typeable command + plain-words outcome
teaches a reader who's never seen the concept.

### One worked example beats a meta description + letter dump

summary: `Coerce word case cr{x}`
❌ `"details": ["cr + a letter picks the case", "c camel, s snake, m Mixed…"]`
✅ `"details": ["crc turns foo_bar into fooBar", "crs gives snake, crm Mixed"]`

*Why:* a single `input → output` example teaches the whole thing; the letter dump
makes the reader assemble it themselves.

### Name press-vs-type when an example crosses into Insert mode

summary: `Edit every copy of a word Alt-n`
❌ `"details": ["Alt-n selects foo, repeat for more", "Then c bar replaces every foo"]`
✅ `"details": ["Alt-n selects foo, repeat for more", "Then c, type bar, Esc — all to bar"]`

*Why:* from the reader's seat `c bar` is one mystery token (they ask "what is c
bar?") — it jams a command and the text you type together. When an example crosses
from a command into Insert-mode typing, name the action — press `c`, type `bar` —
so keystrokes read apart from input.

### Split by intent, not key count

❌ before (one tip cramming two intents):
```json
{ "summary": "Open, close, or toggle a fold",
  "details": ["za toggles the fold under you", "zo forces open, zc forces closed"] }
```
✅ after (one tip per intent):
```json
{ "summary": "Toggle a fold with za",
  "details": ["Opens it if closed, closes if open", "One key covers most fold work"] }
{ "summary": "Force a fold open or closed",
  "details": ["zo opens the fold, zc closes it", "Works when you know the end state"] }
```
*Why:* toggle and force are *different intents*, so they are different tips; a
same-intent direction pair (`gj / gk`, `g0 / g$`) stays together as one tip.
Key count alone decides nothing. Display order is random, so each split tip
must stand alone and earn a *distinct* summary — the generator rejects
duplicates.

### Cut lines that don't earn their place

summary: `Make a word camelCase crc`
❌ `"details": ["crc turns foo_bar into fooBar", "cr + a letter sets the style"]`
✅ `"details": ["crc turns foo_bar into fooBar", "Cursor can sit anywhere in the word"]`

*Why:* the trailing `cr + a letter sets the style` repeated on every case tip adds
nothing — replace filler with a fact the reader doesn't already have.

### In a slashed pair, vary one axis — keep the operator fixed

❌ `"summary": "Act on an argument cia / daa"` · `["cia changes inside an argument", "daa deletes it with its comma"]`
✅ `"summary": "Act on an argument cia / caa"` · `["i is inside, a takes the comma too", "Pairs with d, y and other operators"]`

*Why:* `cia / daa` mixes two operators *and* two text objects. Hold the operator
steady (`c`) so the slash shows just the text-object difference (`i` vs `a`); name
the other operators in a detail. (plugin: argtextobj)

### Operator + motion tips — lead concrete, generalize the open axis

❌ `"summary": "Swap two regions cx"` · `["cx marks a region, cx again swaps", "Takes any motion"]`
✅ `"summary": "Swap two regions cxiw"` · `["cxiw marks a word, cxiw again swaps", "Takes any motion, like cxx"]`

*Why:* the summary leads with one concrete, runnable combo — never a bare operator
or a `{motion}` placeholder. The last detail generalizes the open axis in prose
with ≤1 example. Antonym pairs (`Indent/outdent a block >ip / <ip`) and
fixed-motion change/delete pairs (`Change/delete a word cw / dw`) may keep two
operators in the title. (plugin: vim-exchange)

### Verify the claim against IdeaVim source, not Vim lore

❌ `"summary": "Jump to definition gd / gD"` · `["gd jumps to definition", "gD jumps to declaration"]`
✅ `"summary": "Jump to definition gd"` · `["Lands on where the symbol is defined", "gD does the same in IdeaVim"]`

*Why:* upstream-Vim splits the two, but in `external/ideavim` both keys bind to the
same `GotoDeclarationAction` — confirm in the submodule before claiming a
distinction.

This failed to prevent a second instance, so it hardens into a rule: **a
lowercase/uppercase pair earns a slashed summary only once you've confirmed the
two keys reach different handlers *and* produce different results** — read to the
bottom of the call chain, not just the binding.

❌ shipped for months: `"summary": "Delete folds zd / zD"`, `"details": ["zd deletes one fold at the cursor", "zD deletes nested folds there too"]`, `advanced`.
✅ tip deleted; `zd` folds into its `zf` host as one line:
```json
{ "summary": "Create a fold zfip",
  "details": ["zfip folds the current paragraph", "Takes any motion, like zf} or zfa{", "zd deletes it; IDE folds come back"] }
```
*Why:* separate handlers weren't enough. `VimDeleteFoldAtCursor` and
`VimDeleteFoldsRecursivelyAtCursor` are distinct classes, so a keys-only grep
*looked* confirmed — but both resolve their target through `findInnermostFoldAtLine`,
and an innermost fold has nothing nested, so `zD`'s recursion almost never fires.
Three traps compound it: (1) a command can "work" and show nothing — `zd` deletes
an IntelliJ fold, but the folding builder regenerates it on the next reparse, so
only manual `zf` folds visibly stay deleted (`VimEditor.kt` KDoc says so — read
the doc comment, not just the code); (2) a fold/IDE-bridge command may be newer
than the reader's plugin — `zd` shipped in IdeaVim 2.29.0, so `git tag --contains
<commit>` when a tip teaches a recent key; (3) **re-score before rewording** — a
false claim is often the symptom, but a tip surviving only by telling the reader
to *make* a fold so they can delete it has nothing to try cold and is half
disclaimer. That's the disease → it belongs in "Cut a command you can't try cold."
Ask *should this exist* before *how should this read*.

### Search existing tips before adding — kill semantic duplicates

❌ new tip: `"summary": "Repeat search then center n zz"` · `["n jumps to the next match", "zz centers the line"]`
✅ drop it — `"summary": "Recenter search results nzz"` already exists.

*Why:* the generator only rejects *identical* summaries, so the same idea under
different wording slips through. Grep both the keys *and* the behavior first; drop
or merge instead of adding.

### Decode every key in a mnemonic, not just the ends

summary: `Wrap in an HTML tag ysiwt`
❌ `"mnemonic": "ys you surround, t tag"`
✅ `"mnemonic": "ys you surround, iw inner word, t tag"`

*Why:* the summary key is `ysiwt` but the mnemonic decoded only `ys` and `t`,
skipping `iw` — the confusing middle. A mnemonic that maps some keystrokes must map
them all; a half-decode leaves the reader guessing the part they most need. Keep it
≤40 chars (drop `=` separators before you drop a keystroke).

### Give the decoded words, don't echo the key — the summary already shows it

summary: `Open the refactor menu gm`
❌ `"mnemonic": "gm = go menu (refactor)"`
✅ `"mnemonic": "go menu (refactor)"`

*Why:* the summary already shows `gm` and the renderer prepends `Mnemonic:`, so
`gm =` restates the key twice before the hook lands. Author just the decoded words —
`go menu` already maps g→go, m→menu on its own. This doesn't contradict "decode
every key": the letters still all map, you just drop the redundant `key =` echo.

### Reach for the community mnemonic, not a homemade key-echo

summary: `Rename an HTML tag cst`
❌ `"mnemonic": "cs = change surround, t = tag"`
✅ `"mnemonic": "change surrounding tag"`

*Why:* `cst` already has a reading the whole vim-surround community shares —
*"change surrounding tag"* (tpope's own docs, every cheatsheet). Reach for that
before minting your own gloss. It also satisfies both mnemonic rules at once: it
decodes all three keys (c→change, s→surrounding, t→tag) and echoes none of them.
When a plugin key has an idiomatic name, use it — a homemade key-echo is both less
memorable and less standard.

### One word per key — cut filler words with no keystroke behind them

summary: `Next/previous tab gt / gT`
❌ `"mnemonic": "go to tab"`
✅ `"mnemonic": "go tab"`

*Why:* the keys are `g` and `t`, so the mnemonic should be exactly two words —
g→go, t→tab. "go to tab" reads more naturally as English, but the "to" maps to no
keystroke, so it dilutes the hook. This is the mirror of "decode every key": don't
map *more* words than there are keys. Reach for the tight word-per-key form other
users already share.

### Drop a mnemonic whose decode is obvious — don't fill the slot

summary: `Use mode-specific maps in ~/.ideavimrc`
details: `["Use nmap, imap, or vmap for the target mode", "Normal, Insert, and Visual maps should stay separate"]`
❌ `"mnemonic": "n/i/v = normal/insert/visual"`
✅ (no mnemonic)

*Why:* `n`/`i`/`v` → normal/insert/visual is self-evident to anyone reading a maps
tip — the hook teaches nothing, and here it even restates detail line 2 word for
word. A mnemonic earns its line only when the keys wouldn't otherwise stick; when
the decode is obvious or already stated, delete it rather than fill the slot.

### Join symbol pairs with `and` — a slash between glyphs is a pileup

❌ `"summary": "Move by paragraphs { / }"`
✅ `"summary": "Move by paragraphs { and }"`

*Why:* ` / ` is the default joiner for letter-key pairs (`gj / gk`), but between
bracket/symbol glyphs it reads as three symbols in a row. `and` keeps the pair
readable. (A proposed "consistency" edit normalizing `and` back to ` / ` was
rejected for exactly this reason.)

### Theory earns one tip at most — and it must still be tryable

❌ before (a grammar tip with nothing to press):
`"summary": "Change/delete with operator + motion"` · `["d{motion} deletes text", "c{motion} changes text"]`
✅ after (tip deleted; its rule folds into one concrete host):
`"summary": "Change/delete a word cw / dw"` · `["cw retypes the word, dw removes it", "Same d/c works with any motion"]`

*Why:* `{motion}` placeholders give the reader nothing to try, and rewording
around them keeps failing. When a concept has no tryable form, delete the
standalone tip and fold its rule into exactly *one* concrete host — never echo
it across every sibling. Practical tips lead; theory gets at most one
consolidated line.

### Anchor a family mnemonic in the real hook — and name the key a shape depicts

summary: `Fold all / unfold all zM / zR`
❌ `"mnemonic": "z zips code shut; M more, R reduce"` (invented hook, drifting through `z zips`/`z = zzz`)
✅ `"mnemonic": "z = a folded page; M more, R reduce"`

*Why:* two lessons. **Decode the shared prefix, not just the distinguishing
letter** — every fold command is `z`-prefixed, so the mnemonic must say what `z`
buys, then `M`/`R`. **And anchor it in the documented hook, not an invention:**
Vim's own `usr_28` says *"z looks like a folded piece of paper viewed from the
side"* — reach for the real mnemonic before minting `zip`/`zzz`. This is also the
exception to "don't echo the key": a *shape* hook must name the key it depicts
(`z = a folded page`), because unlike a letter→word decode (`g`→go), the glyph
doesn't map from the letter on its own.

### Merge a set-and-use pair when neither half stands alone

❌ before (two adjacent tips; the jump tip leans on the set tip a reader may never see):
```json
{ "summary": "Drop a mark before exploring with ma",
  "details": ["ma sets a mark before you jump elsewhere", "`a returns to the exact position later"] }
{ "summary": "Jump to a mark with `a / 'a",
  "details": ["`a jumps to the exact marked position", "'a jumps to the marked line"] }
```
✅ after (one tip carrying the whole loop):
```json
{ "summary": "Set and jump to a mark ma / `a",
  "details": ["ma tags the current spot as mark a", "`a jumps back exactly, 'a to the line"] }
```
*Why:* display order is random, so a "jump to a mark" tip seen alone leaves the
reader asking *"what is a mark? how do I make one?"* — it depends on a sibling
that may never appear. When two tips split the *set* and *use* halves of one
feature and neither stands alone, don't duplicate the shared prerequisite across
both — merge them into a single tip that carries the whole loop (what it is → how
to make it → how to use it). Merge beats cross-reference when the halves are that
entangled.

### Cut a command you can't try cold — doubly so when the IDE already does it

❌ before (payoff needs a file path already under the cursor):
`"summary": "Insert filename in : Ctrl-r Ctrl-f"` · `["Inserts the file under cursor", "Handy for file Ex commands"]`
✅ after: tip deleted.

*Why:* a tip must be tryable *cold* — dropped into a random balloon mid-edit, the
reader can act on it right now. `Ctrl-r Ctrl-f` does something only when a file
path happens to sit under the cursor, so most of the time there's nothing to try
(contrast its `Ctrl-r Ctrl-w` sibling — every buffer has a word under the
cursor). It also loses on reach: when you *do* have a path in the text,
IntelliJ's own `Ctrl-B` / Cmd-click is the move people already reach for. Fails
teachability *and* reach → delete, don't reword.

### Prefer the `mode` label over a "Works in X mode" detail line

summary: `Jump to ends of : Ctrl-b / Ctrl-e`
❌ `"details": ["Ctrl-b jumps to the start", "Ctrl-e jumps to the end", "Works in Command-line mode"]`
✅ `"details": ["Ctrl-b jumps to the start", "Ctrl-e jumps to the end"]`, `"mode": "command"`

*Why:* once the `mode` field renders as a dimmed `Vim Coach · Command mode`
title label, a `Works in X mode` detail line is redundant boilerplate. Set `mode`
and delete the line. This **supersedes** the earlier call to give the mode its own
trailing detail line — that was best only *before* the label shipped; the `In X
mode,` prefix stays rejected (it wraps line 1 and buries the keystroke). The mode
word survives in the *wording* only when it's teaching payload or grammar —
leaving Insert (`Leave Insert mode with Esc`), the momentary-Normal dip of
`Ctrl-o` (`One Normal command, back to Insert`) — never when it just names where
the keys live. And tag only the true press mode: a move that *enters* a mode from
Normal (`ma`, `v`, `:s`, `i`/`a`/`o`) is a Normal tip and stays untagged, even
when it lives in `insert.json`/`visual.json`.

### A mapping/config idiom is not a tip — it's general advice

❌ rejected candidates (pitched to fill the thin `mappings` category):
```json
{ "summary": "Set a <leader> prefix key",
  "details": ["let mapleader=\" \" makes Space the prefix", "Then map <leader>w to save, etc."] }
{ "summary": "Map without recursion nnoremap",
  "details": ["nnoremap won't re-trigger other maps", "Prefer it over map for safe bindings"] }
```
✅ after: don't add — no reproducible move exists.

*Why:* both teach how to *write config*, not a keystroke the reader can press in
the balloon and watch work. `<leader>` and `nnoremap` have no on-the-spot payoff —
there's nothing to try, only advice to absorb later in an .ideavimrc. That fails
the core rule (a tip teaches one real move the reader can try right now). A thin
category is not a reason to add — density beats count. When mining a category for
gaps, discard any candidate whose payoff is "understand this for when you edit
your config" rather than "press this and see it happen." Config belongs in a tip
only as the enabling `config` block *under* a tryable move, not as the lesson.

### A vaguer rule already in a detail line — sharpen it, don't mint a sibling

❌ pitched as a new tip, while the `yss` tip already carried a weaker version of the same rule:
```json
{ "summary": "Surround with any character ysiw*",
  "details": ["ysiw* gives *word*", "Any non-letter works: _ | # $"] }
```
host, before: `"summary": "yss) wraps the whole line"` · `["yss) surrounds the line with ( )", "Any bracket or quote works too"]`
✅ after: no new tip — expand the host's weak line in place:
host, after: `["yss) surrounds the line with ( )", "Any non-letter works, like yss*"]`

*Why:* `Any bracket or quote works too` was the *same rule stated too narrowly* —
IdeaVim's `getSurroundPair()` pairs any non-letter with itself. A new tip would
have split one rule across two balloons that each half-teach it, and the reader
who draws only the `yss` tip still leaves believing the narrow version. When a
candidate's lesson is the general form of a line an existing tip already hedges,
the win is a stricter detail line, not a new entry: same balloon count, one fewer
half-truth. Search hits are for the *behavior*, not just the keys — a detail line
that gestures at your candidate is a rewrite target, not a green light. Contrast a
genuinely separate argument the host never gestures at (the shift-free
`b`/`B`/`r`/`a` aliases), which does earn its own tip.

### A search-flag tip shows the keystrokes to type, not the concept

summary: `Match whole words with \< and \>` (`advanced`)
❌ `"details": ["/\\<word\\> matches whole words only", "Use it to avoid partial-token matches"]`
✅ `"details": ["/ opens a search as usual", "Wrap your word: \\<in\\>", "/\\<in\\> finds in, not pin or into"]`

*Why:* a search-flag tip (`\V`, `\c`, `\v`, `\<\>`) fails cold when it *names* the
concept — "literal", "whole words", "partial-token matches" are jargon the reader
can't act on. Every line must be a keystroke they type and a result they watch,
and the tip must place the flag: it rides *inside* the pattern after `/`, which
real users conflate (`/` vs the `\` flag). Show `/`, show where the flag attaches,
then one concrete search on a concrete word with the visible outcome (`/\<in\>`
hits `in`, skips `pin`/`into`). Replace the placeholder (`word`) with a real token,
and drop any describe-the-concept line. The three type-along lines earn their
length even on an advanced tip — the user rejected a trimmed 2-line version because
the dropped line was *where the flag goes*, the exact thing that was unclear.
Concreteness over brevity when the keystrokes are the lesson.
