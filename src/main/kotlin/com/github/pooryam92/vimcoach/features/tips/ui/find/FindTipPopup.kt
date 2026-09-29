package com.github.pooryam92.vimcoach.features.tips.ui.find

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.application.search.FindTipEntry
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory

/** What FindTipAction needs from the popup, so the action can be tested without showing one. */
internal interface FindTipView {
    fun show(project: Project, entries: List<FindTipEntry>, onChosen: (VimTip) -> Unit)
    fun showNoTipsLoaded(project: Project)
}

internal object FindTipPopup : FindTipView {

    override fun show(project: Project, entries: List<FindTipEntry>, onChosen: (VimTip) -> Unit) {
        create(entries, onChosen).showCenteredInCurrentWindow(project)
    }

    override fun showNoTipsLoaded(project: Project) {
        JBPopupFactory.getInstance()
            .createMessage(MyBundle.message("findTipNoTipsMessage"))
            .showCenteredInCurrentWindow(project)
    }

    internal fun create(entries: List<FindTipEntry>, onChosen: (VimTip) -> Unit): JBPopup {
        return JBPopupFactory.getInstance()
            .createPopupChooserBuilder(entries)
            .setTitle(MyBundle.message("findTipPopupTitle"))
            .setRenderer(FindTipRenderer())
            .setNamerForFiltering { it.searchText }
            .setFilterAlwaysVisible(true)
            .setItemChosenCallback { onChosen(it.tip) }
            .setMovable(true)
            .setResizable(true)
            .createPopup()
    }
}
