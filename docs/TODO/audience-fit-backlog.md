# Audience-fit backlog

From the 2026-10-02 audience review. Target personas, in priority order:

1. **Plateaued IdeaVim user** (primary) — knows `hjkl`/`dd`/`ciw`, stopped
   learning. Default rotation is tuned for them.
2. **Vim/Neovim veteran new to JetBrains** (secondary) — needs IdeaVim-specific
   depth, not more vanilla Vim.
3. **Vim newcomer on IdeaVim** (tertiary) — tips complement a tutorial, not
   replace it.

Option A (retuning the `advanced` flag for persona 1) lives in
[tagging.md](../../.claude/skills/tips-maintain/tagging.md).

## B — "Skip basics" setting

About 25 core tips are fundamentals the primary persona already knows
(`i/a`, `I/A`, `o/O`, `Esc`, `dd`, `x`, `yy`, `p/P`, `u`, `w/b/e`, `0/^`, `$`,
`gg/G`, `cc`, `cw`, `r`, `Ctrl-d/u`, `Ctrl-f/b`, `:q`, `ZZ`, `/`). They dilute
random rotation (~1 in 8 impressions) and read as "I know this"; today the only
escape is excluding them one by one.

- [ ] Add a `basic` tip flag (generator validation + model), mirroring `advanced`.
- [ ] Settings toggle **Skip basic tips**, off by default so newcomers keep them.
- [ ] Decide whether `basic` and `advanced` become one `level` field instead of
      two booleans before shipping either.
- [ ] Tag the fundamentals; record the rule in tagging.md.

Open question: a first-run "How well do you know Vim?" choice could set both
toggles at once instead of burying them in settings.

## C — IdeaVim-specific depth for veterans

Only one advanced tip is IdeaVim-specific (`trackactionids`); the advanced pool
is otherwise deeper vanilla Vim, which veterans already know. Candidates with no
tip at all (grep before authoring — some may fail the skill's 4-axis score or
release check):

- [ ] `idearefactormode` — which mode a refactoring template leaves you in
- [ ] `ideamarks` — Vim marks synced with IDE bookmarks
- [ ] which-key plugin
- [ ] EasyMotion / AceJump bridge
- [ ] `<Action>(...)` mapping as its own tip (today only inside `config` lines)

Run through the tips-maintain flow: ranked shortlist → go-ahead → author.
