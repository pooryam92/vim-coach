# Tip review — 2026-06-20

Adversarial review of all **309 tips** in `tips/categories/*.json` plus the
current uncommitted diff, judged against the `tips-maintain` skill's
reader's-seat and wording rules.

- **Method:** 14 per-category reviewers + 3 cross-cutting passes (duplicates,
  recent diff, removal/density) → every finding put through a skeptical
  *refute* pass that checked each support claim against the `external/ideavim`
  submodule. 34 raw findings → **31 survived verification**.
- **Validator baseline:** `node scripts/generate-tips.mjs --check` → `validated
  309 tips` (clean). Every claim below was confirmed against IdeaVim source;
  the six HIGH items were additionally re-confirmed by hand (file:line cited).
- **Not yet applied.** Per the skill, fixes go in small batches (≤2 tips) with
  a before→after and a go-ahead. This file is the worklist, ranked by severity.

| Severity | Count | Nature |
|---|---|---|
| **HIGH** | 6 | Support bugs — tip teaches behavior IdeaVim does not have (one is *destructive*) |
| **MEDIUM** | 16 | Wrong/misleading wording, cross-wording duplicates, an undisclosed key shadow |
| **LOW** | 9 | Noun-label summaries, line-1 restatements, abbreviations — wording polish |

> **Wording-churn cost:** renaming a *summary* resets the user's hide preference
> for that tip (the hide key hashes the trimmed summary). Findings that change
> only `details` carry no such cost and are cheap to apply.

---

## HIGH — support bugs (teach behavior IdeaVim lacks)

These tell the reader to do something that does nothing — or worse. Highest
priority: a wrong tip in this set is worse than a missing one.

### H1. ✅ DONE (2026-06-22) — `files.json` — "Find a file on 'path' with :find {file}"
- **Issue:** support-claim · **Rule 12** (verify support)
- **Before:**
  - summary: `Find a file on 'path' with :find {file}`
  - `:find searches Vim's 'path' and edits the match`
  - `Useful when the file is not in the current directory`
- **After:**
  - summary: `Open a file by name :find {file}`
  - `Searches the project, not 'path'`
  - `Bare :find triggers Go to File`
- **Why / verified:** IdeaVim has **no `'path'` option** and `FindFileCommand`
  never consults one. With an arg it calls `openFile`→`findFile` (resolves via
  home `~/`, project basePath, absolute path, content roots, then project files
  by name); with **no** arg it runs the IDE `GotoFile` action. The `'path'`
  framing is upstream-Vim behavior IdeaVim doesn't replicate — a reader who
  tries it on a file outside the project sees no match.

### H2. ✅ DONE (2026-06-22) — `pattern.json` — "Count matches with :s///n or %"  → **DELETE**
- **Issue:** support-claim · **Rule 12** + removal rule (actively destructive)
- **Before:**
  - summary: `Count matches with :s///n or %`
  - `:%s/pat//gn counts matches without changing`
  - `Or after /pat, run :%s///gn`
- **After:** **DELETE.**
- **Why / verified:** IdeaVim parses the substitute `n` flag but **ignores it**
  — `VimSearchGroupBase.kt:1248-1250` (`// TODO: Support 'n' to report number of
  matches without substituting`). There is no count-only guard, so
  `:%s/pat//gn` runs the substitution with an **empty replacement and deletes
  every match** instead of counting them. The tip teaches a "read-only" command
  that is in fact destructive. It is the only match-counting tip, so deletion
  loses no stronger sibling. *(For counting, `pattern.json` could later gain a
  `:g/pat/` or status-line approach — separate task, verify first.)*

### H3. ✅ DONE (2026-06-22) — `registers.json` — "Paste and reindent ]p / [p"
- **Issue:** support-claim · **Rule 12** (the description is **backwards**)
- **Before:**
  - summary: `Paste and reindent ]p / [p`
  - `]p pastes after and reindents`
  - `[p pastes before and reindents`
- **After:**
  - summary: `Paste keeping indent ]p / [p`  *(28 chars)*
  - `]p pastes below without reindenting`
  - `[p pastes above without reindenting`
- **Why / verified:** `]p`/`[p` map to the **NoIndent** put actions
  (`PutText...NoIndentAction`, `indent = false` in `PutTextAction.kt`) — the
  *opposite* of plain `p` (`indent = true`). `VimPutBase.kt:359`
  (`if (indent) endOffset = doIndent(...)`) confirms `indent=false` skips
  reindent. The tip claims the reverse of what IdeaVim does.

