# The style guide — worked examples (before → after)

Read this before authoring or rewording anything. Each entry shows a tip ❌
before and ✅ after — trimmed to the fields the lesson touches — then *why*.
Groups match SKILL.md's wording quick list. When the user corrects a call, the
lesson lands here as a new pair, or sharpens the entry that failed to prevent
it.

## Summary

### Lead with one runnable combo, not a bare operator

❌ `"summary": "Swap two regions cx"` · `["cx marks a region, cx again swaps", "Takes any motion"]`
✅ `"summary": "Swap two regions cxiw"` · `["cxiw marks a word, cxiw again swaps", "Takes any motion, like cxx"]`

*Why:* never a bare operator or a `{motion}` placeholder in the summary; the
last detail generalizes the open axis in prose with ≤1 example. Antonym pairs
(`Indent/outdent a block >ip / <ip`) and fixed-motion pairs (`Change/delete a
word cw / dw`) may keep two operators. (plugin: vim-exchange)

### In a slashed pair, vary one axis — keep the operator fixed

❌ `"summary": "Act on an argument cia / daa"` · `["cia changes inside an argument", "daa deletes it with its comma"]`
✅ `"summary": "Act on an argument cia / caa"` · `["i is inside, a takes the comma too", "Pairs with d, y and other operators"]`

*Why:* `cia / daa` mixes two operators *and* two text objects. Hold `c` steady
so the slash shows only `i` vs `a`; name the other operators in a detail.
(plugin: argtextobj)

### Join symbol pairs with `and` — a slash between glyphs is a pileup

❌ `"summary": "Move by paragraphs { / }"`
✅ `"summary": "Move by paragraphs { and }"`

*Why:* ` / ` joins letter pairs (`gj / gk`); between glyphs it reads as three
symbols in a row. A "consistency" edit back to ` / ` was rejected for this.

## Details

### Spend line 1 on value, not a restatement

summary: `Edit the next () pair cin)`
❌ `"details": ["cin) seeks ahead to the next pair", "Repeat with . on the next pair"]`
✅ `"details": ["Works even with the cursor outside it", "Repeat with . on the next pair"]`

*Why:* the first detail is the most-read line — spend it on what the reader
doesn't already have.

### Explain the effect, not the anatomy — a tip must read cold

summary: `Keep only matching lines :v//d`
❌ `"details": ["v = non-matching lines, d = delete"]`
✅ `"details": [":v/foo/d deletes lines lacking foo", "What's left: only the foo lines"]`

*Why:* token-naming assumes you already know `:g`. A typeable command plus a
plain-words outcome teaches a reader who's never seen the concept.

### One worked example beats a dump

summary: `Coerce word case cr{x}`
❌ `"details": ["cr + a letter picks the case", "c camel, s snake, m Mixed…"]`
✅ `"details": ["crc turns foo_bar into fooBar", "crs gives snake, crm Mixed"]`

summary: `Jump to matching bracket %`
❌ `"details": ["Cursor on a bracket jumps to its pair", "Works for ) ] } \" ' too"]`
✅ `"details": ["Cursor on a bracket jumps to its pair", "Any bracket or quote works too"]`

*Why:* one `input → output` example teaches the whole thing; a letter dump
makes the reader assemble it, and a row of bare symbols reads like keyboard
mashing — name the family, keep ≤1 example.

### Cut filler — a Useful/Handy opener or a repeated line is a cut, not a reword

summary: `Jump back / forward Ctrl-o / Ctrl-i`
❌ `"details": ["Ctrl-o goes back through older jump locations", "Ctrl-i goes forward again", "Useful after searches, definitions, and other big jumps"]`
✅ `"details": ["Ctrl-o steps back to older jumps", "Ctrl-i steps forward again", "Works across files, like after gd"]`

summary: `Make a word camelCase crc`
❌ `"details": ["crc turns foo_bar into fooBar", "cr + a letter sets the style"]`
✅ `"details": ["crc turns foo_bar into fooBar", "Cursor can sit anywhere in the word"]`

