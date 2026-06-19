# Vim Coach

![Build](https://github.com/pooryam92/vim-coach/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/30148.svg)](https://plugins.jetbrains.com/plugin/30148)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/30148.svg)](https://plugins.jetbrains.com/plugin/30148)

An IntelliJ Platform plugin that helps you master Vim keybindings through interactive tips and coaching. Learn Vim commands while coding!

<!-- Plugin description -->
Vim Coach is an IntelliJ Platform plugin designed to help developers learn and master Vim keybindings. The plugin provides bite-sized Vim tips through notifications, making it easy to discover and practice new commands while you code. Whether you're a Vim beginner or looking to expand your knowledge, Vim Coach helps you level up your editing skills one tip at a time.

## Features

-  **Vim Tips**: Get Vim tips displayed as notifications
-  **On-Demand Action**: Run `Vim Coach: Show Tip` from Find Action (`Ctrl+Shift+A`)
-  **Startup Tips**: See a Vim tip when you start your IDE
-  **Periodic Tips**: Schedule reminder tips from the plugin settings
-  **Category Filters**: Enable or disable tip categories from the settings UI, with newly added categories enabled by default
-  **Tip Exclusions**: Exclude individual tips from notifications and restore them later from settings
-  **Add to .ideavimrc**: When IdeaVim is installed, tips that ship a mapping or command show an apply button that appends it to your `.ideavimrc`, opens it at the added lines, and offers one-click reload. No `.ideavimrc` yet? The button points you to IdeaVim's "Create ~/.ideavimrc" — Vim Coach never creates the file for you
-  **Settings UI**: Control startup tips, periodic reminders, active categories, and excluded tips from `Settings | Tools | Vim Coach`
-  **Comprehensive Library**: Hundreds of Vim commands covering navigation, editing, searching, and more
-  **Remote Updates**: Tips can be loaded from remote sources for fresh content

## Prerequisites

This plugin requires [IdeaVim](https://plugins.jetbrains.com/plugin/164-ideavim) to be installed.

## IdeaVim mapping with `<leader>`

If you use IdeaVim, you can map Vim Coach actions in your `~/.ideavimrc`:

```vim
let mapleader = " "
map <leader>tt <Action>(com.github.pooryam92.vimcoach.actions.ShowVimTipAction)
```

Vim Coach no longer installs a default key binding for `Vim Coach: Show Tip`, so custom IdeaVim mappings are the preferred way to add a shortcut without conflicting with existing IDE shortcuts.

<!-- Plugin description end -->

### Development: load tips from local file

Source selection is done with Gradle run tasks:

- `./gradlew runIde` (default remote mode)
- `./gradlew runIdeWithFileTips` (forces local file mode)
- `./gradlew runIdeWithMinuteTipSchedule` (keeps the setting UI in hours, but treats the configured value as minutes for faster testing)

In `file` mode, the loader uses only `vimcoach.tip.file.path` (set by the run task).

For periodic-tip testing, development mode also supports the JVM property `vimcoach.tip.interval.unit=minutes`.

To point the plugin at an alternative remote tip source (for example a beta branch) without rebuilding, set the `vimcoach.tip.remote.url` JVM option via `Help | Edit Custom VM Options` and restart the IDE. When unset, tips load from the default GitHub source.

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests.

## License

This plugin is based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
