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
