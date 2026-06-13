# Add to .ideavimrc

When a tip has configuration lines (e.g. `set surround`, `Plug 'tpope/vim-surround'`), an **"Add to .ideavimrc"** action button appears on the tip notification. Clicking it appends those lines to the user's `.ideavimrc`, opens the file in the editor at the added lines, and offers a **"Reload now"** button if IdeaVim's reload action is available.

## Vertical Slice

```mermaid
graph LR
    A[TipNotificationController] -->|"tip.config non-empty"| B[AddTipToIdeaVimRc]
    B --> C[IdeaVimRcFile]
    C --> D["~/.ideavimrc\n(or XDG path)"]
    B -->|Result| A
    A -->|Added| E[openIdeaVimRc\nhighlightAppendedLines]
    A -->|AlreadyPresent| F[openIdeaVimRc\nno highlight]
    A -->|Failed| G[failure notification]
    A -->|Added + IdeaVim present| H[reloadIdeaVimRc]
```

## Flow on Button Click

```mermaid
sequenceDiagram
    participant User
    participant CTRL as TipNotificationController
    participant SAVE as FileDocumentManager
    participant USE as AddTipToIdeaVimRc
    participant FILE as IdeaVimRcFile
    participant FS as Filesystem
    participant ED as FileEditorManager
    participant HM as HighlightManager
    participant AM as ActionManager

    User->>CTRL: clicks "Add to .ideavimrc"
    CTRL->>SAVE: saveDocument (if .ideavimrc open in editor)
    CTRL->>USE: add(tip)
    USE->>FILE: findOrCreate()
    alt file exists
        FILE-->>USE: Path
    else file missing
        FILE->>FS: createFile ~/.ideavimrc
        FILE-->>USE: Path (new)
    end
    USE->>FILE: append(path, tip.config)
    FILE->>FS: readText (existing content)
    note over FILE: skip lines already present (exact match)
    FILE->>FS: writeText (existing + new lines)
    FILE-->>USE: AppendOutcome
    USE-->>CTRL: Result.Added / AlreadyPresent / Failed

    alt Added
        CTRL->>ED: openTextEditor at startLine
        CTRL->>ED: reloadFromDisk (sync VFS after NIO write)
        CTRL->>HM: addRangeHighlight (flash added lines)
        CTRL->>CTRL: show "Added to .ideavimrc" notification
        opt IdeaVim reload action registered
            note over CTRL: "Reload now" button shown
            User->>CTRL: clicks "Reload now"
            CTRL->>AM: execute IdeaVim.ReloadVimRc.reload
            CTRL->>CTRL: show ".ideavimrc reloaded" notification
        end
    else AlreadyPresent
        CTRL->>ED: openFile (no highlight)
        CTRL->>CTRL: show "Already in .ideavimrc" notification
    else Failed
        CTRL->>CTRL: show failure notification (WARNING)
    end
```

## File Discovery

`IdeaVimRcFile` mirrors IdeaVim's own `VimRcService` search order:

| Priority | Path |
|----------|------|
| 1 | `~/.ideavimrc` |
| 2 | `~/_ideavimrc` |
| 3 | `$XDG_CONFIG_HOME/ideavim/ideavimrc` (defaults to `~/.config/ideavim/ideavimrc`) |

If none exists, `findOrCreate()` creates `~/.ideavimrc` (first candidate that succeeds).

IdeaVim is not a runtime dependency — the search logic is reimplemented to avoid coupling to a submodule.

## Dedup Logic

Before writing, `IdeaVimRcFile.append` reads the file and builds a set of trimmed lines. Any config line already present verbatim is silently skipped — only genuinely new lines are appended.

**Limitation:** dedup is exact-match only. It will not detect semantic equivalents (e.g. `set surround` vs. `Plug 'tpope/vim-surround'` enabling the same feature).

## VFS Sync

NIO writes bypass IntelliJ's Virtual File System, so the open `Document` has stale content after `refreshAndFindFileByNioFile` (which schedules an async reload). `TipNotificationController.syncDocumentFromDisk` calls `reloadFromDisk` directly to make the reload synchronous, ensuring the editor opens at the correct line with up-to-date content.

## Error Paths

| Condition | Result |
|-----------|--------|
| `tip.config` is empty | Button not shown |
| File creation fails (IOException) | `Result.Failed` → warning notification |
| Append fails (IOException) | `Result.Failed` → warning notification |
| All config lines already present | `Result.AlreadyPresent` → file opened, no highlight |
| IdeaVim reload action not registered | "Reload now" button not shown |
| Reload action missing at click time | warning notification |
