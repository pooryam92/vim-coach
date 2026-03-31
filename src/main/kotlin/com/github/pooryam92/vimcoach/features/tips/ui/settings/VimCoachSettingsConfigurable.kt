package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class VimCoachSettingsConfigurable : SearchableConfigurable {
    private var startupCheckBox: JCheckBox? = null
    private var periodicCheckBox: JCheckBox? = null
    private var intervalSpinner: JSpinner? = null
    private val categoryCheckBoxes = linkedMapOf<String, JCheckBox>()

    override fun getId(): String = ID

    override fun getDisplayName(): String = MyBundle.message("settingsDisplayName")

    override fun createComponent(): JComponent {
        return panel {
            buildStartupRow()
            buildPeriodicRow()
            buildCategoriesSection(tipService().getCategories().values)
        }.also { reset() }
    }

    override fun isModified(): Boolean {
        val startupModified = startupCheckBox?.isSelected != settingsService().isShowTipsOnStartupEnabled()
        val periodicModified = periodicCheckBox?.isSelected != settingsService().isPeriodicTipsEnabled()
        val intervalModified = currentIntervalValue() != settingsService().getTipIntervalHours()
        val categoriesModified = selectedCategories() != settingsService().getEnabledTipCategories(availableCategories())
        return startupModified || periodicModified || intervalModified || categoriesModified
    }

    override fun apply() {
        val settingsService = settingsService()
        val showOnStartup = startupCheckBox?.isSelected ?: return
        val periodicEnabled = periodicCheckBox?.isSelected ?: return
        val intervalHours = currentIntervalValue()

        settingsService.setShowTipsOnStartupEnabled(showOnStartup)
        settingsService.setTipIntervalHours(intervalHours)
        settingsService.setPeriodicTipsEnabled(periodicEnabled)
        settingsService.setEnabledTipCategories(selectedCategories())
    }

    override fun reset() {
        val settingsService = settingsService()
        startupCheckBox?.isSelected = settingsService.isShowTipsOnStartupEnabled()
        periodicCheckBox?.isSelected = settingsService.isPeriodicTipsEnabled()
        intervalSpinner?.value = settingsService.getTipIntervalHours()

        val enabledCategories = settingsService.getEnabledTipCategories(availableCategories()).toSet()
        categoryCheckBoxes.forEach { (category, checkBox) ->
            checkBox.isSelected = category in enabledCategories
        }
    }

    override fun disposeUIResources() {
        startupCheckBox = null
        periodicCheckBox = null
        intervalSpinner = null
        categoryCheckBoxes.clear()
    }

    private fun Panel.buildStartupRow() {
        row {
            startupCheckBox = checkBox(MyBundle.message("settingsShowTipOnStartup"))
                .align(AlignX.LEFT)
                .component
        }
    }

    private fun Panel.buildPeriodicRow() {
        row {
            val periodicCell = checkBox(MyBundle.message("settingsEnableTipsEvery"))
                .align(AlignX.LEFT)

            periodicCheckBox = periodicCell.component
            intervalSpinner = cell(createIntervalSpinner())
                .enabledIf(periodicCell.selected)
                .component
            label(MyBundle.message("settingsTipIntervalSuffix"))
        }
    }

    private fun Panel.buildCategoriesSection(categories: List<String>) {
        row {
            label(MyBundle.message("settingsCategoriesLabel"))
        }

        if (categories.isEmpty()) {
            row {
                label(MyBundle.message("settingsNoCategories"))
            }
            return
        }

        row {
            cell(createCategoryScrollPane(categories))
                .align(AlignX.FILL)
        }
    }

    private fun createIntervalSpinner(): JSpinner {
        return JSpinner(
            SpinnerNumberModel(
                settingsService().getTipIntervalHours(),
                MIN_TIP_INTERVAL_HOURS,
                MAX_TIP_INTERVAL_HOURS,
                1
            )
        )
    }

    private fun createCategoryScrollPane(categories: List<String>): JBScrollPane {
        return JBScrollPane(createCategoryPanel(categories)).apply {
            border = null
        }
    }

    private fun createCategoryPanel(categories: List<String>): JPanel {
        categoryCheckBoxes.clear()

        return JPanel(GridLayout(0, CATEGORY_COLUMN_COUNT, CATEGORY_H_GAP, CATEGORY_V_GAP)).apply {
            isOpaque = false
            categories.forEach { add(createCategoryCheckBox(it)) }
        }
    }

    private fun createCategoryCheckBox(category: String): JCheckBox {
        return JCheckBox(category).also { categoryCheckBoxes[category] = it }
    }

    private fun settingsService(): VimCoachSettingsService = service()

    private fun tipService(): VimTipService = service()

    private fun currentIntervalValue(): Int {
        return (intervalSpinner?.value as? Number)?.toInt() ?: settingsService().getTipIntervalHours()
    }

    private fun availableCategories(): List<String> {
        return categoryCheckBoxes.keys.toList()
    }

    private fun selectedCategories(): List<String> {
        return availableCategories().filter { categoryCheckBoxes[it]?.isSelected == true }
    }

    private companion object {
        const val ID = "com.github.pooryam92.vimcoach.settings"
        const val MIN_TIP_INTERVAL_HOURS = 1
        const val MAX_TIP_INTERVAL_HOURS = 24 * 7
        const val CATEGORY_COLUMN_COUNT = 3
        const val CATEGORY_H_GAP = 12
        const val CATEGORY_V_GAP = 6
    }
}