### H4. ✅ DONE (2026-06-22) — `windows.json` — "Swap two splits Ctrl-w r / Ctrl-w x"  → **DELETE**
- **Issue:** support-claim · **Rule 12**
- **Before:** summary `Swap two splits Ctrl-w r / Ctrl-w x`; details "rotates
  the splits…" / "swaps this split with the next one".
- **After:** **DELETE.**
- **Why / verified:** Neither `<C-W>r` nor `<C-W>x` is registered anywhere in
  IdeaVim, and **no rotate/exchange window action exists**. The complete set of
  registered `<C-W>` keys is `+ - = > < _ | c o s S v w W h j k l` (+ `<C-..>`/
  arrow variants). Both keys are no-ops.

### H5. ✅ DONE (2026-06-22) — `windows.json` — "Move a split to an edge Ctrl-w H/J/K/L"  → **DELETE**
- **Issue:** support-claim · **Rule 12**
- **Before:** summary `Move a split to an edge Ctrl-w H/J/K/L`; details
  "moves this split far left/down/up/right" / "re-orients…".
- **After:** **DELETE.**
- **Why / verified:** Only **lowercase** `<C-W>h/j/k/l` are registered
  (`WindowAction.kt` — they *move focus*). Uppercase `<C-W>H/J/K/L` (move window
  to edge + re-orient) is not registered in the engine, frontend, or any bundled
  extension. Every keystroke and the "re-orients" claim is a no-op.

### H6. ✅ DONE (2026-06-22) — `windows.json` — "New tab :tabnew / Ctrl-w T"  → **DELETE**
- **Issue:** support-claim · **Rule 12** (both halves unsupported)
- **Before:** summary `New tab :tabnew / Ctrl-w T`; details ":tabnew opens an
  empty tab page" / "Ctrl-w T moves the current split into its own tab".
- **After:** **DELETE.**
- **Why / verified:** `:tabnew` is **absent** from IdeaVim's ex-commands — only
  `tabn[ext]`, `tabN[ext]`/`tabp[revious]`, `tabc[lose]`, `tabm[ove]`,
  `tabo[nly]` are registered. `<C-W>T` is not in the registered `<C-W>` set
  (H4). Neither half does anything.

> **HIGH cascade note:** H4/H5/H6 drop `windows.json` from 18 → 15 tips — a
> legitimate density win. The surviving resize/maximize tips (`Ctrl-w +/-/=/<
> />`, `Ctrl-w _ / |`) use only registered keys and are fine (see M12 for the
> resize *wording*).

---

## MEDIUM — wrong wording, duplicates, undisclosed shadow

### M1. `insert.json` — "Insert at line ends I / A" *(factual mislabel)*
- **Issue:** reader-seat-vague · **Rules 7, 9**
- **Before:** summary `Insert at line ends I / A` — "I inserts at first non-blank" / "A appends at end of line"
- **After:** summary `Insert at line start/end I / A` (details unchanged)
- **Why:** Read cold, "line ends" mislabels `I`, which inserts at the line
  **start** (first non-blank) — the detail even says so. `start/end` names both
  outcomes; 30 chars, clean pair.

### M2. `navigation.json` — "Jump to change/yank lines '[ / ']"
- **Issue:** support-claim · **Rule 12**
- **Before:** "`'[` first line of the last change or **yank**" / "`']` jumps to its last line"
- **After:** summary `Jump to last change lines '[ / ']`; "`'[` first line of the last change or **paste**" / "`']` jumps to its last line"
- **Why / verified:** `YankGroupBase` only calls `storeText`, never
  `setChangeMarks`; the `[`/`]` marks are set on **change** (`VimChangeGroupBase`)
  and **put** (`VimPutBase`) only. After a plain yank, `'[` doesn't jump to the
  yank. (See cascade — the backtick sibling M3 has the same bug.)

### M3. `navigation.json` — "Jump to change/yank ends `` `[ / `] ``"
- **Issue:** support-claim · **Rule 12** (same root cause as M2)
- **Before:** "`` `[ `` exact start of the last change or **yank**" / "`` `] `` jumps to its exact end"
- **After:** summary `` Jump to last change ends `[ / `] ``; "`` `[ `` start of last change or **paste**" / "`` `] `` jumps to its exact end"
- **Why / verified:** Same as M2 — change marks are set on change/put, not yank.

