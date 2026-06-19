# tips-maintain — reference

Open this when you need to: **prove a command is supported by IdeaVim**, study
**worked before→after examples**, or **add a new category**. Everyday authoring
(format, wording, categories, the loop) is in `SKILL.md`.

## Checking IdeaVim support

These tips target **IdeaVim**, where keys are often remapped or collapsed onto IDE
actions — many keys bind to the *same* IDE action, and upstream-Vim semantics
often don't carry over. Vim docs give *meaning*; the IdeaVim submodule proves
*support*. Be conservative when ambiguous: bang forms (`:e!`, `:q!`) can be valid
even when only the base command is indexed; some pattern/mapping behaviors are
syntax inside a supported command, not standalone commands.

**Before keeping a tip:** is the command/behavior clearly supported by IdeaVim? Is
the summary honest about mode/prompt/plugin requirements? Plugin-backed → tagged
`plugins`? IdeaVim-specific but not a plugin → is `ideavim` enough?

The submodule is checked out at `external/ideavim/`. Its KSP-generated JSON lists
the real commands/options/functions:

- **engine** (`commands`, `ex_commands`, `vimscript_functions`):
  `external/ideavim/vim-engine/src/main/resources/ksp-generated/`
- **frontend-only** (`:buffer`, `:ls`, `:help`, `:read`, `:actionlist`…):
  `external/ideavim/src/main/resources/ksp-generated/`
- **plugins** — `ideavim_extensions.json` (both paths). Check before claiming a
  plugin exists.

If the submodule needs refreshing or a wider checkout:

```bash
git submodule update --init external/ideavim
git -C external/ideavim sparse-checkout init --cone
git -C external/ideavim sparse-checkout set \
  src/main/resources/ksp-generated \
  vim-engine/src/main/resources/ksp-generated
git submodule update --remote external/ideavim   # refresh to latest master
# Need more than KSP JSON (an action's @CommandOrMotion keys, an option in
# IjOptions.kt)? widen the checkout:
git -C external/ideavim sparse-checkout add \
  annotation-processors vimscript-info src vim-engine
```

**Vim docs** (for *meaning*, not support): https://vimhelp.org/, user manual
https://vimhelp.org/usr_toc.txt.html. Category → page: `editing`→editing.txt,
`navigation`→motion.txt/scroll.txt/fold.txt, `pattern`→pattern.txt,
`cmdline`→cmdline.txt, `options`→options.txt, `visual`→visual.txt,
`mappings`→map.txt, `windows`→windows.txt/tabpage.txt. Setup/usage not in the
tree: [IdeaVim wiki](https://github.com/JetBrains/ideavim/wiki).

## Worked examples (before → after)

Each shows the rule from `SKILL.md` in action. ❌ before → ✅ after.

**Name the family in prose, never dump symbols.**
❌ `Works for ) ] } " ' too` → ✅ `Any bracket or quote works too`
A row of bare symbols reads like keyboard mashing; name the family, keep ≤1 example.

**Don't restate the summary in detail line 1.**
summary `Edit the next () pair cin)` —
❌ detail `cin) seeks ahead to the next pair` → ✅ `Works even with the cursor outside it`
The first detail is the most-read line; spend it on the *value*, not a rephrase.

**Explain the effect, not the anatomy — a tip must read cold.**
summary `Keep only matching lines :v//d` —
❌ details `v = non-matching lines, d = delete` (token-naming still assumes you
know `:g`/global commands) → ✅ `:v/foo/d deletes lines lacking foo` +
`What's left: only the foo lines`. Anatomy *looks* educational but recycles the
jargon; a typeable command + plain-words outcome teaches a reader who's never
seen the concept.

**One worked example beats a meta description + letter dump.**
❌ `cr + a letter picks the case / c camel, s snake, m Mixed…` →
✅ `crc turns foo_bar into fooBar`
A single `input → output` example teaches the whole thing.

**Split a dense tip into single-concept tips** (display order is random, so a tip
can't lean on another).
❌ one tip cramming next + previous → ✅ `Edit the next () pair cin)` and
`Edit the previous () pair cil)` as two tips. Only split when each earns a
*distinct* summary (the generator rejects duplicates).

**Cut lines that don't earn their place.**
❌ trailing `cr + a letter sets the style` on every case tip → ✅ (removed).

**In a slashed pair, vary one axis — keep the operator fixed.**
❌ `Act on an argument cia / daa` → ✅ `Act on an argument cia / caa`
`cia / daa` mixes two operators *and* two text objects; hold the operator steady
and let the slash show the text-object difference (`i` vs `a`). Name the other
operators in a detail: `Pairs with d, y and other operators`.

**Operator + motion tips — lead concrete, generalize the open axis.**
Summary leads with one concrete, runnable combo, never a bare operator or a
`{motion}` placeholder: ❌ `Swap two regions cx` → ✅ `Swap two regions cxiw`.
Last detail generalizes the open axis in prose with ≤1 example — open axis =
motion → `Takes any motion, like cxx`; open axis = operator → `Pairs with any
operator — c, d, y`. Antonym pairs (`Indent/outdent a block >ip / <ip`) and
fixed-motion delete/change pairs (`Change/delete a word cw / dw`) may keep two
operators in the title.

**Verify the claim against IdeaVim source, not Vim lore.**
❌ `gd jumps to definition, gD jumps to declaration` →
✅ confirm in `external/ideavim` first — both keys bind to the *same*
`GotoDeclarationAction`, so they're identical here.

**Search existing tips before adding — kill semantic duplicates.**
❌ adding `Repeat search and place line n then zz` when `Recenter search results
with nzz` already teaches it → ✅ grep the concept first; drop or merge. The
generator only rejects *identical* summaries, so the same idea under different
wording slips through — search both the keys *and* the behavior.

## Adding or changing a category

Coupled across code + docs — update together:

1. `tips/categories/<name>.json` — adding a category = a new file (its name is the
   category); removing one = migrate or delete its tips first.
2. The category table + picking rules in `SKILL.md` (and the 14-count).
3. `docs/discover/config-tips-roadmap.md` if it affects the config roadmap.

Ordering needs no change — categories sort alphabetically automatically. Then run
`node scripts/generate-tips.mjs` to confirm it validates.
