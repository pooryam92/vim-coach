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
- `change`
  Primary: `change.txt`, `usr_04.txt`
  Nearby: `undo.txt`, `repeat.txt`, `visual.txt`, `usr_12.txt`
- `undo`
  Primary: `undo.txt`, `usr_32.txt`
  Nearby: `change.txt`, `insert.txt`, `repeat.txt`
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
- If a tip only works in a specific mode or prompt context, make that explicit in the summary or first detail line.
- Keep tip lines short; target a maximum of about 40 characters per summary/detail line when practical.
- Do not claim support that was not verified.
- While working on a section, also scan the related user-facing Vim manual chapters and pull in relevant tips that belong in the same visible category.
- When a section feels thin, scan more of vimhelp.org around the nearby reference and user-manual pages instead of stopping at the first matching page.
- Always do a final pass from the user's perspective:
  check whether the tip is easy to find, easy to understand, and not misleading in how a user would actually think about the command.

## Current state

- [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json) currently has `editing`, `motion`, `scroll`, `options`, `insert`, `change`, `undo`, `repeat`, `visual`, `cmdline`, `pattern`, `map`, `windows`, `tabpage`, and `fold` sections.
- Current totals: `193` unique tips, `88` reused from v1 by exact summary match, `105` new.
- Total counts should always mean unique tip entries only.
- Current category totals: `21` `editing`, `27` `motion`, `9` `scroll`, `13` `options`, `13` `insert`, `18` `change`, `3` `undo`, `7` `repeat`, `15` `visual`, `14` `cmdline`, `24` `pattern`, `8` `map`, `8` `windows`, `6` `tabpage`, `7` `fold`.
- Category totals should use the primary category only so multi-tagged tips are not double-counted in the overall total.
- Exact-summary reused/new counts are only a rough migration snapshot.
  Once a v2 tip is rewritten for clarity, the wording may no longer match v1 exactly even if the concept was reused.
- Current rejected examples from runtime contradiction:
  `Ctrl-g`, `g Ctrl-g`, `:file {name}`.
- Some commands depend on editor state, not just command support.
  Example: horizontal scroll commands like `zh`, `zl`, `zH`, `zL`, `ze`, and `zs` may appear to do nothing when soft-wrap is enabled or horizontal scrolling is not relevant in the current editor view.
- The user-manual pass already surfaced useful additions that a reference-only pass did not prioritize:
  jump return/navigation and wrap-aware motions.
- For `cmdline`, explicit `C`-mode editing and history keys are strong support signals.
  Examples: `Ctrl-b`, `Ctrl-e`, `Ctrl-w`, `Ctrl-u`, `Ctrl-r`, `Ctrl-p`, `Ctrl-n`, `<Up>`, `<Down>`, `<PageUp>`, `<PageDown>`, `<S-Left>`, and `<S-Right>`.
- For mode-specific tips, do not assume the category label is visible to the user.
  If a command only makes sense in `:` or `/`, say that in the tip itself.
- Be more conservative with `cmdline` completion and command-line-window tips.
  Plain `<Tab>` completion and `q:` are documented in Vim, but they did not have a clean enough generated-support signal in this pass to keep as IdeaVim tips yet.
- For `pattern`, many of the useful regex features are syntax inside supported commands, not standalone commands.
  Examples: `\V`, `\v`, `\c`, `\C`, `\<`, and `\>` belong to `/`, `?`, and `:s`, so they will not appear as separate entries in the generated command maps.
- For `pattern`, the strongest support anchors are the surrounding commands:
  `/`, `?`, `n`, `N`, `*`, `#`, `g*`, `g#`, `:s`, `&`, `:noh`, `:g`, and `:v`.
- For `pattern`, practical workflows matter as much as raw regex features.
  High-value examples were `nzz`, `cgn`, and `:.,$s`, which are easier for users to apply than abstract pattern syntax alone.
- For `pattern`, summaries must describe the user outcome, not the Vim concept label.
  Tips like `:g`, `:v`, `gc`, and `&` were much clearer once the summary said what they do instead of naming the underlying command family.
- For `map`, the strongest support signal comes from `map.txt`, `usr_40.txt`, and the generated Ex command map together.
  The mapping commands themselves appear in the generated list, but many useful pieces like `<Leader>`, `<silent>`, and `<Nop>` are mapping arguments, not standalone commands.
- For `map`, keep the section focused on practical `.ideavimrc` workflows.
  High-value topics were inspecting maps, choosing the right mode-specific map, preferring `noremap`, using `<Leader>`, unmapping keys, and disabling risky keys with `<Nop>`.
- For IdeaVim-specific action mappings, use documented behavior instead of guessing from generic Vim help.
  The project README and existing IdeaVim tips both point to `map <Leader>... <Action>(...)`, and that form should stay `map`, not `noremap`.
- For `map`, be explicit about where the user uses the syntax.
  Direct `:map` and `:unmap` tips can use Ex-command wording, but mapping-definition tips should say they belong in `~/.ideavimrc`.
