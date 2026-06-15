# Checking Support & References

How to confirm a command or behavior is actually supported before keeping a tip,
plus the reference sources to draw content from. For the pass/fail checklist, see
the **Support Checklist** in [workflow.md](workflow.md).

Use Vim docs for meaning and teaching value. Use IdeaVim docs and source to
confirm a command or behavior is actually supported.

## Local Support Data

Use the generated KSP JSON files in the IdeaVim submodule as the first local
support check for whether a command, Ex command, Vimscript function, or IdeaVim
extension exists.

To fetch the submodule after cloning this repo:

```bash
git submodule update --init external/ideavim
```

To keep the local submodule checkout focused on generated KSP files:

```bash
git -C external/ideavim sparse-checkout init --cone
git -C external/ideavim sparse-checkout set \
  src/main/resources/ksp-generated \
  vim-engine/src/main/resources/ksp-generated
```

To refresh it to the latest tracked IdeaVim `master` commit:

```bash
git submodule update --remote external/ideavim
```

- Engine command data:
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/engine_commands.json`
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/engine_ex_commands.json`
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/engine_vimscript_functions.json`
- Frontend command data:
  - `external/ideavim/src/main/resources/ksp-generated/frontend_commands.json`
  - `external/ideavim/src/main/resources/ksp-generated/frontend_ex_commands.json`
  - `external/ideavim/src/main/resources/ksp-generated/frontend_vimscript_functions.json`
- Extension data:
  - `external/ideavim/vim-engine/src/main/resources/ksp-generated/ideavim_extensions.json`
  - `external/ideavim/src/main/resources/ksp-generated/ideavim_extensions.json`

Use them like this:

- Check `engine_*` files for core Vim engine support.
- Check `frontend_*` files for frontend-only commands such as `:buffer`,
  `:buffers`, `:files`, `:ls`, `:help`, `:read`, and `:actionlist`.
- Check the extension JSON files before claiming a plugin-backed IdeaVim
  extension exists.
- If the KSP data confirms existence but not user-facing behavior, use Vim docs
  for semantics and IdeaVim docs/source for plugin setup or runtime details.

## Vim Docs

- Reference manual: https://vimhelp.org/
- User manual table of contents: https://vimhelp.org/usr_toc.txt.html

Useful starting pages by category:

- `editing` -> `editing.txt`
- `motion` -> `motion.txt`
- `scroll` -> `scroll.txt`
- `insert` -> `insert.txt`
- `change` -> `change.txt`
- `undo` -> `undo.txt`
- `repeat` -> `repeat.txt`
- `visual` -> `visual.txt`
- `cmdline` -> `cmdline.txt`
- `options` -> `options.txt`
- `pattern` -> `pattern.txt`
- `map` -> `map.txt`
- `windows` -> `windows.txt`
- `tabpage` -> `tabpage.txt`
- `fold` -> `fold.txt`

Also check nearby `usr_*.txt` chapters when a section feels thin. The user
manual often gives better beginner and workflow-oriented material than the
reference pages alone.

## IdeaVim Source Sources

The `external/ideavim` submodule is the source of truth — read it directly
instead of browsing GitHub. The recommended sparse checkout (see **Local Support
Data**) only includes the `ksp-generated` JSON, which is enough for most support
checks. To read the full source, widen the sparse-checkout set, for example:

```bash
git -C external/ideavim sparse-checkout add \
  annotation-processors vimscript-info src vim-engine
```

- Generated support data: `external/ideavim/.../ksp-generated/`
- Annotation processors: `external/ideavim/annotation-processors`
- Vimscript info: `external/ideavim/vimscript-info`
- Frontend source: `external/ideavim/src`
- Engine source: `external/ideavim/vim-engine`

For setup and usage docs not in the source tree, see the
[IdeaVim wiki](https://github.com/JetBrains/ideavim/wiki).
