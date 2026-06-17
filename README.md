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
-  **Settings UI**: Control startup tips, periodic reminders, active categories, and excluded tips from `Settings | Tools | Vim Coach`
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
5. **Exclude Repeated Tips**: Use `Exclude tip` on a notification to hide that tip from future random selections. Restore excluded tips from `Settings | Tools | Vim Coach`.
6. **Browse Tips**: Each tip includes a summary and detailed explanation with examples
7. **Refresh Tips from Remote**: Use the `Vim Coach: Refresh Tips` action to update your tips library from the remote source

### IdeaVim mapping with `<leader>`

If you use IdeaVim, you can map Vim Coach actions in your `~/.ideavimrc`:

```vim
let mapleader = " "
map <leader>tt <Action>(com.github.pooryam92.vimcoach.actions.ShowVimTipAction)
```

Vim Coach no longer installs a default key binding for `Vim Coach: Show Tip`, so custom IdeaVim mappings are the preferred way to add a shortcut without conflicting with existing IDE shortcuts.

<!-- Plugin description end -->

## Covered IdeaVim plugins

Tips in the **plugins** category teach commands from these IdeaVim-emulated plugins. Each tip's
"Add to .ideavimrc" button installs the matching `Plug` line for you:

| Plugin | `.ideavimrc` | What the tips cover |
| --- | --- | --- |
| vim-surround | `Plug 'tpope/vim-surround'` | Add/change/delete surroundings, tags, repeats |
| vim-commentary | `Plug 'tpope/vim-commentary'` | Toggle comments with `gcc` / `gc` |
| vim-highlightedyank | `Plug 'machakann/vim-highlightedyank'` | Flash the yanked region |
| ReplaceWithRegister | `Plug 'vim-scripts/ReplaceWithRegister'` | Replace text with a register via `gr` |
| vim-exchange | `Plug 'tommcdo/vim-exchange'` | Swap two regions with `cx` |
| argtextobj | `Plug 'vim-scripts/argtextobj.vim'` | Argument text object `ia` / `aa` |
| vim-indent-object | `Plug 'michaeljsmith/vim-indent-object'` | Indent-block text object `ii` / `ai` |
| switch.vim | `Plug 'AndrewRadev/switch.vim'` | Toggle `true`/`false` and similar pairs with `gs` |
| vim-abolish | `Plug 'tpope/vim-abolish'` | Recase a word with `cr` coercions |
| vim-multiple-cursors | `Plug 'terryma/vim-multiple-cursors'` | Add cursors to matches |
| vim-textobj-entire | `Plug 'kana/vim-textobj-entire'` | Whole-buffer text object `ae` / `ie` |
| vim-sneak | `Plug 'justinmk/vim-sneak'` | Jump to any two characters |
| matchit | `Plug 'chrisbra/matchit'` | Extend `%` to tags and blocks |
| vim-paragraph-motion | `Plug 'dbakker/vim-paragraph-motion'` | `{` / `}` ignore whitespace-only lines |
| NERDTree | `Plug 'preservim/nerdtree'` | Browse the project tree |

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
