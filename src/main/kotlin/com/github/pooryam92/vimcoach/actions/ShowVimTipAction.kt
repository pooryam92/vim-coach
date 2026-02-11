package com.github.pooryam92.vimcoach.actions

import com.github.pooryam92.vimcoach.notifications.VimTipNotifier
import com.github.pooryam92.vimcoach.services.VimTipService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ShowVimTipAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val notifier = VimTipNotifier(project.service<VimTipService>())
        notifier.showRandomTip(project)
    }
}
