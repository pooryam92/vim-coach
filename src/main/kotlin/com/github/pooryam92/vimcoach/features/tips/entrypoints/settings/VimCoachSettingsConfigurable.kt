package com.github.pooryam92.vimcoach.features.tips.entrypoints.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class VimCoachSettingsConfigurable : SearchableConfigurable {
    private var panel: JPanel? = null
    private var showTipsOnStartupCheckBox: JBCheckBox? = null

    override fun getId(): String {
        return ID
    }

    override fun getDisplayName(): String {
        return MyBundle.message("settingsDisplayName")
    }

    override fun createComponent(): JComponent {
        val checkbox = JBCheckBox(MyBundle.message("settingsShowTipOnStartup"))
        checkbox.isSelected = settingsService().isShowTipsOnStartupEnabled()
        showTipsOnStartupCheckBox = checkbox

        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            add(checkbox, BorderLayout.NORTH)
        }.also { panel = it }
    }

    override fun isModified(): Boolean {
        val checkbox = showTipsOnStartupCheckBox ?: return false
        return checkbox.isSelected != settingsService().isShowTipsOnStartupEnabled()
    }

    override fun apply() {
        val checkbox = showTipsOnStartupCheckBox ?: return
        settingsService().setShowTipsOnStartupEnabled(checkbox.isSelected)
    }

    override fun reset() {
        val checkbox = showTipsOnStartupCheckBox ?: return
        checkbox.isSelected = settingsService().isShowTipsOnStartupEnabled()
    }

    override fun disposeUIResources() {
        panel = null
        showTipsOnStartupCheckBox = null
    }

    private fun settingsService(): VimCoachSettingsService {
        return service()
    }

    private companion object {
        const val ID = "com.github.pooryam92.vimcoach.settings"
    }
}
