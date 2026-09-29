package com.github.pooryam92.vimcoach.features.tips.entrypoints.notifications

import com.github.pooryam92.vimcoach.features.tips.application.notifications.ShowTips
import com.github.pooryam92.vimcoach.features.tips.application.search.FindTips
import com.github.pooryam92.vimcoach.features.tips.ui.find.FindTipPopup
import com.github.pooryam92.vimcoach.features.tips.ui.find.FindTipView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

class FindTipAction internal constructor(
    private val findTips: () -> FindTips,
    private val view: FindTipView,
    private val showTips: (Project) -> ShowTips,
) : AnAction() {

    constructor() : this(
        findTips = { FindTips() },
        view = FindTipPopup,
        showTips = { project -> project.service() },
    )

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val entries = findTips().entries()
        thisLogger().debug("Find Tip opened with ${entries.size} tips")

        if (entries.isEmpty()) {
            view.showNoTipsLoaded(project)
            return
        }
        view.show(project, entries) { tip -> showTips(project).showTip(tip) }
    }
}
