# Discovery: Audience Fit

From the 2026-10-02 audience review. Target personas, in priority order:

1. **Plateaued IdeaVim user** (primary) — knows `hjkl`/`dd`/`ciw`, stopped
   learning. Default rotation is tuned for them.
2. **Vim/Neovim veteran new to JetBrains** (secondary) — needs IdeaVim-specific
   depth, not more vanilla Vim.
3. **Vim newcomer on IdeaVim** (tertiary) — tips complement a tutorial, not
   replace it.

## IdeaVim-specific depth for veterans

The advanced pool is mostly deeper vanilla Vim, which veterans already know.
2026-10-03 pass:

- [x] `ideawindowjumps` — per-split jump list (`windows.json`, advanced)
- [ ] Vim keys in the run/debug console (`ideaeditor`, on by default since
      2.47.0) — maybe; only tryable while a run is open
- [ ] CamelCaseMotion — built in, but its keys hang off `<leader>`, which
      config tips can't ship yet

Ruled out:

- `idearefactormode` — the default Select mode already replaces the name as
  you type (skill examples.md, "A config button that sets the default")
- `ideamarks` — on by default; already a detail on the `mA` tip
- which-key, EasyMotion, quick-scope — need a separate Marketplace plugin
  (config-tips-roadmap.md)
- Standalone `<Action>(…)` tip — ~10 tips already teach it through concrete
  maps, and `trackactionids` already says to paste the ID into one
- Tag stack `Ctrl-t` — covered by the `Ctrl-]` tip
- `ideaeditor+=main/chat`, keylog — unreleased
