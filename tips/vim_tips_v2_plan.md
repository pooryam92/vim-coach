# vim_tips_v2 category plan

## Goal

Keep the visible tag list close to Vim's docs, but only expose parts that are clear and support-safe in IdeaVim.

Users see one list of tags and turn them on or off to control which tips they get. The visible tags are the only tags.

## Visible categories

- `editing`
- `motion`
- `scroll`
- `insert`
- `change`
- `undo`
- `repeat`
- `visual`
- `cmdline`
- `options`
- `pattern`
- `map`
- `windows`
- `tabpage`
- `fold`
- `ideavim`

## Excluded sections

Do not expose these as visible tags unless IdeaVim support becomes clearly worth it:

- `recover`
- `various`
- `tagsrch`
- `spell`
- `diff`
- `autocmd`
- `channel`
- `eval`
- `functions`

## Sources

Primary local support sources:

- [tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json)
- [engine_commands.json](/home/poorya/IdeaProjects/vim-coach/tips/ksp-generated/engine_commands.json)
- [engine_ex_commands.json](/home/poorya/IdeaProjects/vim-coach/tips/ksp-generated/engine_ex_commands.json)
- [frontend_commands.json](/home/poorya/IdeaProjects/vim-coach/tips/ksp-generated-frontend/frontend_commands.json)
- [frontend_ex_commands.json](/home/poorya/IdeaProjects/vim-coach/tips/ksp-generated-frontend/frontend_ex_commands.json)

Reference docs:

- https://vimhelp.org/
- https://github.com/JetBrains/ideavim
- https://github.com/JetBrains/ideavim/wiki

## Support rules

- Use Vim docs as the content source, not as proof of IdeaVim support.
- Prefer support evidence in this order:
  runtime behavior, `frontend_*`, `engine_*`, historical docs.
- If support is still unclear after checking generated data and docs, check the IdeaVim source code before keeping the tip.
- Do not keep a tip just because `engine_*` exposes a command or action.
- Aliases may show up only in `frontend_ex_commands.json`.
- Bang forms like `:e!` and `:q!` may still be valid even when only the base command is indexed.
- Keep `vim_tips_v2.json` as the single working v2 file.

## Migration rules

- Reuse v1 tips when they still fit.
- Add `migrated` only to the reused source tip in [tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json).
- Do not add `migrated` to [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json).

## Writing rules

- Default to one visible category per tip.
- Add a second visible category only when it clearly improves filtering.
- Keep summaries command-first and concrete.
- Keep details short and factual.
- Do not claim support that was not verified.

## Current state

- [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json) is currently an `editing`-only dataset.
- Current rejected examples from runtime contradiction:
  `Ctrl-g`, `g Ctrl-g`, `:file {name}`.

## Process

1. Start from the Vim help page for the target section.
2. Build a candidate list of commands, keys, and workflows.
3. Filter candidates through the local IdeaVim support sources.
4. Reuse matching v1 tips where it makes sense.
5. Write supported tips into [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json).
6. Mark reused v1 source tips with `migrated` in [tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json).
7. Validate JSON with `jq . tips/vim_tips_v2.json` and `jq . tips/vim_tips.json` when v1 changes.
8. Save newly learned support edge cases back into this file.

## Tip template

```json
{
  "category": [
    "<section>"
  ],
  "summary": "Command-first summary",
  "details": [
    "What it does",
    "Why or when to use it"
  ]
}
```
