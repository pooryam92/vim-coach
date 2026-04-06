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
-  **Settings UI**: Control startup tips, periodic reminders, and active categories from `Settings | Tools | Vim Coach`
-  **Comprehensive Library**: Hundreds of Vim commands covering navigation, editing, searching, and more
-  **Remote Updates**: Tips can be loaded from remote sources for fresh content

## Prerequisites

This plugin requires [IdeaVim](https://plugins.jetbrains.com/plugin/164-ideavim) to be installed.

## Usage

Once installed, you can:

1. **Show a Vim Tip on Demand**: Run `Vim Coach: Show Tip` from Find Action (`Ctrl+Shift+A`)
2. **Learn at Startup**: Tips will appear automatically when you open a project 
3. **Schedule Periodic Tips**: Open `Settings | Tools | Vim Coach` and enable recurring reminders
4. **Filter by Category**: Use the category checkboxes in `Settings | Tools | Vim Coach` to focus on the types of tips you want to practice. When refreshed tips introduce new categories, they start enabled unless you explicitly turn them off.
5. **Browse Tips**: Each tip includes a summary and detailed explanation with examples
6. **Refresh Tips from Remote**: Use the `Vim Coach: Refresh Tips` action to update your tips library from the remote source

### IdeaVim mapping with `<leader>`

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

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests.

## License

This plugin is based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
