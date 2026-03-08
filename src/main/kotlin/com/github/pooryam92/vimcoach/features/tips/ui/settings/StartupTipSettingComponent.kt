package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.ui.components.JBCheckBox
import javax.swing.JComponent

class StartupTipSettingComponent(
    private val settingsService: VimCoachSettingsService
) : VimCoachSettingComponent {

    private val checkBox = JBCheckBox(MyBundle.message("settingsShowTipOnStartup"))

    init {
        reset()
    }

    override fun component(): JComponent = checkBox

    override fun isModified(): Boolean {
        return checkBox.isSelected != settingsService.isShowTipsOnStartupEnabled()
    }

    override fun apply() {
        settingsService.setShowTipsOnStartupEnabled(checkBox.isSelected)
    }

    override fun reset() {
        checkBox.isSelected = settingsService.isShowTipsOnStartupEnabled()
    }
}
