# Plugin-tip indicator gap (discover)

Tracks the case left **out of scope** by the "create `.ideavimrc` on click" work
(see [../features/ideavimrc-button.md](../features/ideavimrc-button.md)).

## Background

The **Add to .ideavimrc** button was the only on-tip signal that a tip needs a
plugin or option — e.g. `ysiw)` only works with vim-surround installed. The
button is gated on two things:

1. **IdeaVim installed** — `plugin-ideavim.xml` (which registers `FindIdeaVimRc`)
   only loads when IdeaVim is present. IdeaVim is an **optional** dependency
   (`plugin.xml`).
2. **`.ideavimrc` exists** — previously also required the file to already exist.

Gate (2) is now closed (2026-06-19): the button shows whenever IdeaVim is
installed (`isAvailable()` keys on the `FindIdeaVimRc` service, not on a file).
When no `.ideavimrc` exists, clicking returns `Result.NoVimRc` and the user is
**guided** to create one through IdeaVim — Vim Coach never writes the file. That
closes the **has-IdeaVim, no-file** hole without coupling to IdeaVim internals.
See [ideavimrc-button.md](../features/ideavimrc-button.md). (We rejected
auto-creating the file via IdeaVim's unstable `VimRcService.findOrCreateIdeaVimRc()`.)

## The config-tip slice — CLOSED (2026-06-21)

When **IdeaVim is not installed**, the button was correctly hidden — but a
plugin/config tip still *rendered*, with **no indicator** it needs anything: the
user tried `ysiw)`, nothing happened, confusion.

This is now resolved for config tips via **Option B (suppress)**: tip *selection*
itself drops `config`-bearing tips when IdeaVim is absent, so they are never
shown rather than shown without a usable button. `TipNotifications.selectRandomTip()`
passes `includeConfigTips = ideaVimAvailable()` into `VimTipRepository.getRandomTip()`;
`VimTipRepositoryImpl.visibleTips()` does the filtering. The button gate
(`TipIdeaVimRc.getAction` → null) remains as a second line of defence.

The **install funnel is preserved**: the filter keys on *IdeaVim installed*, not
on the specific plugin — a user with IdeaVim but not vim-surround still sees the
`ysiw)` tip (discovery intact). Only the no-IdeaVim-at-all case suppresses.
See [ideavimrc-button.md](../features/ideavimrc-button.md) and
[show-tip.md](../features/show-tip.md#tip-selection).

## The remaining gap

The broader product question is still open: **without IdeaVim, *every* tip is
dead**, not just config tips — Vim Coach teaches IdeaVim keystrokes (`dw`, `ciw`)
that need IdeaVim to work at all. We currently still show those plain-motion tips
to a no-IdeaVim user.

## TODO

- [x] Detect "IdeaVim not installed" (service absent / dependency check) —
      `ideaVimAvailable()` / `TipIdeaVimRc.isAvailable()`.
- [x] Option B — suppress **config** tips while IdeaVim is absent.
- [x] Keep plugin tips as **discovery** for users who have IdeaVim but not the
      specific plugin (funnel preserved — see
      [config-tips-roadmap.md](../discover/config-tips-roadmap.md)).
- [ ] Decide behavior for **non-config** tips when IdeaVim is absent (the broader
      "every tip is dead" case):
  - [ ] Option A — nudge: a one-time "Install IdeaVim to use these tips"
        notification instead of (or before) showing tips.
  - [ ] Option B' — suppress *all* tips while IdeaVim is absent.
  - [ ] Option C — leave tips on, but mark them as "needs IdeaVim".

## Related

- [../features/ideavimrc-button.md](../features/ideavimrc-button.md) — the button
  + create-on-click behavior and its error/gating table.
- [config-tips-roadmap.md](../discover/config-tips-roadmap.md) — config-tip kinds and the
  plugin-discovery funnel this must not break.
