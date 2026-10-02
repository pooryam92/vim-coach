# The tip balloon — measured layout (IntelliJ 2026.1)

What the reader actually sees, so length and wording calls rest on numbers, not
guesses. Measured on build 261 (2026.1.3) by rendering every tip through the
real `TipNotificationFactory` + platform `NotificationsUtil` HTML path with the
IDE's bundled Inter 13, plus a screenshot of a live balloon. Platform sources:
`BalloonLayoutConfiguration.java`, `NotificationsManagerImpl.java`,
`BalloonLayoutImpl.kt` in JetBrains/intellij-community (branch `261`).

## What the reader sees first: title + summary only

A balloon with a title *and* actions shows only **2 lines of body (34px)**
before the reader clicks the expand chevron (`lines = 4 − title − actions`).
Every tip is taller than that (summary + 2 details ≈ 77px), so **every tip
opens collapsed**:

```
┌──────────────────────────────────────────┐
│ [icon] Vim Coach · Advanced · Visual mode│  title (dimmed labels)
│        Fold a Visual selection zf        │  summary — the only body line shown
│                                          │
│        Next   Mute                     ⌄  │  actions + expand chevron
└──────────────────────────────────────────┘
```

Details and the mnemonic are behind the chevron. So the **summary must carry
the tip on its own** — which behavior, which keys — because most readers never
expand. Details are the payoff for the reader who does.

The collapse is **intended, not a defect**: one line up front, more on demand,
so a tip never overwhelms the reader. A layout that showed title + two details
at once (summary as the title, margin-free details) was tried on 2026-10-02 and
rejected as too much text in one go. Don't reclaim the collapsed space for more
text.

## Widths (1× scale, identical on Linux, Windows, macOS)

| Part | Width | Source |
|---|---|---|
| Whole balloon | 380px (360 + 10px shadow each side) | stack forces `FixedWidth()` on every balloon |
| Text column | ≈ 322px (360 − 4 left inset − 32 icon column − 2 gap) | computed from insets |
| Body line, normal | wraps past ≈ 320px | content up to `MaxWidth()` = 320 stays unwrapped |
| Body line, after clamp | wraps at **312px** | any line > 320px applies `width:240px`, which Swing renders ×1.3 = 312 |
| Title | bold, `nowrap`, clipped past 292px | text column − 30px close-button offset |
| Action row | 306px collapsed, 290px expanded | text column − 16px gap (− 16px collapse icon when expanded) |

The `width:240px` clamp is a red herring: Swing's default CSS scales `px` by
1.3 (the notification kit never sets `W3C_LENGTH_UNITS`), so clamping narrows
the body by only ~8px. There is effectively **one body limit: ≈ 312–320px**.

Before 2026.1 the width varied by OS (Linux 410/270, Windows 330/205, macOS
360/240 raw/style px); commit `0bc35bdd52` (IJPL-219830) unified all OSes to
the old macOS values. The plugin supports builds from 252, so Linux readers on
2025.x get a wider balloon and Windows readers a narrower one — 2026.1 is the
reference.

## Font and characters per line

The new UI on the JetBrains Runtime uses **Inter 13** on all three OSes; body
lines are 17px tall. JBR bundles Inter Regular/Italic/SemiBold but no Bold, so
the bold summary is synthesized from Regular and measures the same width.

Measured over the real tip corpus (282 tips):

| Text | Style | px/char mean / p90 | chars per 312px line (typical / worst) |
|---|---|---|---|
| summary | bold | 6.34 / 6.77 | ~49 / ~46 |
| details | regular | 6.32 / 6.72 | ~49 / ~46 |
| mnemonic (incl. `Mnemonic:` label) | italic | 6.81 / 7.18 | ~46 / ~43 |
| title | bold | — | ~46 in 292px |

Chars per line are invariant to IDE font size and HiDPI (`JBUIScale` derives
from the font size). They change only under a user font override, a CJK locale
(falls back to the system font), the classic UI, or a non-JBR runtime.

At the time of measuring, **no tip wrapped**: the widest summary was 236px
(35 chars), the widest detail 245px (35 chars). The widest title
(`Vim Coach · Advanced · Command mode`) is 253px of 292.

**Limits SKILL.md sets from this:** summary and detail lines max **42 chars** —
312px ÷ 7.28px, the widest per-char rate measured on any summary line, so even
a glyph-heavy line fits. Mnemonic max **32 chars**, because the 10-char
`Mnemonic: ` label shares its italic line (42 × 7.18px ≈ 302px).

## Action row: the `config.name` budget

Actions render as links at Inter 13 with a 16px gap: `Next` (30px), `Mute`
(33px), then the config action — its `config.name` verbatim, else `Apply`. When
the row exceeds the available width, IntelliJ hides links from the right into a
`More ▾` dropdown, which would bury the config action.

Row = 63 + 32 + name width. Every current name fits both states:

| `config.name` | name px | row px | collapsed (306) | expanded (290) |
|---|---|---|---|---|
| Enable live substitute preview | 189 | 284 | fits | fits |
| Install vim-paragraph-motion | 179 | 274 | fits | fits |
| Show relative line numbers | 170 | 265 | fits | fits |

Budget: a `config.name` up to **~195px (~30 chars)** survives both states,
~211px (~33 chars) the default collapsed view.

## Re-measuring

Re-measure when the platform version moves or a title/action label changes.
The method: a throwaway `BasePlatformTestCase` in
`features/tips/ui/notifications` that sets `UIManager` `Label.font` and
`EditorPane.font` to Inter 13, builds each tip with
`TipNotificationFactory.createNotificationWithActions`, renders it through
`NotificationsUtil.configureHtmlEditorKit` + `NotificationsUtil.buildHtml`, and
reads line breaks and widths from `modelToView2D`. Gotchas: the test icon
manager returns 16px dummy icons, so `FixedWidth()`/`MaxWidth()` read 392/332
in tests — use the real 380/320; and `HTMLDocument.getFont()` reports 12pt
because it ignores the pane's display font — measure from the views, not the
stylesheet. Delete the probe afterwards; confirm with a screenshot from
`./gradlew runIde`.
