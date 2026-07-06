<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Vim Coach Changelog

## [Unreleased]

### Added

- Tips can now carry a **mnemonic** — a short memory aid shown as a dimmed line beneath the tip to make its keystrokes easier to recall
- A **Show advanced tips** setting (off by default) that keeps advanced, niche tips out of the default rotation until you opt in from Vim Coach settings. Advanced tips are labelled `Vim Coach · Advanced` in their notification title, and a one-time notification points you to the toggle the first time an advanced tip is available

### Changed

- Expanded and refreshed the tip library with new tips across categories

## [1.4.1] - 2026-06-22

### Fixed

- Config-bearing tips and their **Apply to .ideavimrc** button now appear automatically after upgrading the plugin, instead of requiring a manual **Refresh Tips**. The tip cache now records which plugin version parsed it and refetches once after an upgrade so a newer parser re-runs even when the remote tip content is unchanged

### Changed

- Tips that carry an `.ideavimrc` snippet are now hidden entirely when IdeaVim is not installed, rather than being shown without an actionable button

## [1.4.0] - 2026-06-19

### Added

- Tip notifications now include an **Apply** action that adds a tip's mapping or command directly to your `.ideavimrc` when the IdeaVim plugin is installed, with one-click reload and detection of lines already present. The action appears whenever IdeaVim is installed; if you have no config file yet, it guides you to create one through IdeaVim's "Create ~/.ideavimrc" — Vim Coach never creates the file itself

### Changed

- Reorganized and expanded the tip library, including new `files`, `registers`, and `plugins` categories, renamed `motion` → `navigation` and `map` → `mappings`, and improved plugin tips with additional tips across categories

## [1.4.0-beta.2] - 2026-06-19

### Changed

- The **Apply to .ideavimrc** action now appears whenever IdeaVim is installed, not only when a `.ideavimrc` already exists. If you have no config file yet, clicking it guides you to create one through IdeaVim's "Create ~/.ideavimrc" — Vim Coach never creates the file itself
- Expanded and refined the tip library, with improved plugin tips and additional tips across categories

## [1.4.0-beta.1] - 2026-06-18

### Added

- Tip notifications now include an **Apply** action that adds a tip's mapping or command directly to your `.ideavimrc` when the IdeaVim plugin is installed, with one-click reload and detection of lines already present
- Optional `vimcoach.tip.remote.url` JVM override to point the plugin at an alternative remote tip source (e.g. a beta branch) without rebuilding

### Changed

- Reorganized and expanded the tip library, including new `files`, `registers`, and `plugins` categories and renamed `motion` → `navigation` and `map` → `mappings`

## [1.3.0] - 2026-05-24

### Added

- Tip notifications now include an `Exclude tip` action to hide specific tips from future random selections
- Vim Coach settings now include an excluded tips list with undo controls for restoring hidden tips

## [1.2.2] - 2026-04-30

### Added

- Settings category controls now include a single button to select or deselect all categories

## [1.2.1] - 2026-04-07

### Changed

- Newly added tip categories now stay selected by default in Vim Coach settings after tip refreshes
- Category selection persistence now stores explicit opt-outs, so category lists expand safely as the tip taxonomy grows

## [1.2.0] - 2026-04-01

### Added

- Category-aware tip filtering with selectable categories in the settings UI

### Changed

- Switched settings category controls to a compact multi-column layout
- Indexed in-memory tip selection by category to avoid full-list filtering on every shown tip

## [1.1.0] - 2026-03-10

### Added

- Settings page for enabling startup tips and configuring periodic tip reminders
- Project-level periodic tip scheduler for showing tips on a recurring interval
- Dedicated notification service to prevent overlapping periodic tip balloons

### Changed

- Migrated the settings UI to Kotlin UI DSL
- Declared action update threads explicitly for IntelliJ platform compatibility
- Switched service-to-service dependencies to lazy lookups instead of constructor-time resolution
- Refined notification tracking and tests around visible versus stale tip balloons
- Standardized Find Action labels to `Vim Coach: Show Tip` and `Vim Coach: Refresh Tips`

## [1.0.1] - 2026-03-03

### Removed

- Removed the plugin's default keymap binding for `Show Vim Tip` to avoid conflicts.

## [0.3.0-beta] - 2026-02-18

### Improved

- updated tips

## [0.2.1-beta] - 2026-02-17

### Added

- Conditional downloads via GitHub API
- Automatic startup update checks

## [0.1.3-beta] - 2026-02-12

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Vim tip display system with notifications
- Keyboard shortcut (Shift+T, T) to show random Vim tips
- Startup activity to show tips when opening projects
- Comprehensive library of Vim commands and tips
- Support for remote tip sources
- Action to refetch tips from remote source for updating the local tip library

[Unreleased]: https://github.com/pooryam92/vim-coach/compare/1.4.1...HEAD
[1.4.1]: https://github.com/pooryam92/vim-coach/compare/1.4.0...1.4.1
[1.4.0]: https://github.com/pooryam92/vim-coach/compare/1.4.0-beta.2...1.4.0
[1.4.0-beta.2]: https://github.com/pooryam92/vim-coach/compare/1.4.0-beta.1...1.4.0-beta.2
[1.4.0-beta.1]: https://github.com/pooryam92/vim-coach/compare/1.3.0...1.4.0-beta.1
[1.3.0]: https://github.com/pooryam92/vim-coach/compare/1.2.2...1.3.0
[1.2.2]: https://github.com/pooryam92/vim-coach/compare/1.2.1...1.2.2
[1.2.1]: https://github.com/pooryam92/vim-coach/compare/1.2.0...1.2.1
[1.2.0]: https://github.com/pooryam92/vim-coach/compare/1.1.0...1.2.0
[1.2.0-beta]: https://github.com/pooryam92/vim-coach/compare/1.1.0...1.2.0-beta
[1.1.0]: https://github.com/pooryam92/vim-coach/compare/1.0.1...1.1.0
[1.1.0-beta]: https://github.com/pooryam92/vim-coach/compare/1.0.1...1.1.0-beta
[1.0.1]: https://github.com/pooryam92/vim-coach/compare/0.3.0-beta...1.0.1
[0.3.0-beta]: https://github.com/pooryam92/vim-coach/compare/0.2.1-beta...0.3.0-beta
[0.2.1-beta]: https://github.com/pooryam92/vim-coach/compare/0.1.3-beta...0.2.1-beta
[0.1.3-beta]: https://github.com/pooryam92/vim-coach/commits/0.1.3-beta
