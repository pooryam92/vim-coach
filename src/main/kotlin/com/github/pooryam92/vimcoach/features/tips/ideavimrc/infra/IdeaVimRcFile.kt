package com.github.pooryam92.vimcoach.features.tips.ideavimrc.infra

import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Locates (or creates) the user's .ideavimrc and appends config lines to it.
 *
 * The search order mirrors IdeaVim's own VimRcService: ~/.ideavimrc, ~/_ideavimrc, then the
 * XDG path $XDG_CONFIG_HOME/ideavim/ideavimrc (defaulting to ~/.config/ideavim/ideavimrc).
 * IdeaVim is only a reference submodule here, not a runtime dependency, so the logic is
 * reimplemented rather than called.
 *
 * Home and XDG roots are constructor parameters so tests can point at a temp directory.
 */
class IdeaVimRcFile(
    private val userHome: String? = System.getProperty("user.home"),
    private val xdgConfigHome: String? = System.getenv("XDG_CONFIG_HOME")
) {
    fun findOrCreate(): Path? {
        find()?.let { return it }
        return create()
    }

    fun find(): Path? {
        val home = userHome
        if (home != null) {
            for (name in HOME_VIMRC_NAMES) {
                val file = Path(home, name)
                if (file.exists()) {
                    return file
                }
            }
        }
        return xdgVimRcPath()?.takeIf { it.exists() }
    }

    /**
     * Appends [lines] to [path], skipping any that already appear verbatim (after trim).
     * Note: dedup is exact-match only and does not recognize that e.g. `set surround` and
     * `Plug 'tpope/vim-surround'` enable the same plugin.
     */
    fun append(path: Path, lines: List<String>): AppendOutcome {
        val cleaned = lines.map(String::trim).filter(String::isNotEmpty)
        if (cleaned.isEmpty()) {
            return AppendOutcome(path, addedLines = emptyList(), alreadyPresentLines = emptyList())
        }

        val existing = if (path.exists()) path.readText() else ""
        val existingLines = existing.lineSequence().map(String::trim).toHashSet()

        val added = mutableListOf<String>()
        val alreadyPresent = mutableListOf<String>()
        for (line in cleaned) {
            if (line in existingLines || line in added) {
                alreadyPresent.add(line)
            } else {
                added.add(line)
            }
        }

        if (added.isEmpty()) {
            return AppendOutcome(path, addedLines = added, alreadyPresentLines = alreadyPresent)
        }

        // 0-based index of the line the first appended line will occupy once written. Counting
        // '\n' matches the editor Document's notion of lines (it normalizes \r\n to one line),
        // and a missing trailing newline shifts the first appended line down by one.
        val addedStartLine = when {
            existing.isEmpty() -> 0
            else -> existing.count { it == '\n' } + if (existing.endsWith("\n")) 0 else 1
        }

        val builder = StringBuilder(existing)
        if (existing.isNotEmpty() && !existing.endsWith("\n")) {
            builder.append(LINE_SEPARATOR)
        }
        for (line in added) {
            builder.append(line).append(LINE_SEPARATOR)
        }
        path.writeText(builder.toString())

        return AppendOutcome(
            path,
            addedLines = added,
            alreadyPresentLines = alreadyPresent,
            addedStartLine = addedStartLine
        )
    }

    private fun create(): Path? {
        val home = userHome ?: return null
        for (name in HOME_VIMRC_NAMES) {
            try {
                val file = Path(home, name)
                file.createFile()
                return file
            } catch (ignored: IOException) {
                // Try the next candidate name.
            }
        }
        return null
    }

    private fun xdgVimRcPath(): Path? {
        val xdg = xdgConfigHome
        if (!xdg.isNullOrEmpty()) {
            val base = if (xdg.startsWith("~/") || xdg.startsWith("~\\")) {
                val home = userHome ?: return null
                home + xdg.substring(1)
            } else {
                xdg
            }
            return Path(base, XDG_VIMRC_RELATIVE)
        }
        val home = userHome ?: return null
        return Path(home, ".config", XDG_VIMRC_RELATIVE)
    }

    companion object {
        private const val VIMRC_FILE_NAME = "ideavimrc"
        private val HOME_VIMRC_NAMES = arrayOf(".$VIMRC_FILE_NAME", "_$VIMRC_FILE_NAME")
        private const val XDG_VIMRC_RELATIVE = "ideavim/$VIMRC_FILE_NAME"
        private val LINE_SEPARATOR: String = System.lineSeparator()
    }
}

data class AppendOutcome(
    val path: Path,
    val addedLines: List<String>,
    val alreadyPresentLines: List<String>,
    /** 0-based line index of the first appended line, or -1 when nothing was appended. */
    val addedStartLine: Int = -1
) {
    val addedSomething: Boolean get() = addedLines.isNotEmpty()
}