### M4. `editing.json` — "Delete/change to line end D / C"  → **MERGE**
- **Issue:** duplicate (cross-wording) · Search-before-adding / each-tip-stands-alone
- **Before:** summary `Delete/change to line end D / C` — "D is shorthand for d$" / "C is shorthand for c$". A sibling tip carries the **identical** summary `Delete/change to line end` for `d$ / c$`.
- **After:** **MERGE** into the `d$ / c$` tip, then delete this one:
  - summary `Delete/change to line end d$ / c$` *(33 chars)*
  - `D / C are the one-key forms`
  - `d$ deletes, c$ changes the rest`
- **Why:** Two tips, identical summary, one behavior — this one just restates the
  sibling's keys as shorthand. `D`=`d$`, `C`=`c$` is canonical and worth keeping,
  so merge (not delete). Folds the one-key value into a detail line.

### M5. `plugins.json` — "Delete one comma-separated item di,"  → **MERGE**
- **Issue:** duplicate (i/a pair split) · Consistent pair phrasing
- **Before:** this and "Delete one comma-separated item **da,**" share a summary
  prefix and the same `targets.vim` behavior, differing only by `i` vs `a`.
- **After:** **MERGE** into one slashed-pair tip, delete the standalone:
  - summary `Delete a comma item da, / di,` *(29 chars)*
  - `da, takes the item and a comma`
  - `di, keeps the commas in place`
  - `Any separator works, e.g. ; or |`
  - keep `category: ["plugins","editing"]` + the Install targets.vim config.
- **Why:** Everywhere else the set folds the inside/around contrast into one tip
  (`diw / daw`, `di( / da(`, `cia / caa`). Two near-identical summaries here is
  the same behavior under different wording.

### M6. `ideavim.json` — "Find all usages gr" *(recent diff — undisclosed shadow)*
- **Issue:** undisclosed-shadow · reader's-seat / disambiguation
- **Before:** summary `Find all usages gr`; details "Lists every call site…" /
  "Works on methods, classes, vars"; config `nmap gr <Action>(FindUsages)`.
