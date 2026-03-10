<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Vim Coach Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/pooryam92/vim-coach/compare/1.1.0...HEAD
[1.1.0]: https://github.com/pooryam92/vim-coach/compare/1.0.1...1.1.0
[1.1.0-beta]: https://github.com/pooryam92/vim-coach/compare/1.0.1...1.1.0-beta
[1.0.1]: https://github.com/pooryam92/vim-coach/compare/0.3.0-beta...1.0.1
[0.3.0-beta]: https://github.com/pooryam92/vim-coach/compare/0.2.1-beta...0.3.0-beta
[0.2.1-beta]: https://github.com/pooryam92/vim-coach/compare/0.1.3-beta...0.2.1-beta
[0.1.3-beta]: https://github.com/pooryam92/vim-coach/commits/0.1.3-beta
