# Add to .ideavimrc

When a tip has configuration lines (e.g. `set surround`, `Plug 'tpope/vim-surround'`) **and** the user has IdeaVim installed, an apply action button appears on the tip notification. Clicking it appends those lines to the `.ideavimrc`, opens it in the editor at the added lines, and offers a **"Reload now"** button if IdeaVim's reload action is available.

The button label comes from the tip's `config`: if `config.name` is set it is used **verbatim** (e.g. `Install vim-surround`); otherwise the generic **"Apply"** label is used. The lines written to the file come from `config.lines`. See the `tips-maintain` skill ([../../.claude/skills/tips-maintain/SKILL.md](../../.claude/skills/tips-maintain/SKILL.md)) for the `config` object shape (and the legacy array form still accepted for back-compat).

**File creation is deliberately out of scope** — Vim Coach never creates a `.ideavimrc` itself. The button is gated only on **IdeaVim being installed** (not on a file existing), so a user who has IdeaVim but no config file still sees the affordance. When they click it and no `.ideavimrc` exists yet (`Result.NoVimRc`), they get a one-line guidance message pointing at IdeaVim's own "Create ~/.ideavimrc" (the Vim status-bar icon); once they create it, the next click appends as normal. This closes the **has-IdeaVim, no-file** dead-end without taking a dependency on IdeaVim's internal file-creation API (`VimRcService.findOrCreateIdeaVimRc()`, the unstable internal the codebase deliberately avoids). See [the indicator-gap notes](../TODO/plugin-tip-indicator-gap.md) for the still-open **no-IdeaVim** case.

## Vertical Slice

```mermaid
graph LR
    A[TipNotifications] -->|"getAction(tip)"| B[TipIdeaVimRc]
    B -->|"isAvailable()"| C[AddTipToIdeaVimRc]
    C -->|"serviceOrNull()"| D([FindIdeaVimRc])
    D --> E[IdeaVimPluginFindVimRc]
    E --> F["~/.ideavimrc\n(or XDG path)"]
    C -->|Document API| G[FileDocumentManager]
    G --> H[IntelliJ Document]
    H --> F
    C -->|Result| B
    B -->|Added| I[openIdeaVimRcAtLine\nhighlightAppendedLines]
    B -->|AlreadyPresent| J[openIdeaVimRcAtLine\nhighlightAppendedLines]
    B -->|NoVimRc| M[create-it guidance notification]
    B -->|Failed| K[failure notification]
    B -->|Added + IdeaVim present| L[reloadIdeaVimRc]
```

## Flow on Button Click

```mermaid
sequenceDiagram
    participant User
    participant TN as TipNotifications
    participant IV as TipIdeaVimRc
    participant ADD as AddTipToIdeaVimRc
    participant PLAN as IdeaVimRcAppendPlan
    participant FIND as FindIdeaVimRc
    participant VFS as LocalFileSystem / VirtualFile
    participant DOC as FileDocumentManager / Document
    participant ED as FileEditorManager
    participant HM as HighlightManager
    participant N as TipNotifier
    participant AM as ActionManager

    User->>TN: clicks "Add to .ideavimrc"
    note over TN: executes lambda returned earlier by TipIdeaVimRc.getAction(tip)
    TN->>IV: handle(tip)
    IV->>ADD: add(tip)
    ADD->>FIND: findVimRc() via serviceOrNull<FindIdeaVimRc>()
    note over FIND: IdeaVimPluginFindVimRc searches user.home + XDG paths
    FIND-->>ADD: Path (or null ⇒ Result.NoVimRc, no file written)
    ADD->>VFS: refreshAndFindFileByNioFile(path)
    ADD->>VFS: vf.isWritable
    ADD->>DOC: getDocument(vf) → read document.text
    ADD->>PLAN: determine(existingText, tip.config?.lines)
    note over PLAN: pure: block match + insert text + start line
    PLAN-->>ADD: Append / AlreadyPresent / Empty
    ADD->>DOC: WriteCommandAction → insertString
    ADD->>DOC: WriteAction → saveDocument (sync flush to disk)
    ADD-->>IV: Result.Added / AlreadyPresent / Failed

    alt Added
        IV->>ED: openTextEditor at startLine
        IV->>HM: addRangeHighlight (flash added lines)
        IV->>N: showAddedToIdeaVimRc(onReload)
        opt IdeaVim reload action registered
            User->>IV: clicks "Reload now"
            IV->>AM: execute IdeaVim.ReloadVimRc.reload
            IV->>N: showReloadedIdeaVimRc()
        end
    else AlreadyPresent
        IV->>ED: openTextEditor at startLine
        IV->>HM: addRangeHighlight (flash existing lines)
        IV->>N: showAlreadyInIdeaVimRc()
    else NoVimRc
        IV->>N: showCreateIdeaVimRcGuidance()
    else Failed
        IV->>N: showAddToIdeaVimRcFailed()
    end
```

Notifications go through the `TipNotifier` port (see [Show Tip](show-tip.md)); `TipIdeaVimRc` itself touches no IntelliJ `Notification` types. `project` is used only for editor IO (open / highlight).

## File Discovery

