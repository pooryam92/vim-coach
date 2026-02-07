package com.github.pooryam92.plugin.actions

import com.github.pooryam92.plugin.notifications.VimTipNotifier
import com.github.pooryam92.plugin.services.VimTipProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ShowVimTipAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val notifier = VimTipNotifier(project.service<VimTipProvider>())
        notifier.showRandomTip(project)
    }
}
