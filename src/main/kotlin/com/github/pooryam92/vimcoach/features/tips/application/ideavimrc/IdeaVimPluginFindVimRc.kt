package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import com.maddyhome.idea.vim.vimscript.services.VimRcService
import java.nio.file.Path

class IdeaVimPluginFindVimRc : FindIdeaVimRc {
    override fun findVimRc(): Path? = VimRcService.findIdeaVimRc()
}
