package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import javax.swing.JComponent
import javax.swing.JCheckBox
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class VimCoachSettingsConfigurable : SearchableConfigurable {
    private var startupCheckBox: JCheckBox? = null
    private var periodicCheckBox: JCheckBox? = null
    private var intervalSpinner: JSpinner? = null

    override fun getId(): String = ID

    override fun getDisplayName(): String = MyBundle.message("settingsDisplayName")

    override fun createComponent(): JComponent {
        val periodicCheckBoxCell = arrayOfNulls<com.intellij.ui.dsl.builder.Cell<JCheckBox>>(1)

        return panel {
            row {
                startupCheckBox = checkBox(MyBundle.message("settingsShowTipOnStartup"))
                    .align(AlignX.LEFT)
                    .component
            }
            row {
                periodicCheckBox = checkBox(MyBundle.message("settingsEnableTipsEvery"))
                    .align(AlignX.LEFT)
                    .also { periodicCheckBoxCell[0] = it }
                    .component
                intervalSpinner = cell(
                    JSpinner(
                        SpinnerNumberModel(
                            settingsService().getTipIntervalHours(),
                            MIN_TIP_INTERVAL_HOURS,
                            MAX_TIP_INTERVAL_HOURS,
                            1
                        )
                    )
                )
                    .enabledIf(periodicCheckBoxCell[0]!!.selected)
                    .component
                label(MyBundle.message("settingsTipIntervalSuffix"))
            }
        }.also { reset() }
    }

    override fun isModified(): Boolean {
        val startupModified = startupCheckBox?.isSelected != settingsService().isShowTipsOnStartupEnabled()
        val periodicModified = periodicCheckBox?.isSelected != settingsService().isPeriodicTipsEnabled()
        val intervalModified = currentIntervalValue() != settingsService().getTipIntervalHours()
        return startupModified || periodicModified || intervalModified
    }

    override fun apply() {
        val settingsService = settingsService()
        val showOnStartup = startupCheckBox?.isSelected ?: return
        val periodicEnabled = periodicCheckBox?.isSelected ?: return
        val intervalHours = currentIntervalValue()

        settingsService.setShowTipsOnStartupEnabled(showOnStartup)
        if (periodicEnabled) {
            settingsService.setTipIntervalHours(intervalHours)
            settingsService.setPeriodicTipsEnabled(true)
        } else {
            settingsService.setPeriodicTipsEnabled(false)
            settingsService.setTipIntervalHours(intervalHours)
        }
    }

    override fun reset() {
        val settingsService = settingsService()
        startupCheckBox?.isSelected = settingsService.isShowTipsOnStartupEnabled()
        periodicCheckBox?.isSelected = settingsService.isPeriodicTipsEnabled()
        intervalSpinner?.value = settingsService.getTipIntervalHours()
    }

    override fun disposeUIResources() {
        startupCheckBox = null
        periodicCheckBox = null
        intervalSpinner = null
    }

    private fun settingsService(): VimCoachSettingsService = service()

    private fun currentIntervalValue(): Int {
        return (intervalSpinner?.value as? Number)?.toInt() ?: settingsService().getTipIntervalHours()
    }

    private companion object {
        const val ID = "com.github.pooryam92.vimcoach.settings"
        const val MIN_TIP_INTERVAL_HOURS = 1
        const val MAX_TIP_INTERVAL_HOURS = 24 * 7
    }
}
