# Config tips — what's safe to ship

Open when authoring or reviewing a tip's `config` block. Everyday format/wording
is in `SKILL.md`.

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
  `Plug 'bkad/CamelCaseMotion'` **and** `let g:camelcasemotion_key = '<prefix>'`
  together. The `g:` var is plugin-private (not leader-style shared state), and
  IdeaVim inits extensions only after the whole rc is sourced, so the two lines
  are order-independent. But it now *claims a key family*, so collision-check the
  prefix like an action mapping — the upstream default `,` is both a built-in
  motion and a common leader, so it's usually not a safe pick.
- **Tune a built-in option** — e.g. `set scrolloff=5`, `hlsearch`. Primary
  `options`.
- **IDE-bridge `set`** — e.g. `set ideajoin`, `set idearefactormode=keep`.
  Primary `ideavim`.
- **IDE-bridge action mapping** — `nmap <keys> <Action>(ActionId)`. Use a
  recursive `map`/`nmap`, never `noremap` (`<Action>()` needs a recursive map).
  Primary `ideavim`; short button label, e.g. `Map errors`. This is the one
  shippable line that *claims a key*: it sets no shared state, but appending
  always wins and dedup won't catch a clash, so the button can **override a
  user's existing binding**. Collision-check the keys against those already taught
  in the set before shipping — prefer idiomatic free slots (the `]e`/`[e`
  bracket-pair family, an unused `g`-prefix).

`lines` are **enable**/action lines only, verbatim — never usage mappings
(`ysiw)` is usage, not config).

## Not shippable yet

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
  argtextobj…). The "Setup" block in `external/ideavim/doc/IdeaVim Plugins.md`
  reveals which need an extra install.
- **multiple-cursors** — emulated, but its default keys don't bind and the
  `<Plug>` workaround maps aren't verified to work (VIM-2178); deferred until
  confirmed. (Unlike CamelCaseMotion above, where the prefix var *does* bind
  working keys.)
