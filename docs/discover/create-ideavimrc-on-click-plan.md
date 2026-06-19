# Plan: create `.ideavimrc` on button click (discover / WIP)

Working notes to resume later. **Not implemented yet.**

## Problem

The **Add to .ideavimrc** button was the only on-tip signal that a tip needs a
plugin/option (e.g. `ysiw)` needs vim-surround). The button only showed when a
`.ideavimrc` **already existed**, so a user with IdeaVim but no file saw the tip
with no button and no hint — it looked broken.

Related: [ideavimrc-button.md](../features/ideavimrc-button.md) ·
[plugin-tip-indicator-gap.md](plugin-tip-indicator-gap.md) ·
[config-tips-roadmap.md](config-tips-roadmap.md)

## Decision (chosen direction = "option 1")

Stop requiring a pre-existing `.ideavimrc`. When IdeaVim is installed, **show the
button anyway**; on click, **let IdeaVim create the file**, then append as usual.

Rejected alternatives: a "Requires X" line in the tip body; hiding unmet tips
(kills plugin discovery for new users); a summary chip.

## Functional behavior (after change)

- **Has IdeaVim + has `.ideavimrc`** → unchanged (append on click).
- **Has IdeaVim + no `.ideavimrc`** → button shows; click creates
  `~/.ideavimrc` via IdeaVim, appends the line, opens file, offers "Reload now".
  Confirmation reads "Created ~/.ideavimrc and added …".
- **No IdeaVim** → button stays hidden, nothing created (correct by
  construction; the gated service/`VimRcService` aren't even loaded). Leftover
  indicator gap parked in [plugin-tip-indicator-gap.md](plugin-tip-indicator-gap.md).

## How IdeaVim creates the file

`com.maddyhome.idea.vim.vimscript.services.VimRcService.findOrCreateIdeaVimRc(): Path?`
— public on the vim-engine `object`. Creates `~/.ideavimrc` (or `~/_ideavimrc`)
from IdeaVim's template, returns the path, or null on failure.

**Template is NOT empty** — it ships active lines:
`set scrolloff=5`, `set incsearch`, `map Q gq`,
`Plug 'machakann/vim-highlightedyank'`, `Plug 'tpope/vim-commentary'`
(plus a `source ~/.vimrc` block if `~/.vimrc` exists).

## Implementation sketch

1. **`FindIdeaVimRc` port** — add `findOrCreateVimRc(): Path?`, implemented in
   `IdeaVimPluginFindVimRc` by delegating to `VimRcService.findOrCreateIdeaVimRc()`.
   Isolates the unstable IdeaVim dependency to the one IdeaVim-gated impl
   (same pattern as `findVimRc()` and the reload action-id).
2. **`AddTipToIdeaVimRc`** — inject `() -> FindIdeaVimRc?`
   (`= { serviceOrNull<FindIdeaVimRc>() }`).
   - `isAvailable()` → service present (IdeaVim installed) + config lines exist.
     No longer needs a file.
   - `add()` → check `findVimRc()` first (null ⇒ "will create"), then
     `findOrCreateVimRc()`, then the existing refresh → Document append →
     save → highlight → reload flow.
   - null from `findOrCreateVimRc()` → `Result.Failed(NotAccessible)`.
3. **Result message** — add a "Created ~/.ideavimrc and added …" variant
   (new `TipNotifier` method + bundle key) used when the file was freshly created.
4. **Docs/tests** — reverse "file creation out of scope" in
   `ideavimrc-button.md` + its gating/error table; flip the "hidden when no file"
   tests; add a create-on-click test via a fake `FindIdeaVimRc`; update
   README/CHANGELOG (user-facing).

## OPEN DECISION (pin)

**#1 — message for create-then-already-present.** Because the template already
contains `vim-commentary` and `vim-highlightedyank`, clicking **Install
vim-commentary** / **Install vim-highlightedyank** on a no-file system creates
the file (already containing that Plug line) → block-match returns
`AlreadyPresent` → "already in your .ideavimrc" right after a create reads as
confusing. Decide: dedicated message ("Created ~/.ideavimrc — vim-commentary is
already included") vs punt to the generic already-present message.

## Other considerations (handle during impl, no decision needed)

- **Compile dep on `VimRcService`** — reload used a string action-id to avoid a
  compile dep; here we `import` it directly. Safe only because the call lives in
  the IdeaVim-gated `IdeaVimPluginFindVimRc`. Confirm the build resolves the
  symbol (optional plugin on compile classpath) before relying on it.
- **Threading** — `findOrCreateIdeaVimRc` writes via raw NIO; we then
  `refreshAndFindFileByNioFile` before the Document append. Verify the refresh
  picks up the just-written file on Windows.
- **Reload activates the plugin** — created file + appended `Plug` + "Reload now"
  must enable the extension live. Runtime behavior → verify in `runIde`, not just
  unit tests.
- **Undo asymmetry** — append is on the undo stack; the NIO file creation is not.
  Undo removes lines, not the file. Acceptable.
- **XDG users** — IdeaVim only ever creates `~/.ideavimrc` / `~/_ideavimrc`,
  never the XDG path. Matches IdeaVim's own behavior.

## Resume checklist

- [ ] Decide OPEN DECISION #1.
- [ ] Confirm `VimRcService` compile-classpath availability.
- [ ] Implement steps 1–3.
- [ ] Update docs/tests + README/CHANGELOG (step 4).
- [ ] `runIde` verification of create + reload + plugin activation.
