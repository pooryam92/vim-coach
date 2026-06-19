# Config tips — the three additive kinds

Open when authoring or reviewing a tip's `config` block. Everyday format/wording
is in `SKILL.md`.

A `config` tip must be one of these — each line is unique + order-independent, so
the **Add to .ideavimrc** button can safely append it:

1. **Install a plugin** — one `Plug '<github-alias>'` line. Primary category
   `plugins` + a functional secondary. Aliases:
   `external/ideavim/doc/IdeaVim Plugins.md`. Use `Plug`, not the legacy
   `set <plugin>` form.
2. **Tune a built-in option** — e.g. `set scrolloff=5`, `hlsearch`. Primary
   `options`.
3. **IDE-bridge `set`** — e.g. `set ideajoin`, `set idearefactormode=keep`.
   Primary `ideavim`.

Anything that claims keys or sets shared state is *positional*, not shippable —
don't author it. `lines` are **enable** lines only, verbatim — never usage
mappings (`ysiw)` is usage, not config).

**Skip plugins that need a separate Marketplace IDE plugin** — the button only
appends the config line, so a tip whose `config` looks complete would silently do
nothing. EasyMotion (needs IdeaVim-EasyMotion + AceJump), which-key (needs the
Which-Key IDE plugin), and multiple-cursors (default keys unbound, VIM-2178) are
**deferred** — see `docs/discover/config-tips-roadmap.md`. A `Plug`/`set` line is
only shippable when IdeaVim emulates the plugin itself (surround, commentary,
sneak, NERDTree, argtextobj…). The "Setup" block in
`external/ideavim/doc/IdeaVim Plugins.md` reveals which need an extra install.
