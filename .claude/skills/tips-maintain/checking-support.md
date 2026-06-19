# Checking IdeaVim support

Open when you need to **prove a command/behavior is supported by IdeaVim** before
keeping a claim. Everyday authoring is in `SKILL.md`.

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
