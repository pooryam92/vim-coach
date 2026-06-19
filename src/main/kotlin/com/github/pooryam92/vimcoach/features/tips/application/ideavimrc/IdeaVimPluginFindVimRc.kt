package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * Locates the user's .ideavimrc using the same search order as IdeaVim's VimRcService:
 *   1. ~/.ideavimrc
 *   2. ~/_ideavimrc
 *   3. $XDG_CONFIG_HOME/ideavim/ideavimrc (defaults to ~/.config/ideavim/ideavimrc)
 *
 * Reimplements rather than delegates to VimRcService — that class is an internal IdeaVim
 * detail with no stability contract. The external/ideavim submodule is the reference to
 * check for drift.
 *
 */
class IdeaVimPluginFindVimRc(
    private val home: () -> String? = { System.getProperty("user.home") },
    private val xdgConfigHome: () -> String? = { System.getenv("XDG_CONFIG_HOME") },
    private val fileExists: (Path) -> Boolean = { it.exists() }
) : FindIdeaVimRc {

    override fun findVimRc(): Path? {
        val home = home() ?: return null
        return findDotFile(home) ?: findXdgFile(home)
    }

    private fun findDotFile(home: String): Path? =
        listOf(".ideavimrc", "_ideavimrc")
            .map { Path(home, it) }
            .firstOrNull(fileExists)

    private fun findXdgFile(home: String): Path? {
        val path = xdgPath(home)
        return if (fileExists(path)) path else null
    }

    private fun xdgPath(home: String): Path {
        val configBase = xdgConfigHome()
            ?.takeIf { it.isNotEmpty() }
            ?.let { Path(expandTilde(it, home)) }
            ?: Path(home, ".config")
        return configBase.resolve("ideavim/ideavimrc")
    }

    // Mirror IdeaVIM: only expand ~/ or ~\ (not bare ~)
    private fun expandTilde(path: String, home: String): String =
        if (path.startsWith("~/") || path.startsWith("~\\"))
            home + path.substring(1)
        else
            path
}