- For config-file tips, keep the syntax consistent with config-file usage.
  In `.ideavimrc`, write `nmap`, `nnoremap`, and similar forms without a leading `:`.
- For `windows`, the strongest support signal comes from `windows.txt`, `usr_08.txt`, and the generated command/ex-command maps together.
  The safest first batch is the core split, close, only, and window-navigation commands.
- Be conservative with advanced window layout commands until they are clearly exposed.
  `:new`, `:vnew`, `:wincmd`, resize commands, and window-moving commands were not strong enough in the local support maps for this pass.
- Prefer the key forms that are actually exposed in the generated maps.
  Example: `Ctrl-w c` had a clear support signal for closing the current split; `Ctrl-w q` did not.
- Current unresolved `windows` candidates are still:
  `Ctrl-w t / b`, `Ctrl-w =`, `Ctrl-w _ / |`, and `Ctrl-w + - < >`.
  They are useful in Vim, but they did not show up cleanly in the local generated support maps yet.
- For `tabpage`, the strongest support signal comes from `tabpage.txt` plus the generated tab commands.
  `gt`, `gT`, `:tabn`, `:tabp`, `:tabclose`, `:tabonly`, and `:tabmove` all had clear local support.
- Be careful with tab commands that are present in Vim docs but missing from the local generated Ex-command map.
  `:tabnew` and `:tabs` are still useful candidates, but they did not have a clean enough support signal in this pass.
- For `fold`, the strongest support signal came from `fold.txt`, `usr_28.txt`, and the generated command map together.
  The local maps clearly expose `zf`, `zo`, `zc`, `za`, `zO`, `zC`, `zA`, `zj`, `zk`, `zM`, `zR`, `zr`, and `zm`.
- Be conservative with fold options until they are documented more clearly in IdeaVim.
  Vim's `foldmethod`, `foldlevel`, and related settings are useful in Vim, but they did not have a strong enough local IdeaVim support signal for this pass.
- For fold tips, mention IDE folding dependence when it affects behavior.
  Manual fold commands are useful, but whether they visibly work still depends on the current editor and IDE folding support.
- For `options`, the strongest support signal comes from the official IdeaVim `set` docs plus Vim's own `options.txt`.
  That is more reliable than looking for individual option names in the generated command maps.
- Keep `options` practical and high-value.
  Prefer commonly adjusted behaviors like search, numbers, wrapping, clipboard, and generic `:set` workflows over long lists of niche settings.
- Be careful with options that overlap IDE-controlled editor behavior.
  Example: `wrap` can affect line wrapping, but JetBrains soft-wrap settings may still shape what the user actually sees.
- `scroll` and `options` can overlap in a user-meaningful way.
  Tips like `scrolloff` and `scroll` should be discoverable from both categories.
- `change` overlaps naturally with `motion`, `insert`, and `repeat`.
  Tips like `cw`, `s`, `cc`, and `.` are easier to find when they can show up from more than one user path.
- `undo` stays small and works best as a focused category plus a few cross-tagged workflow tips.
  Tips like `u`, `Ctrl-r`, `:undo`, and `Ctrl-g u` can stay primary `undo`, while nearby workflows like last-edit jumps can also point there.
- `repeat` is partly its own category and partly a cross-cutting behavior.
  The strongest repeat tips are `.`, macro record/playback, `Ctrl-a`/`Ctrl-@`, `@:`, and a few repeat-style commands that naturally belong to other sections.
- When a repeat tip already has a strong home in another section, prefer cross-tagging over moving it.
  Good examples are `;` / `,` under `motion`, `n` / `N` under `pattern`, and `&` under `pattern` or `cmdline`.
- Be careful with packed repeat tips.
  `{count}@{reg}` and `:normal @r` read better as two tips than one overloaded macro tip.
- For `repeat`, primary-category counts should only include tips whose main user intent is repetition itself.
  Cross-tagged repeat behaviors in `motion`, `pattern`, or `cmdline` should not inflate the primary `repeat` total.
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

1. Start by reading the Vim reference page for the target section.
2. Do a real sweep of the related user-facing Vim manual chapters as well.
3. Build a candidate list of commands, keys, and workflows.
4. Add any relevant user-facing workflows or explanations from those chapters to the same category pass.
5. Filter candidates through the local IdeaVim support sources.
6. Reuse matching v1 tips where it makes sense.
7. Write supported tips into [vim_tips_v2.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips_v2.json).
8. Mark reused v1 source tips with `migrated` in [tips/vim_tips.json](/home/poorya/IdeaProjects/vim-coach/tips/vim_tips.json).
9. Validate JSON with `jq . tips/vim_tips_v2.json` and `jq . tips/vim_tips.json` when v1 changes.
10. Save newly learned support edge cases and user-facing content lessons back into this file.

Do not call a category done until both passes were done:
- the reference-doc pass
- the user-manual (`usr_*.txt`) pass

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
