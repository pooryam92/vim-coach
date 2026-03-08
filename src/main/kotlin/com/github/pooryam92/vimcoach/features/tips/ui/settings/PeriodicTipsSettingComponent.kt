package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.ui.components.JBCheckBox
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class PeriodicTipsSettingComponent(
    private val settingsService: VimCoachSettingsService
) : VimCoachSettingComponent {

    private val checkBox = JBCheckBox(MyBundle.message("settingsEnableTipsEvery"))
    private val intervalSpinner = JSpinner(
        SpinnerNumberModel(
            settingsService.getTipIntervalHours(),
            MIN_TIP_INTERVAL_HOURS,
            MAX_TIP_INTERVAL_HOURS,
            1
        )
    )

    private val panel = createComponentPanel()

    init {
        reset()
        checkBox.addActionListener { updateIntervalControlState() }
    }

    override fun component(): JComponent = panel

    override fun isModified(): Boolean {
        return checkBox.isSelected != settingsService.isPeriodicTipsEnabled() ||
            spinnerValue() != settingsService.getTipIntervalHours()
    }

    override fun apply() {
        settingsService.setPeriodicTipsEnabled(checkBox.isSelected)
        settingsService.setTipIntervalHours(spinnerValue())
    }

    override fun reset() {
        checkBox.isSelected = settingsService.isPeriodicTipsEnabled()
        intervalSpinner.value = settingsService.getTipIntervalHours()
        updateIntervalControlState()
    }

    private fun createComponentPanel(): JPanel {
        val suffixLabel = JLabel(MyBundle.message("settingsTipIntervalSuffix")).apply {
            alignmentY = JComponent.CENTER_ALIGNMENT
        }
        checkBox.alignmentY = JComponent.CENTER_ALIGNMENT
        intervalSpinner.alignmentY = JComponent.CENTER_ALIGNMENT

        return JPanel().apply {
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS)
            add(checkBox)
            add(Box.createHorizontalStrut(8))
            add(intervalSpinner)
            add(Box.createHorizontalStrut(8))
            add(suffixLabel)
        }
    }

    private fun updateIntervalControlState() {
        intervalSpinner.isEnabled = checkBox.isSelected
    }

    private fun spinnerValue(): Int {
        return (intervalSpinner.value as Number).toInt()
    }

    private companion object {
        const val MIN_TIP_INTERVAL_HOURS = 1
        const val MAX_TIP_INTERVAL_HOURS = 24 * 7
    }
}
