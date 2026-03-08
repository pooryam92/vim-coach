package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JPanel

class VimCoachSettingsConfigurable : SearchableConfigurable {
    private var rootPanel: JPanel? = null
    private var startupSetting: VimCoachSettingComponent? = null
    private var periodicSetting: VimCoachSettingComponent? = null

    override fun getId(): String = ID

    override fun getDisplayName(): String = MyBundle.message("settingsDisplayName")

    override fun createComponent(): JComponent {
        val settingsService = settingsService()
        val startup = StartupTipSettingComponent(settingsService)
        val periodic = PeriodicTipsSettingComponent(settingsService)
        startupSetting = startup
        periodicSetting = periodic

        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            add(createContentPanel(startup.component(), periodic.component()), BorderLayout.NORTH)
        }.also { rootPanel = it }
    }

    override fun isModified(): Boolean {
        return (startupSetting?.isModified() ?: false) ||
            (periodicSetting?.isModified() ?: false)
    }

    override fun apply() {
        startupSetting?.apply()
        periodicSetting?.apply()
    }

    override fun reset() {
        startupSetting?.reset()
        periodicSetting?.reset()
    }

    override fun disposeUIResources() {
        rootPanel = null
        startupSetting = null
        periodicSetting = null
    }

    private fun settingsService(): VimCoachSettingsService = service()

    private fun createContentPanel(startupComponent: JComponent, periodicComponent: JComponent): JPanel {
        startupComponent.alignmentX = JComponent.LEFT_ALIGNMENT
        periodicComponent.alignmentX = JComponent.LEFT_ALIGNMENT

        return JPanel().apply {
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
            add(startupComponent)
            add(Box.createVerticalStrut(8))
            add(periodicComponent)
        }
    }

    private companion object {
        const val ID = "com.github.pooryam92.vimcoach.settings"
    }
}
