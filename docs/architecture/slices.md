# Vertical Slices

Each row is one user-facing capability — every class involved left to right.

```mermaid
graph TD
    subgraph showTip["Show Tip"]
        direction LR
        A1[ShowVimTipAction]
        A2([ShowTips])
        A3[TipNotificationController]
        A4([VimTipService])
        A5([VimCoachSettingsService])
        A6[TipNotificationFactory]
        A7[(VimTipStore)]
        A1 --> A2 --> A3
        A3 --> A4 & A5 & A6
        A4 --> A7
    end

    subgraph refresh["Refresh Tips"]
        direction LR
        B1[RefetchVimTipsAction]
        B2([RefreshTips])
        B3[TipRefreshCoordinator]
        B4([TipSourceService])
        B5[TipSourceServiceImpl]
        B6[RemoteTipSourceServiceImpl]
        B7[TipJsonParser]
        B8[[GitHub API]]
        B9[(VimTipStore)]
        B1 --> B2 --> B3
        B3 --> B4 & B9
        B4 --> B5 --> B6
        B6 --> B7 & B8
    end

    subgraph periodic["Periodic Tips"]
        direction LR
        C1[VimTipStartupActivity]
        C2([ScheduleTips])
        C3[PeriodicTipScheduler]
        C4([VimCoachSettingsService])
        C5([ShowTips])
        C6[TipNotificationController]
        C1 --> C2 --> C3
        C3 --> C4 & C5
        C5 --> C6
    end

    subgraph settings["Settings"]
        direction LR
        D1[VimCoachSettingsConfigurable]
        D2[VimCoachSettingsScreenController]
        D3([VimCoachSettingsService])
        D4([VimTipService])
        D5[(VimCoachSettingsStore)]
        D1 --> D2
        D2 --> D3 & D4
        D3 --> D5
    end

    subgraph ideavimrc["Add to .ideavimrc"]
        direction LR
        E1[TipNotificationController]
        E2([AddTipToIdeaVimRc])
        E3[IdeaVimIntegration]
        E4[[VimRcService]]
        E5[(.ideavimrc)]
        E6[FileDocumentManager]
        E7[FileEditorManager]
        E8[[IdeaVim.ReloadVimRc.reload]]
        E1 --> E2 --> E3 --> E4 --> E5
        E2 --> E6 --> E5
        E1 -->|Added| E7
        E1 -.->|opt reload| E8
    end

    showTip ~~~ refresh ~~~ periodic ~~~ settings ~~~ ideavimrc
```
