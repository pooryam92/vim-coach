# Checking Support

Vim docs give *meaning*; IdeaVim data/source proves *support*. Be conservative
when ambiguous. Bang forms (`:e!`, `:q!`) can be valid even when only the base
command is indexed; some pattern/mapping behaviors are syntax inside supported
commands, not standalone commands.

**Before keeping a tip:** command/behavior clearly supported by IdeaVim? Summary
honest about mode/prompt/plugin requirements? Plugin-backed → tagged `plugins`?
IdeaVim-specific but not a plugin → is `ideavim` enough? Useful with no repo
context?

**Local KSP data** (first check) — generated JSON in the IdeaVim submodule. Fetch
+ focus the checkout:

```bash
git submodule update --init external/ideavim
git -C external/ideavim sparse-checkout init --cone
git -C external/ideavim sparse-checkout set \
  src/main/resources/ksp-generated \
  vim-engine/src/main/resources/ksp-generated
git submodule update --remote external/ideavim   # refresh to latest master
```

- `engine_*` (core engine): `commands`, `ex_commands`, `vimscript_functions` under
  `external/ideavim/vim-engine/src/main/resources/ksp-generated/`.
- `frontend_*` (frontend-only: `:buffer`, `:ls`, `:help`, `:read`, `:actionlist`…)
  under `external/ideavim/src/main/resources/ksp-generated/`.
- `ideavim_extensions.json` (both paths) — check before claiming a plugin exists.

**Vim docs** — reference https://vimhelp.org/, user manual
https://vimhelp.org/usr_toc.txt.html. Category → page: `editing`→editing.txt,
`navigation`→motion.txt/scroll.txt/fold.txt, `pattern`→pattern.txt,
`cmdline`→cmdline.txt, `options`→options.txt, `visual`→visual.txt,
`mappings`→map.txt, `windows`→windows.txt/tabpage.txt. Check nearby `usr_*.txt`
chapters for workflow-oriented material.

**Full IdeaVim source** — the submodule is the source of truth (don't browse
GitHub). Widen the sparse checkout when you need more than KSP JSON:

```bash
git -C external/ideavim sparse-checkout add \
  annotation-processors vimscript-info src vim-engine
```

Setup/usage docs not in the tree: [IdeaVim wiki](https://github.com/JetBrains/ideavim/wiki).