`FindIdeaVimRc` is an application service interface registered only when IdeaVim is installed (via `plugin-ideavim.xml`), with `IdeaVimPluginFindVimRc` as its implementation. `AddTipToIdeaVimRc` resolves it via `serviceOrNull<FindIdeaVimRc>()` — when IdeaVim is absent the service is not registered, `serviceOrNull` returns null, and `isAvailable()` returns false. `isAvailable()` keys purely on the **service's presence** (IdeaVim installed), not on a file existing: the file lookup happens later in `add()`, which returns `Result.NoVimRc` when `findVimRc()` finds nothing — that result drives the create-it guidance rather than hiding the button.

`plugin.xml` declares `IdeaVIM` as an optional dependency for that descriptor, and `gradle.properties` declares the same Marketplace plugin in `platformPlugins`. Keeping both in sync lets IntelliJ resolve `plugin-ideavim.xml` during development and lets the custom run IDE tasks install the same IdeaVim version.

`IdeaVimPluginFindVimRc.findVimRc()` replicates IdeaVim's search order directly using `System.getProperty("user.home")` and `System.getenv("XDG_CONFIG_HOME")`:

| Priority | Path |
|----------|------|
| 1 | `~/.ideavimrc` |
| 2 | `~/_ideavimrc` |
| 3 | `$XDG_CONFIG_HOME/ideavim/ideavimrc` (defaults to `~/.config/ideavim/ideavimrc`) |

Only paths that already exist on disk are returned; `null` means no file was found.

## Why Document API

All writes go through IntelliJ's `Document` + `WriteCommandAction` rather than NIO:

- **Windows compatibility**: IntelliJ holds an exclusive file lock on open documents. A raw NIO write would fail with an access error. The Document API is the platform's own abstraction over this.
- **Line endings**: Document API normalises line endings per platform automatically — no `System.lineSeparator()` differences.
- **No VFS sync needed**: the Document is always current; `reloadFromDisk` is unnecessary.
- **Undo support**: `WriteCommandAction` registers the change in IntelliJ's undo stack.

After `WriteCommandAction`, the document is saved synchronously via `WriteAction { saveDocument(doc) }` so IdeaVim's "Reload now" reads the up-to-date file from disk immediately.

## Append Planning

Before writing, `AddTipToIdeaVimRc.add` reads `document.text` and hands it with `tip.config?.lines` to `IdeaVimRcAppendPlan.determine()` — a pure, IDE-free function that decides what to append. A tip's config is treated as a **single, indivisible snippet**: config lines are trimmed and blank lines dropped, then the snippet is matched against the file as a contiguous run of trimmed lines, in order. The result is one of:

- **`AlreadyPresent(startLine, lineCount)`** — the whole block already exists; nothing is written. The 0-based start line and line span of the existing block are returned so the caller can re-highlight it.
- **`Append(insertText, startLine, addedCount)`** — the snippet is copied in full (verbatim, preserving order and any repeated lines), along with the 0-based start line of the first appended line and the added-line count.
- **`Empty`** — the config had no usable (non-blank) lines.

It deliberately does **not** append "just the missing lines": a snippet whose lines exist but are scattered or reordered is re-appended in full, since a snippet may rely on its lines being together and in order. Keeping this logic free of `Document`/VFS types makes the branching unit-testable (`IdeaVimRcAppendPlanUnitTest`); `add()` is left to do only the IO.

### Vim Coach stamp

When appending, a vimscript comment is written **above** the snippet so the user can tell which lines Vim Coach added — `AddTipToIdeaVimRc.stampFor` produces `" <name> — added by Vim Coach` when the tip's `config.name` is set, or `" Added by Vim Coach` otherwise. The stamp counts toward `addedCount` so the highlight covers the whole inserted block (stamp + lines).

The stamp is **not** part of the already-present match: `findBlockStart` keys off the real config lines only. A snippet that was previously added with a stamp is still recognised on a re-add — neither the lines nor a second stamp are duplicated.

The stamp is **attribution only** — it just marks the lines as Vim Coach's, deliberately not a description of what they do (a richer authored `comment` was considered and dropped as not worth the duplication/authoring cost). A multi-line config gets the single header stamp above the whole block; a closing-boundary marker (to show where a multi-line block *ends*) is deferred until multi-line configs actually exist — it would be a purely additive change to the insert builder, with no schema or dedup impact.

**Limitation:** block matching is exact-match only. It will not detect semantic equivalents (e.g. `set surround` vs. `Plug 'tpope/vim-surround'` enabling the same feature). Key-aware dedup is tracked as future work.

## Error Paths

| Condition | Result |
|-----------|--------|
| IdeaVim not installed | Button not shown (`FindIdeaVimRc` service unregistered → `isAvailable()` false) |
| `tip.config` absent or `config.lines` empty | Button not shown |
| `.ideavimrc` does not exist (IdeaVim installed) | Button shown; click → `Result.NoVimRc` → guidance to create one via IdeaVim (no file written) |
| `VirtualFile` not found | `Result.Failed` → warning notification |
| File not writable | `Result.Failed` → warning notification |
| `Document` unavailable | `Result.Failed` → warning notification |
| All config lines already present | `Result.AlreadyPresent` → file opened at the existing lines, briefly highlighted |
| IdeaVim reload action not registered | "Reload now" button not shown |
