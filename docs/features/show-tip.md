# Show Tip

Displays a random tip as an IntelliJ balloon notification. The same flow is triggered by three entry points: the `ShowVimTipAction` (user-invoked from Find Action), startup (after the tip cache is refreshed), and the periodic scheduler.

## Components

```mermaid
graph LR
    A1[ShowVimTipAction] -->|showRandomTip| B
    A2[VimTipStartupActivity] -->|showRandomTipIfNoneActive| B
    A3[TipScheduler] -->|showRandomTipIfNoneActive| B
    B([ShowTips]) --> C[TipNotifications]
    C --> D([VimTipRepository])
    C --> E([SettingsRepository])
    C --> N([TipNotifier])
    N --> NA[IntelliJTipNotifier]
    NA --> F[TipNotificationFactory]
    NA --> T[ActiveTipNotificationTracker]
    D --> G[(PersistentVimTipStore)]
    E --> H[(PersistentSettingsStore)]
```

`ShowTips` is a project service. `TipNotifications` is its implementation. The interface has two methods:

- `showRandomTip()` — always shows a tip, expiring any currently visible one first.
- `showRandomTipIfNoneActive()` — skips silently if a tip balloon is already visible. Used by startup and the periodic scheduler so they don't interrupt the user.

## Notification Port

`TipNotifications` (application layer) speaks only to the `TipNotifier` port — a platform-agnostic interface (`showTip`, `showTipExcluded`, `showAdvancedTipsAvailable`, `hasVisibleTip`, the `.ideavimrc` result messages). It holds **no** IntelliJ `Notification` types: wording, balloon rendering, and the active-notification lifecycle all live behind the port. This is the seam that lets the notification presentation be swapped (e.g. a different layout or surface) without touching application code, and it is what makes `TipNotifications` unit-testable against a fake.

`IntelliJTipNotifier` (UI layer) is the production adapter, registered as a project service (`TipNotifier` → `IntelliJTipNotifier`). It renders via `TipNotificationFactory`, owns the `ActiveTipNotificationTracker`, and is the only place that calls `Notification.notify()` / `expire()`.

## Tip Selection

Selection runs five stages before a tip is drawn. The first happens in `TipNotifications`; the rest live in `TipSelector`, a pure component the repository delegates to — the single chokepoint for both `getRandomTip` overloads, so every entry point passes through the same filters and any future filter has an obvious home.

1. **Category filter**: `selectRandomTip()` in `TipNotifications` asks `SettingsRepository` for the enabled categories, then calls `VimTipRepository.getRandomTip(enabledCategories, includeConfigTips)`. If no categories are available yet (tips not loaded), falls back to `getRandomTip(includeConfigTips)` with no category filter.

2. **Config filter**: `includeConfigTips` is `ideaVimAvailable()` — true only when IdeaVim is installed. When IdeaVim is absent, tips carrying an `.ideavimrc` snippet (`VimTip.config`) are dropped from the draw, since their only payoff is the "Add to .ideavimrc" button (see [Add to .ideavimrc](ideavimrc-button.md)), which is itself hidden without IdeaVim. This keeps users (e.g. WebStorm with no IdeaVim) from seeing tips they can't act on.

3. **Exclusion filter**: tips whose SHA-256 hash of the summary appears in the hidden-hashes list are stripped before the random draw. The hash is computed by `TipHash.fromTip()`.

