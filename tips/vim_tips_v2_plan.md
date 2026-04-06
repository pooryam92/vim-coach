# vim_tips_v2 category plan

## Goal

Keep the visible tag list close to Vim's docs, but only expose parts that are clear and support-safe in IdeaVim.

Users see one list of tags and turn them on or off to control which tips they get. The visible tags are the only tags.

## Visible categories

- `editing`
- `motion`
- `scroll`
- `insert`
- `change / undo`
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
- https://vimhelp.org/usr_toc.txt.html
- https://github.com/JetBrains/ideavim
- https://github.com/JetBrains/ideavim/wiki

When relevant, also check the matching user-manual chapters under `usr_*.txt`.
Those pages are often more user-facing than the reference manual and can surface better beginner/intermediate tips.

## Vim help sitemap

Use this as the starting map when hunting for tips.
Start with the primary pages, then scan the nearby pages if the section still feels thin.

- `editing`
  Primary: `editing.txt`, `usr_07.txt`
  Nearby: `windows.txt`, `cmdline.txt`, `options.txt`
- `motion`
  Primary: `motion.txt`, `usr_03.txt`
  Nearby: `scroll.txt`, `pattern.txt`, `usr_29.txt`
- `scroll`
  Primary: `scroll.txt`, `usr_03.txt`
  Nearby: `motion.txt`, `options.txt`, `usr_28.txt`
- `insert`
  Primary: `insert.txt`, `usr_24.txt`
  Nearby: `change.txt`, `visual.txt`, `usr_04.txt`
- `change / undo`
  Primary: `change.txt`, `undo.txt`, `usr_04.txt`, `usr_32.txt`
  Nearby: `repeat.txt`, `visual.txt`, `usr_12.txt`
- `repeat`
  Primary: `repeat.txt`, `usr_10.txt`
  Nearby: `change.txt`, `visual.txt`, `usr_12.txt`
- `visual`
  Primary: `visual.txt`, `usr_10.txt`
  Nearby: `motion.txt`, `change.txt`, `usr_26.txt`
- `cmdline`
  Primary: `cmdline.txt`, `usr_20.txt`
  Nearby: `editing.txt`, `pattern.txt`, `map.txt`
- `options`
  Primary: `options.txt`, `usr_05.txt`
  Nearby: `scroll.txt`, `pattern.txt`, `usr_25.txt`
- `pattern`
  Primary: `pattern.txt`, `usr_27.txt`
  Nearby: `change.txt`, `cmdline.txt`, `usr_12.txt`
- `map`
  Primary: `map.txt`, `usr_40.txt`
  Nearby: `cmdline.txt`, `options.txt`, `usr_41.txt`
- `windows`
  Primary: `windows.txt`, `usr_08.txt`
  Nearby: `editing.txt`, `tabpage.txt`, `usr_09.txt`
- `tabpage`
  Primary: `tabpage.txt`
  Nearby: `windows.txt`, `usr_09.txt`
- `fold`
  Primary: `fold.txt`, `usr_28.txt`
  Nearby: `motion.txt`, `scroll.txt`, `options.txt`
- `ideavim`
  Primary: IdeaVim wiki, README, plugin pages, supported set-command docs
  Nearby: generated support maps, IdeaVim source code, IntelliJ-backed option docs

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
- Prefer reusing and adapting existing tips over rewriting them from scratch when the old version is still good.
- Add `migrated` only to the reused source tip in [tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json).
- Do not add `migrated` to [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json).

## Writing rules

- Keep the tips files cohesive and small enough to review comfortably.
- Avoid adding extra files or structure unless there is a clear need.
- Default to one visible category per tip.
- Multiple visible categories are allowed.
- Add a second or third visible category when it clearly improves filtering or helps users find the tip from more than one natural path.
- Keep summaries command-first and concrete.
- Keep details short and factual.
- Keep tip lines short; target a maximum of about 40 characters per summary/detail line when practical.
- Do not claim support that was not verified.
- While working on a section, also scan the related user-facing Vim manual chapters and pull in relevant tips that belong in the same visible category.
- When a section feels thin, scan more of vimhelp.org around the nearby reference and user-manual pages instead of stopping at the first matching page.
- Always do a final pass from the user's perspective:
  check whether the tip is easy to find, easy to understand, and not misleading in how a user would actually think about the command.

## Current state

- [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json) currently has `editing`, `motion`, `scroll`, `options`, `insert`, and `change / undo` sections.
- Current totals: `94` unique tips, `42` reused from v1 by exact summary match, `52` new.
- Total counts should always mean unique tip entries only.
- Current category totals: `21` `editing`, `27` `motion`, `9` `scroll`, `2` `options`, `14` `insert`, `21` `change / undo`.
- Category totals should use the primary category only so multi-tagged tips are not double-counted in the overall total.
- Exact-summary reused/new counts are only a rough migration snapshot.
  Once a v2 tip is rewritten for clarity, the wording may no longer match v1 exactly even if the concept was reused.
- Current rejected examples from runtime contradiction:
  `Ctrl-g`, `g Ctrl-g`, `:file {name}`.
- Some commands depend on editor state, not just command support.
  Example: horizontal scroll commands like `zh`, `zl`, `zH`, `zL`, `ze`, and `zs` may appear to do nothing when soft-wrap is enabled or horizontal scrolling is not relevant in the current editor view.
- The user-manual pass already surfaced useful additions that a reference-only pass did not prioritize:
  jump return/navigation and wrap-aware motions.
- `scroll` and `options` can overlap in a user-meaningful way.
  Tips like `scrolloff` and `scroll` should be discoverable from both categories.
- `change / undo` overlaps naturally with `motion`, `insert`, and `repeat`.
  Tips like `cw`, `s`, `cc`, `.`, and `u` are easier to find when they can show up from more than one user path.
- Multi-tagging works best when it matches how users look for the tip, not how Vim internally classifies it.
- Packed command-family tips often need to be split into smaller tips for usability.
  This was true for both the `z<CR> / z. / z- / z+ / z^` family and the `zh / zl / zH / zL / zs / ze` family.
- Nearby tips should not duplicate the same command unless the user intent is clearly different.
  Example: avoid two adjacent tips that both hinge on `dw` unless each teaches a distinct decision.
- If a duplicated tip is only duplicated for discoverability, keep one tip and give it multiple categories instead of repeating the same command in separate entries.
  Prefer multi-category tagging over duplicate tip entries when the content is otherwise the same.
- Prefer standard Vim wording when it is clearer and more precise.
  Example: keep terms like "first non-blank" instead of looser rephrasings like "first text".
- When reusing a tip from v1, do not accidentally drop the key behavior that made it useful.
  Example: `cc` should still mention that it enters Insert mode.
- Separate command tips from strategy/advice tips carefully.
  If two tips both teach `.`, merge them or make the second one clearly about workflow rather than restating the command.
- The 40-character line target is still not consistently enforced across older tips.
  Treat that as cleanup debt to pay down while revisiting sections.

## Process

1. Start from the Vim help page for the target section.
2. Check the related user-facing Vim manual chapters as well.
3. Build a candidate list of commands, keys, and workflows.
4. Add any relevant user-facing workflows or explanations from those chapters to the same category pass.
5. Filter candidates through the local IdeaVim support sources.
6. Reuse matching v1 tips where it makes sense.
7. Write supported tips into [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json).
8. Mark reused v1 source tips with `migrated` in [tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json).
9. Validate JSON with `jq . tips/vim_tips_v2.json` and `jq . tips/vim_tips.json` when v1 changes.
10. Save newly learned support edge cases and user-facing content lessons back into this file.

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
