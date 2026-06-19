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
See [ideavimrc-button.md](../features/ideavimrc-button.md) and the
[rejected create-on-click plan](create-ideavimrc-on-click-plan.md).

## The remaining gap

When **IdeaVim is not installed**, the button is correctly hidden (nothing could
read a `.ideavimrc`, and `VimRcService` isn't on the classpath). But then a
plugin/config tip renders with **no indicator** it needs anything — the user
tries `ysiw)`, nothing happens, confusion.

This is a slice of a broader product question: **without IdeaVim, *every* tip is
dead**, not just plugin tips — Vim Coach teaches IdeaVim keystrokes that need
IdeaVim to work at all.

## TODO

- [ ] Decide Vim Coach's behavior when IdeaVim is **absent**:
  - [ ] Detect "IdeaVim not installed" (service absent / dependency check).
  - [ ] Option A — nudge: a one-time "Install IdeaVim to use these tips"
        notification instead of (or before) showing tips.
  - [ ] Option B — suppress plugin/config tips entirely while IdeaVim is absent.
  - [ ] Option C — leave tips on, but mark plugin/config tips as "needs IdeaVim".
- [ ] Decide whether plugin tips should still surface as **discovery** for users
      who have IdeaVim but not the specific plugin (don't kill the install funnel
      — see [config-tips-roadmap.md](../discover/config-tips-roadmap.md)).
- [ ] If we keep showing tips without IdeaVim, settle how (if at all) to indicate
      a tip's requirement in the body without the button as the carrier.

## Related

- [../features/ideavimrc-button.md](../features/ideavimrc-button.md) — the button
  + create-on-click behavior and its error/gating table.
- [config-tips-roadmap.md](../discover/config-tips-roadmap.md) — config-tip kinds and the
  plugin-discovery funnel this must not break.