*Why:* a Useful / Handy / Great / Good for line *announces* value without a
key to press; rewording keeps the empty frame, so delete it or swap in a
use-site (`like after gd`). A line repeated across sibling tips (`cr + a
letter…` on every case tip) is filler too. Filler is also where the length
goes — trim it before trimming meaning.

### Name press-vs-type, and give setup and use their own lines

summary: `Grab all copies at once Alt-n` (multiple-cursors)
❌ `"details": ["Alt-n on foo selects every foo", "Then c bar replaces every foo"]`
✅ `"details": ["Alt-n on foo selects every foo", "Then c, type bar, Esc — all to bar"]`

summary: `Auto-expand text with :iabbrev`
❌ `"details": [":iab teh the fixes that typo", "Expands after Space or Enter"]`
✅ `"details": [":iab teh the, then press Enter", "Typing teh then Space gives the"]`

*Why:* `c bar` reads as one mystery token, and in `:iab teh the fixes that
typo` the reader couldn't tell `teh` was the typo they'd type. When a command
crosses into typed text, name the action (press `c`, type `bar`); when a tip
sets something up (`:iab`, `:map`, a mark) then uses it, finish the setup on
one line and show the trigger and result on the next.

### A search-flag tip shows the keystrokes, not the concept

summary: `Match whole words with \< and \>` (`advanced`)
❌ `"details": ["/\\<word\\> matches whole words only", "Use it to avoid partial-token matches"]`
✅ `"details": ["/ opens a search as usual", "Wrap your word: \\<in\\>", "/\\<in\\> finds in, not pin or into"]`