4. **Advanced filter**: tips marked `VimTip.advanced` are dropped unless `SettingsRepository.isShowAdvancedTipsEnabled()` is on (default off). When the settings service is unavailable, advanced tips are hidden (the safe default). See [Settings](settings.md#advanced-tips-opt-in) for the toggle.

`VimTipRepositoryImpl` packages the inputs to stages 2–4 as a `TipVisibilityCriteria` (read from `SettingsRepository` on each draw) and hands them to `TipSelector` together with the category-matched tips.

5. **No-repeat rotation** (`TipRotation`, invoked by `TipSelector` on whatever survived stages 1–4): a tip already shown this IDE session is not drawn again until every eligible tip has been shown once. The shown-tip memory is a set of `TipHash`es held by the repository — an application service — so one rotation is shared across all projects and entry points. It is deliberately **in-memory only**: the cycle restarts with the IDE. When the eligible pool is exhausted, only that pool's hashes are forgotten before redrawing, so cycling through one category filter never resets progress through another. Because rotation runs after the exclusion filter, an excluded tip can neither block the cycle nor be resurrected by a reset. Fallback tips bypass the rotation entirely.

`TipSelectionIndex` is a lazy cache inside `VimTipRepositoryImpl` that groups tips by category. It is rebuilt only when the tip list reference changes (after a refresh), not on every call.

Fallback tips are returned when the candidate list is empty after filtering:

| Condition | Fallback shown |
|-----------|----------------|
| No tips loaded at all | "No tips found." |
| All tips filtered out by category (or all remaining tips are advanced with the opt-in off) | "No tips match the selected categories." |

The filtered fallback's detail line points at both remedies — enabling a matching category and turning on "Show advanced tips" — since either can be the reason the draw came up empty.

## Active Notification Tracking

`ActiveTipNotificationTracker` (UI layer, owned by `IntelliJTipNotifier`) keeps a reference to the current tip notification per project. `hasVisibleNotification()` — surfaced through the port as `TipNotifier.hasVisibleTip()` — checks whether the tracked notification still has a live, non-disposed balloon; stale references are cleared and treated as absent. The tracker uses a lock because `replaceWith` and expiry callbacks can run on different threads.

## Exclude Flow

Clicking "Don't show again" runs `TipNotifications`' exclude callback, which applies the business rule via `ExcludeTipFromNotifications.exclude()`:

1. Computes `TipHash.fromTip(tip)` (SHA-256 of the trimmed summary).
2. Calls `settingsService.hideTip(hash)` — adds the hash to the persistent hidden list.
3. Calls `settingsService.consumeExcludedTipsManagementHint()`. This returns `true` exactly once (on the first-ever exclusion) and flips a persistent flag. When it returns `true`, `TipNotifier.showTipExcluded()` presents a secondary notification prompting the user to manage excluded tips in Settings.

Dismissing the tip balloon is the adapter's responsibility: `IntelliJTipNotifier` wires the action to run the application callback and then expire that notification, keeping `Notification` handling out of the application layer.

## Advanced Tips Marker and Nudge

The `advanced` flag is parsed leniently and the gate lives only in the new plugin, so marking a tip advanced hides it **only for users on this version or later** — an older plugin has no gate and shows it as a normal tip. This is acceptable (those users already saw every tip) but cannot be retroactive.

Two presentation concerns support the advanced-tips opt-in (see [Settings](settings.md#advanced-tips-opt-in)):

- **Title label.** `TipNotificationFactory` appends dimmed metadata labels to the notification title after the app name. The title renders HTML: `Vim Coach` stays full weight while the whole label tail sits inside one `<span>` coloured at `mnemonicForeground` (the same dim used for the balloon's mnemonic line), so the app name is the only prominent text. Two optional labels join with ` · ` (the app-name separator glyph), **Advanced first, then mode**: `Vim Coach · Advanced` when `tip.advanced` is true, `Vim Coach · Insert mode` (or `Visual mode` / `Command mode`) when the tip carries a `mode`, and `Vim Coach · Advanced · Insert mode` when both. A tip with neither label keeps the plain, non-HTML `Vim Coach` title. Only opted-in users ever draw an advanced tip and the mode is informational, so the labels need no in-app legend. See [Tips pipeline](../tips/tips-pipeline.md) for the `mode` field itself.
- **One-time nudge.** After showing any tip, `TipNotifications` asks `AdvancedTipsNudge.shouldNudgeAfterTipShown()` (a dedicated application use-case, mirroring `ExcludeTipFromNotifications`) whether to announce advanced tips; on `true` it calls `TipNotifier.showAdvancedTipsAvailable()`, which presents a notification with an "Open settings" action. `AdvancedTipsNudge` owns the whole rule — the guards, the sequencing, and the three-tip threshold (a constant private to the class) — while `SettingsRepository` only persists the plain state behind it: the hint flag and a tip counter (`tipsShownForAdvancedNudge`). The guards run cheapest-and-most-final first: the persistent hint flag is read first, so once it is set every later tip exits on a single boolean read instead of scanning the tip cache. Then the setting must be off, the cache must contain advanced tips (`VimTipRepository.hasAdvancedTips()`), and the counter must reach **three** tips seen while the nudge is pending — so a fresh install isn't ambushed with a settings pointer on its very first tip. The counter self-bounds: it stops writing once the threshold is reached, so it only ever costs three settings writes. Only then is `SettingsRepository.consumeAdvancedTipsHint()` called — a check-and-set that returns `true` exactly once and flips the persistent flag (mirroring the excluded-tips hint above), so the count is only spent while a nudge is genuinely pending. The hint is also retired when the user enables "Show advanced tips" on their own (`setShowAdvancedTipsEnabled(true)` sets the flag): someone who found the toggle and later turned it off is never nudged to re-enable it. Both the counter and the flag live in `PersistentSettingsStore` (which roams), so the nudge normally fires **once per user**, not once per machine. The check-and-set is local, though — two machines that each reach the count before settings sync propagates the flag can each show the nudge once, a rare and acceptable double. A pre-feature store deserializes the count to `0` and the flag to "not shown", correct for existing users.

## Notification Structure

`TipNotificationFactory` (invoked by `IntelliJTipNotifier`) renders the tip as an HTML string. The layout is:

```html
<html><div>
  <div style="margin-top:5px;"><b>summary</b></div>
  <div style="margin-top:8px;margin-bottom:8px;">detail line 1<br/>detail line 2</div>
  <div style="margin-top:4px;font-style:italic;color:#8c8c8c;">Mnemonic: hook</div>
</div></html>
```

The mnemonic line is omitted when the tip has none; when present it is dimmed by
mixing the theme's label foreground toward its context-help foreground (`MNEMONIC_DIM_RATIO`,
resolved at render time), keeping the summary and details at full strength.

Actions are standard `NotificationAction` buttons appended to the balloon, in order:

- **"Next tip"** and **"Exclude tip"** — always present.
- **Apply-to-`.ideavimrc`** — when `tip.config?.lines` is non-empty; labelled by `config.name` or the generic "Apply".
- **"Note…"** — a **dev-only** action for flagging a tip for maintainers. It appears only when the `vimcoach.tip.notes.file` system property is set, which the `runIdeWithFileTips` and `runIdeWithMinuteTipSchedule` Gradle tasks do (pointing at the git-ignored `docs/tips/tip-feedback.md`). Released builds never set it, so the action is absent. Clicking it opens a multiline input dialog and appends a timestamped markdown entry — summary, tip hash, and the note — to that file (see `RecordTipNote`). See [Tips pipeline](../tips/tips-pipeline.md) for the maintainer workflow.