- **After:** add a 3rd detail line: `Overrides gr ReplaceWithRegister`
- **Why / verified:** `gr` is the **ReplaceWithRegister operator**
  (`ReplaceWithRegister.kt:22` `nmapPluginAction("gr", RWR_OPERATOR)`, powering
  `griw`/`grr`) — which the set actively teaches in `plugins.json:237` ("Paste
  over a word, keep yank griw"). `nmap gr <Action>(FindUsages)` makes `gr` fire
  immediately, so `griw` can never be entered: a reader who installs both finds
  `griw` silently broken. The twin `gi` tip *does* disclose its shadow
  ("Overrides gi insert-at-last-edit") — this one must too. `gr` is the
  conventional Neovim-LSP slot, so the mapping choice is fine; only the
  disclosure is missing.

### M7. `mappings.json` — "Inspect maps with :map / :nmap / :imap"
- **Issue:** key-density · **Rule 10** (3+ keys → name the outcome, map keys in details)
- **Before:** summary is a 3-key slashed cluster (38 chars).
- **After:**
  - summary `List your mappings with :map`
  - `:map lists maps for every mode`
  - `:nmap :imap :vmap show one mode`
  - `Check one mode when a key fails`  *(all ≤35)*
- **Why:** Rule 10 — a 3-key cluster names the outcome and maps each key in the
  details (which already do).

### M8. `windows.json` — "Resize splits Ctrl-w + / - / = / < / >"
- **Issue:** key-density · **Rule 10** (5-key chord cluster)
- **Before:** summary dumps `Ctrl-w + / - / = / < / >`.
- **After:** summary `Resize the current split` (details already map every key:
  "Ctrl-w +/- change height, Ctrl-w </> change width" / "Ctrl-w = makes all
  splits equal size").
- **Why:** Mirrors the sibling `Maximize a split's height or width` (Ctrl-w _ / |),
  which the SKILL cites as the model. Keys are all registered (valid) — only the
  summary needs de-dumping.

### M9. `cmdline.json` — "Cancel the : prompt Ctrl-c / Esc"
- **Issue:** restate-summary · **Rule 8** + padding
- **Before:** "Ctrl-c cancels the prompt" / "Esc cancels it too" — both just
  re-say the summary.
- **After:** keep summary; details →
  - `Discards the line you typed`
  - `Still saved to : history`  *(verified: a cancelled command line is kept —
    `:set digraph<Esc>` then `:<Up>` recalls it)*

### M10. `cmdline.json` — "Show command history with :history"
- **Issue:** restate-summary · **Rule 8**
- **Before:** "`:history` shows : history" (restates, reads awkwardly) / ":history / shows search history"
- **After:** keep summary; `Lists past : commands, newest last` / `:history / shows search history`
- **Why / verified:** `HistoryCommand.kt` — bare `:history` lists command history
  ascending (newest last); `:history /` shows search history.

### M11. `ideavim.json` — "Make :w use IDE Save All"
- **Issue:** restate-summary (line 1 = the bare config line) · **Rule 8**
- **Before:** details `set ideawrite=all` / ":w then fires IDE save hooks"
- **After:** `:w then saves every open file` / `Fires IDE save hooks like reformat` (config block unchanged)
- **Why / verified:** detail 1 duplicates `config.lines`. `IjFileGroup.saveFile()`:
  with `ideawrite=all`, `:w` dispatches SaveAll and routes through the IDE action
  so on-save reformat/optimize-imports fire.

### M12. `insert.json` — "Run one Normal command with Ctrl-o"
- **Issue:** restate-summary · **Rule 8**
- **Before:** "Run one Normal command" (verbatim restatement) / "Then return to Insert mode"
- **After:** keep summary; `One Normal command, back to Insert` / `e.g. Ctrl-o dd deletes the line`
- **Why / verified:** `InsertSingleCommandAction` (i_CTRL-O) — one Normal command
  then back to Insert; `Ctrl-o dd` deletes the line.

### M13. `insert.json` — "Insert digraphs with Ctrl-k"
- **Issue:** placeholder + restate · **Rule 7** (typeable example over `{placeholder}`)
- **Before:** "In Insert mode, type Ctrl-k {digraph}" / "Use :digraphs to list…"
- **After:** keep summary; `Ctrl-k a: gives ä, e' gives é` / `:digraphs lists all the codes`
- **Why / verified:** `DigraphGetFunctionTest`: `digraph_get('a:') == ä`;
  `DigraphsCommandTest` lists `e' é 233`. (`a:`/`e'` are digraph codes, not key
  separators.)

### M14. `repeat.json` — "Repeat last change with ."
- **Issue:** restate-summary + vocabulary collision · **Rule 8**
- **Before:** "`.` redoes the last edit" / "Counts work, e.g. 3. repeats 3x"
- **After:** keep summary; `Replays the last edit at the cursor` / `Counts work, e.g. 3. repeats 3x`
- **Why:** "redoes" collides with redo (`Ctrl-r` — `editing.json:436` already
  uses "redoes" for it). `.` *replays* the last change at a new spot;
  `RepeatChangeAction` confirms.

### M15. `visual.json` — "Paste over a selection p" *(press-vs-meaning blur)*
- **Issue:** the `c bar` problem · reader's-seat
- **Before:** "Select, then p drops your yank in" / "But **p** then grabs the replaced text"
- **After:** "Select, then p drops your yank in" / `Replaced text overwrites your yank`
- **Why / verified:** read cold, "p then grabs…" reads as pressing `p` again; the
  point is that the *register* now holds the replaced text.
  `PutVisualTextAfterCursorAction` uses `modifyRegister=true`; the sibling `P`
  (`modifyRegister=false`) keeps the yank.

### M16. `visual.json` — "Select a text object viw vip"
- **Issue:** two non-pair keys in summary + line-1 restate · **Rules 8, 10**
- **Before:** summary `Select a text object viw vip`; "viw a word, vip a paragraph" (restates) / "vap also grabs the blank lines" / "Swap i for a to include the edges"
- **After:** summary `Select a text object viw`; details →
  - `After v press i then w b ( {`  *(no colon separator; ≤35)*
  - `vip grabs a paragraph, vap its blanks`  *(tightened ≤35)*
  - `Swap i for a to include the edges`
- **Why:** summary carries two non-pair keys; detail 1 just re-lists them. Teach
  the mechanic (`v` + `i` + object) and name the family. *(Confidence: medium —
  shape this one with the user before wording.)*

---

## LOW — wording polish (judgment calls)

> All LOW items are real rule hits but wording-only. Those that rename a
> **summary** reset the hide preference, so weigh the churn.

### L1. `ideavim.json` — "Keep Vim mode during rename"  *(details only)*
- restate-summary · **Rule 8**. Detail 1 is the bare `set idearefactormode=keep`
  (already in the config). After: `Rename won't force Select mode` / `Stay in
  Normal mode as you rename`. Verified `IdeaSpecifics.kt:271` — "keep" maintains
  the current mode by removing the selection.

### L2. `options.json` — "Keep matches highlighted hlsearch"  *(details only)*
- restate-summary · **Rule 8**. Detail 1 restates the summary. After detail 1:
  `Every match of the search stays lit`; keep `Clear temporarily with :noh`.
  *(Siblings share this `:set X <restated>` house form — fix as a set if at all.)*

### L3. `ideavim.json` — "IDE completion with Ctrl-Space"
- noun-label · **Rule 1**. After: `Complete code with Ctrl-Space` (verb-first, 29
  chars; details unchanged). Lone noun-label among ~20 verb-first siblings.

### L4. `insert.json` — "Insert char above/below Ctrl-y/e"
- compressed/ambiguous pair · **Rule 9**. `Ctrl-y/e` drops the spaces and
  truncates the 2nd key to a bare `e` (itself a motion). The full spaced form
  overflows 35, so name the outcome: summary `Insert char above/below` (details
  already spell both keys).

### L5. `navigation.json` — "Jump list navigation Ctrl-o / Ctrl-i"
- noun-label · **Rule 1**. "Jump list navigation" is jargon. A verb-first form
  ≤35 chars (e.g. `Jump back / forward Ctrl-o / Ctrl-i`) names the outcome.
  *(Pick the exact wording with the user — the obvious "Jump back and forward …"
  is 37 chars, over the cap.)*

### L6. `options.json` — "Show matches while typing incsearch"  *(details only)*
- restate-summary · **Rule 8**. Detail 1 restates the summary. After detail 1:
  `Jumps to the match as you type`; keep `Makes /pattern navigation feel
  instant`. Same house-form caveat as L2.

### L7. `options.json` — "Wrap searches at EOF with wrapscan"
- abbreviation · **Rule** (spell out abbreviations). `EOF` is unspelled. After:
  `Wrap search at file end wrapscan` (32 chars; keeps the option name).

### L8. `repeat.json` — "Append to a macro qA"  *(details only)*
- restate-summary · **Rule 8**. Detail 1 "qA appends to macro a" restates the
  summary. After: `Uppercase register appends to it` / `Lowercase q{reg} would
  replace it`. Verified `VimRegisterGroupBase.finishRecording:552-571` —
  uppercase register name appends, lowercase overwrites.

### L9. `navigation.json` — "Reposition the cursor line on screen" *(recent diff)*
- reader-seat-vague + over-length · **Rules 1, 7, 10**. The recent reword dropped
  the `zt zz zb` key dump (correct) but the result is 36 chars and vague. After:
  `Place cursor line top/center/bottom` (exactly 35; names the three-way outcome
  as prose; keys stay mapped in detail 1). Avoids leading with "recenter"
  because `pattern.json:95` already teaches "Recenter search results with nzz".

---

## Cascade / same-pass fixes

- **M2 + M3** share one root cause (yank sets no change marks) — fix together.
- **H4 / H5 / H6** are the three unsupported `windows.json` tips — delete in one
  batch; re-run `--check` after.
- **M4 / M5** are merges — each removes one tip; update both halves' details.
- After **H1** the `files.json` summary changes; nothing else references it.

## Suggested order

1. **HIGH** (H1–H6) — correctness, including the destructive H2 and three deletes.
2. **Duplicates/shadow** (M4, M5, M6) — structural, low blast radius.
3. **Wrong-fact mediums** (M1, M2, M3) — same correctness bar as HIGH, smaller scope.
4. **Remaining MEDIUM** wording (M7–M16).
5. **LOW** (L1–L9) — opportunistic; prefer the details-only ones (L1, L2, L6, L8)
   that don't reset hide preferences.

Each fix goes through *The loop*: edit the primary-category file → `node
scripts/generate-tips.mjs --check` → `node scripts/lint-tips.mjs` → `git status
--short`. Leave `tips/vim_tips_min.json` out of commits (CI regenerates it).
