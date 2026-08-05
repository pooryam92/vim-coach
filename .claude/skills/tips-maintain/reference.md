# Tips reference — support checks, config blocks, categories

On-demand companion to `SKILL.md`. Open the section you need:

- [Checking IdeaVim support](#checking-ideavim-support) — prove a
  command/behavior is supported before keeping a claim.
- [Config tips — what's safe to ship](#config-tips--whats-safe-to-ship) —
  authoring or reviewing a tip's `config` block.
- [Adding or changing a category](#adding-or-changing-a-category) — the coupled
  code+docs change.

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

**Option-backed `config`:** check the value isn't already the default
(`IjOptions.kt` / option definitions in the submodule) — `set
ideavimsupport=dialog` was once proposed, but `dialog` *is* the default, so the
button would ship a no-op.

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

The submodule pin is **updated on demand, never automatically** — `branch =
master` in `.gitmodules` only names the branch `--remote` fast-forwards to; it
does not float. Refresh it when mining a new IdeaVim release, then leave it.

After a refresh `git status` shows `M external/ideavim` (the recorded commit
moved). **That is expected and is not one of your intended files** — no
workflow checks out submodules and no Gradle script reads the path, so the pin
affects nothing but this local checkout. Leave it out of a tips commit; discard
it with `git checkout -- external/ideavim` if you'd rather not carry it.

**Is the key actually released?** A binding present in the submodule may be
newer than the reader's plugin — `master` runs ahead of the marketplace build.
Confirm before teaching a recently added key:

```bash
git -C external/ideavim log -1 --format=%h -S 'keys = ["zd"]' -- .  # commit that added it
git -C external/ideavim tag --contains <commit> | head -1           # first release; empty = unreleased
```

Cross-check `CHANGES.md`, the clearer signal when mining a release: find the
feature's `VIM-` issue and read its heading **on `master`**. Under `## To Be
Released` → **do not write the tip**; under `## X.Y.Z` → fine. Every release
carries a tag (`2.45.2`, `2.42.0-eap.1`), so an empty `--contains` does mean
unreleased.

Two quirks make the signals disagree — trust `--contains`, confirm on master:

- **A release tag's own changelog is empty** — at tag `2.43.0`, `## 2.43.0` is a
  bare heading with everything it shipped still under `## To Be Released` below
  it. `git show <tag>:CHANGES.md` is *not* a release gate.
- **Intermediate versions get rolled up** — `2.42.x`–`2.44.x` shipped as tags but
  have no heading on master; their features sit under the next stable heading
  (`## 2.45.0`). Released either way, so it never blocks a tip.

A **changed default** needs the same gate and is easier to miss — read the file as
the *released* tag has it, not just `master`:

```bash
git -C external/ideavim show 2.42.0-eap.1:<path/to/Ext.kt> | grep parseKeys
```

This is how the `multiple-cursors` tip came to teach `<C-n>` while every shipped
build still bound `<A-n>` (VIM-2178, unreleased). When a tip's keys ride on an
unreleased default, pin them in its `config` via the plugin's `<Plug>` targets —
stable across versions, so the taught key holds on both builds.

**Vim docs** (for *meaning*, not support): https://vimhelp.org/, user manual
https://vimhelp.org/usr_toc.txt.html. Category → page: `editing`→editing.txt,
`navigation`→motion.txt/scroll.txt/fold.txt, `pattern`→pattern.txt,
`cmdline`→cmdline.txt, `options`→options.txt, `visual`→visual.txt,
`mappings`→map.txt, `windows`→windows.txt/tabpage.txt. Setup/usage not in the
tree: [IdeaVim wiki](https://github.com/JetBrains/ideavim/wiki).

## Config tips — what's safe to ship

### The `config` field

`{ "name": "<button label>", "lines": ["<rc line>", …] }`. `name` is the button
label verbatim — set it **only when it's a meaningful label**; omit it and the
button reads a generic `Apply`, which is fine. The legacy array form
(`"config": ["<line>", …]`) is still accepted — don't convert it to the object
form just to add a `name`; a labelless `Apply` is not a defect to clean up.

```json
{
  "category": ["plugins", "editing"],
  "summary": "Add surroundings ysiw)",
  "details": ["ys, then a motion, then a pair", "ys$\" quotes to end of line"],
  "config": { "name": "Install vim-surround", "lines": ["Plug 'tpope/vim-surround'"] }
}
```

The **Add to .ideavimrc** button appends the whole `lines` block verbatim, in
order, at the end of the file. It never reorders, and it skips re-adding only an
*identical* contiguous block — it does **not** detect per-key or per-option
clashes. So a block is shippable only when both hold:

1. **Self-contained & order-independent** — it stands alone and works wherever it
   lands. Multi-line is fine, and a later line *may* depend on an earlier line in
   the **same** block (they ship together); it just can't depend on anything
   outside the block.
2. **Sets no shared state others rely on** — nothing that silently changes the
   meaning of the user's other config, or that their config overrides.

Lines that satisfy both — the working examples:

- **Install a plugin** — one `Plug '<github-alias>'` line. Primary category
  `plugins` + a functional secondary. Aliases:
  `external/ideavim/doc/IdeaVim Plugins.md`. Use `Plug`, not the legacy
  `set <plugin>` form. If the plugin binds *no* default keys, ship its binding
  config in the **same block**: e.g. CamelCaseMotion exposes only `<Plug>`
  targets until you set `g:camelcasemotion_key`, so ship
  `Plug '<repo>'` **and** `let g:<plugin>_key = '<prefix>'` together. The `g:` var
  is plugin-private (not leader-style shared state), and IdeaVim inits extensions
  only after the whole rc is sourced, so the two lines are order-independent. But
  it now *claims a key family*, so collision-check the prefix like an action
  mapping. **CamelCaseMotion has no safe prefix and so no shippable `config`** —
  IdeaVim's own doc recommends `<leader>` (banned, see "Not shippable yet") and
  the upstream default `,` claims the built-in repeat-`f`/`t`-backwards motion. A
  tip shipping `,` was cut for exactly that; the plugin-free `[w` / `[b` motions
  cover the same ground.
- **Tune a built-in option** — e.g. `set scrolloff=5`, `hlsearch`. Primary
  `options`.
- **IDE-bridge `set`** — e.g. `set ideajoin`, `set idearefactormode=keep`.
  Primary `ideavim`.
- **IDE-bridge action mapping** — `nmap <keys> <Action>(ActionId)`. Use a
  recursive `map`/`nmap`, never `noremap` (`<Action>()` needs a recursive map).
  Primary `ideavim`; short button label, e.g. `Map errors`. This is the one
  shippable line that *claims a key*: it sets no shared state, but appending
  always wins and dedup won't catch a clash, so the button can **override a
  user's existing binding**. Pick keys by convention first: use the established
  Vim/Neovim binding for the action (the LSP-style `gd`/`gr`/`gi` family) even
  when it shadows a built-in or an already-taught key — and disclose the shadow
  in a detail line (`Overrides built-in gi insert spot`). Only when no
  convention exists, fall back to an idiomatic free slot (the `]e`/`[e`
  bracket-pair family, an unused `g`-prefix). Collision-check either way, so you
  know what to disclose.

`lines` are **enable**/action lines only, verbatim — never usage mappings
(`ysiw)` is usage, not config).

### Not shippable yet

These look complete but fail a test above, so the button would misfire. Don't
author them until the blocker is fixed.

- **Leader-dependent mappings** (`<leader>…`, or maps relying on a custom
  `mapleader`) — fail test 2. The mapping needs `let mapleader` too, which is
  shared state: appending it can clobber the user's leader, and the button can't
  guarantee leader is set first. Revisit once the snippet can establish or detect
  leader safely.
- **Plugins that need a separate Marketplace IDE plugin** — fail test 1: the
  button appends only the config line, so a `config` that looks complete would
  silently do nothing. EasyMotion (needs IdeaVim-EasyMotion + AceJump) and
  which-key (needs the Which-Key IDE plugin) are deferred — see
  `docs/discover/config-tips-roadmap.md`. A `Plug`/`set` line is only shippable
  when IdeaVim emulates the plugin itself (surround, commentary, sneak, NERDTree,
  argtextobj, multiple-cursors…). The "Setup" block in
  `external/ideavim/doc/IdeaVim Plugins.md` reveals which need an extra install.
  `multiple-cursors` is shippable, but **not on its default keys**: VIM-2178
  (which switches the defaults to upstream's `<C-n>` family) is on `master` and
  unreleased, so every shipped build still binds `<A-n>`. Its tip therefore pins
  `<C-n>` itself with `nmap`/`xmap <Plug>NextWholeOccurrence` — stable on both
  builds. Drop the pin only once VIM-2178 appears under a released `## X.Y.Z`
  changelog heading.

## Adding or changing a category

The 14 current categories and picking rules are in `SKILL.md`. Coupled across
code + docs — update together:

1. `tips/categories/<name>.json` — adding a category = a new file (its name is the
   category); removing one = migrate or delete its tips first.
2. The category list + picking rules in `SKILL.md`.
3. `docs/discover/config-tips-roadmap.md` if it affects the config roadmap.

Ordering needs no change — categories sort alphabetically automatically. Then run
`node scripts/generate-tips.mjs` to confirm it validates.
