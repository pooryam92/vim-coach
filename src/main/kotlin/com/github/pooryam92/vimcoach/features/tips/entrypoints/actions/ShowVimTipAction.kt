package com.github.pooryam92.vimcoach.features.tips.entrypoints.actions

import com.github.pooryam92.vimcoach.features.tips.application.TipNotificationService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ShowVimTipAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        project.service<TipNotificationService>().showRandomTip()
    }
}