*Why:* "whole words", "literal", "partial-token" are jargon the reader can't
act on. Show `/`, show where the flag attaches (readers conflate `/` and `\`),
then one concrete search with a visible outcome. A trimmed 2-line version was
rejected because the dropped line was *where the flag goes* — concreteness
over brevity when the keystrokes are the lesson.

### Never number steps — line order is the sequence

summary: `Replace a block on many lines`
❌ `"details": ["1. Ctrl-v and hjkl mark the block", "2. Press c, type the new text", "3. Esc fills every line"]`
✅ `"details": ["Ctrl-v and hjkl mark the block", "Press c, type the new text", "Esc fills every line"]`

*Why:* details render top to bottom, so `1.` `2.` `3.` spend width on order
the reader gets free. The user rejected numbering outright; no multi-step
exception.

### Use the `mode` label, not a mode line

summary: `Jump to ends of : Ctrl-b / Ctrl-e`
❌ `"details": ["Ctrl-b jumps to the start", "Ctrl-e jumps to the end", "Works in Command-line mode"]`
✅ `"details": ["Ctrl-b jumps to the start", "Ctrl-e jumps to the end"]`, `"mode": "command"`

*Why:* the dimmed `Vim Coach · Command mode` title label makes the line
boilerplate. Supersedes the old trailing mode line; the `In X mode,` prefix
stays rejected (it buries the keystroke). When the mode word may stay, and
what not to tag: tagging.md → "`mode`".

## Mnemonics

Six calls, one theme: a mnemonic earns its line only when it makes the keys
stick, and then it decodes every key in a word each.

| Lesson | ❌ | ✅ |
|---|---|---|
| Decode every key, not just the ends (`ysiwt`) | `ys you surround, t tag` | `ys you surround, iw inner word, t tag` |
| Don't echo the key — the summary shows it (`gm`) | `gm = go menu (refactor)` | `go menu (refactor)` |
| Prefer the community reading (`cst`) | `cs = change surround, t = tag` | `change surrounding tag` |
| One word per key — no filler (`gt / gT`) | `go to tab` | `go tab` |
| Drop an obvious decode (`nmap`/`imap`/`vmap` tip) | `n/i/v = normal/insert/visual` | *(no mnemonic)* |
| Anchor a family in the real hook; a shape names its key (`zM / zR`) | `z zips code shut; M more, R reduce` | `z = a folded page; M more, R reduce` |

*Why:* a half-decode leaves the reader guessing the confusing middle — drop
`=` separators before dropping a keystroke. The renderer already prepends
`Mnemonic:` and the summary shows the key, so `gm =` says it twice. tpope's
own reading (*change surrounding tag*) beats a homemade gloss and satisfies
both rules at once. `to` in `go to tab` maps to no key and dilutes the hook.
`n/i/v` taught nothing and restated a detail line. For a prefix family, decode
the prefix too, from the documented hook (Vim's `usr_28`: *"z looks like a
folded piece of paper"*) — and a *shape* hook must name the key it depicts,
since the glyph doesn't map from the letter.

## Truth — verify against IdeaVim

### Two keys earn a slashed pair only if they really differ

❌ `"summary": "Jump to definition gd / gD"` · `["gd jumps to definition", "gD jumps to declaration"]`
✅ `"summary": "Jump to definition gd"` · `["Lands on where the symbol is defined", "gD does the same in IdeaVim"]`

❌ shipped for months: `"summary": "Delete folds zd / zD"` · `["zd deletes one fold at the cursor", "zD deletes nested folds there too"]`, `advanced`
✅ tip deleted; `zd` folds into its `zf` host:
```json
{ "summary": "Create a fold zfip",
  "details": ["zfip folds the current paragraph", "Takes any motion, like zf} or zfa{", "zd deletes it; IDE folds come back"] }
```
*Why:* upstream Vim splits `gd`/`gD`; IdeaVim binds both to
`GotoDeclarationAction`. `zd`/`zD` *looked* confirmed — distinct handler
classes — but both resolve through `findInnermostFoldAtLine`, and an innermost
fold has nothing nested, so `zD`'s recursion almost never fires. Read to the
bottom of the call chain, not just the binding. Three traps compound it: a
command can work and show nothing (IntelliJ regenerates deleted IDE folds on
reparse — `VimEditor.kt`'s KDoc says so; read doc comments too); a key may be
newer than the reader's build (release gate, reference.md); and **re-score
before rewording** — a tip that survives only by telling the reader to *make*
a fold so they can delete it has nothing to try cold. Ask *should this exist*
before *how should this read*.

### A `!` that parses isn't a `!` that works

❌ `"summary": "Quit :q / :q!"` · `[":q closes if no unsaved changes", ":q! forces close/discard (if allowed)", "ZQ is the key form of :q!"]`
✅ `"summary": "Close this editor tab :q"` · `[":q!, ZQ and :bd do the same", "Edits are kept, nothing discarded"]`

*Why:* `QuitCommand` never reads its bang, so the IDE keeps the edits the tip
promised to discard. If the bang is ignored, that *is* the lesson — "do the
same" beats a promise the IDE won't keep. The grep that proves it:
reference.md → "Checking IdeaVim support".

### A config button that sets the default is a no-op

❌ before (`ideavim.json`):
```json
{ "summary": "Sync marks with :set ideamarks",
  "details": ["A-Z marks sync to IDE bookmarks", "Great for cross-file jumps"],
  "config": { "name": "Enable ideamarks", "lines": ["set ideamarks"] } }
```
✅ after (`navigation.json`, no `config`):
```json
{ "summary": "Mark a spot across files mA",
  "details": ["'A jumps back to it from any file", "It shows as an IDE bookmark too"] }
```
*Why:* `ideamarks` is already on, so Apply changed nothing and the reader
stopped trusting the button. When the behavior is already live, teach the
keystroke that shows it and drop the button. Near-miss: a button whose new
behavior is *no better* than the default for the move shown — `set
idearefactormode=keep` taught `ciw` on a rename, but the default Select mode
already replaces the name as you type. Cut as too niche.

## Shape — add, split, merge, cut

### Search before adding — and sharpen a host instead of minting a sibling

❌ new tip: `"summary": "Repeat search then center n zz"` · `["n jumps to the next match", "zz centers the line"]`
✅ drop it — `Recenter search results with nzz` already exists (`["n finds the next match, zz centers", "Nzz does it the other way"]`).

❌ new tip pitched beside an existing `yss` host:
```json
{ "summary": "Surround with any character ysiw*",
  "details": ["ysiw* gives *word*", "Any non-letter works: _ | # $"] }
```
host before: `["yss) surrounds the line with ( )", "Any bracket or quote works too"]`
✅ no new tip; host after: `["yss) surrounds the line with ( )", "Any non-letter works, like yss*"]`

*Why:* the generator rejects only *identical* summaries. And a host line that
gestures at your candidate (`Any bracket or quote`) is the same rule stated
too narrowly — IdeaVim's `getSurroundPair()` pairs any non-letter with itself.
Sharpen that line: same balloon count, one fewer half-truth. A genuinely
separate argument the host never mentions (the shift-free `b`/`B`/`r`/`a`
aliases) does earn its own tip.

### Split by intent, not key count

❌ one tip, two intents:
```json
{ "summary": "Open, close, or toggle a fold",
  "details": ["za toggles the fold under you", "zo forces open, zc forces closed"] }
```
✅ one tip per intent:
```json
{ "summary": "Toggle a fold with za",
  "details": ["Opens it if closed, closes if open", "One key covers most fold work"] }
{ "summary": "Force a fold open or closed",
  "details": ["zo opens the fold, zc closes it", "Works when you know the end state"] }
```
*Why:* toggle and force are different intents; a same-intent direction pair
(`gj / gk`, `g0 / g$`) stays one tip. Each split tip must stand alone and earn
a distinct summary.

### Merge a set-and-use pair when neither half stands alone

❌ two tips, the jump leaning on a set tip the reader may never see:
```json
{ "summary": "Drop a mark before exploring with ma",
  "details": ["ma sets a mark before you jump elsewhere", "`a returns to the exact position later"] }
{ "summary": "Jump to a mark with `a / 'a",
  "details": ["`a jumps to the exact marked position", "'a jumps to the marked line"] }
```
✅ one tip carrying the whole loop:
```json
{ "summary": "Set and jump to a mark ma / `a",
  "details": ["ma tags the current spot as mark a", "`a returns exactly, 'a to the line"] }
```
*Why:* order is random, so a lone jump tip leaves "what is a mark?" Merge
beats cross-reference when the halves are that entangled. A looser case a
reader caught: `Search forward/backward / and ?` plus `Next/previous match n /
N` → merged into `Search and hop through matches /` (`["/foo Enter jumps to
the next foo", "n goes to the next one, N back", "? searches backward
instead"]`).

### Theory earns one tip at most — and it must still be tryable

❌ `"summary": "Change/delete with operator + motion"` · `["d{motion} deletes text", "c{motion} changes text"]`
✅ tip deleted; its rule folds into one host: `"summary": "Change/delete a word cw / dw"` · `["cw retypes the word, dw removes it", "Same d/c works with any motion"]`

*Why:* `{motion}` gives the reader nothing to try, and rewording around it
kept failing. Fold the rule into exactly *one* concrete host — never echo it
across siblings.

### Cut a command you can't try cold — doubly so when the IDE already does it

❌ `"summary": "Insert filename in : Ctrl-r Ctrl-f"` · `["Inserts the file under cursor", "Handy for file Ex commands"]`
✅ tip deleted.

*Why:* it does something only when a file path happens to sit under the
cursor (contrast `Ctrl-r Ctrl-w` — there's always a word), and when there *is*
a path, IntelliJ's `Ctrl-B` / Cmd-click is what people reach for. Fails
teachability *and* reach → delete, don't reword.

### A config idiom is not a tip

❌ pitched to fill the thin `mappings` category:
```json
{ "summary": "Set a <leader> prefix key",
  "details": ["let mapleader=\" \" makes Space the prefix", "Then map <leader>w to save, etc."] }
{ "summary": "Map without recursion nnoremap",
  "details": ["nnoremap won't re-trigger other maps", "Prefer it over map for safe bindings"] }
```
✅ don't add — no reproducible move exists.

*Why:* both teach how to *write config*, with nothing to press in the balloon
and watch work. A thin category is not a reason to add. Config belongs in a
tip only as the enabling `config` block *under* a tryable move.
