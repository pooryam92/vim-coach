package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

class IdeaVimPluginFindVimRc : FindIdeaVimRc {
    override fun findVimRc(): Path? {
        val home = System.getProperty("user.home") ?: return null

        for (name in listOf(".ideavimrc", "_ideavimrc")) {
            val candidate = Path(home, name)
            if (candidate.exists()) return candidate
        }

        val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
        val xdgConfig = if (xdgConfigHome.isNullOrEmpty()) {
            Path(home, ".config", "ideavim/ideavimrc")
        } else {
            // Mirror IdeaVIM: only expand ~/ or ~\ (not bare ~), preserve the separator via string concat
            val configHome = if (xdgConfigHome.startsWith("~/") || xdgConfigHome.startsWith("~\\")) {
                home + xdgConfigHome.substring(1)
            } else {
                xdgConfigHome
            }
            Path(configHome, "ideavim/ideavimrc")
        }
        return if (xdgConfig.exists()) xdgConfig else null
    }
}
