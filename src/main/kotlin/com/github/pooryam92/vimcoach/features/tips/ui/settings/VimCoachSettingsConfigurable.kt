package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenService
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenState
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class VimCoachSettingsConfigurable : SearchableConfigurable {
    private var startupCheckBox: JCheckBox? = null
    private var periodicCheckBox: JCheckBox? = null
    private var intervalSpinner: JSpinner? = null
    private var categoryToggleButton: JButton? = null
    private val categoryCheckBoxes = linkedMapOf<String, JCheckBox>()
    private var screenState: VimCoachSettingsScreenState = defaultScreenState()

    override fun getId(): String = ID

    override fun getDisplayName(): String = MyBundle.message("settingsDisplayName")

    override fun createComponent(): JComponent {
        screenState = settingsScreenService().loadState()

        return panel {
            buildStartupRow()
            buildPeriodicRow()
            buildCategoriesSection(screenState.availableCategories)
        }.also { reset() }
    }

    override fun isModified(): Boolean {
        return currentScreenState() != screenState
    }

    override fun apply() {
        screenState = currentScreenState()
        settingsScreenService().saveState(screenState)
    }

    override fun reset() {
        screenState = settingsScreenService().loadState()
        startupCheckBox?.isSelected = screenState.showTipsOnStartup
        periodicCheckBox?.isSelected = screenState.periodicTipsEnabled
        intervalSpinner?.value = screenState.tipIntervalHours

        val enabledCategories = screenState.enabledCategories.toSet()
        categoryCheckBoxes.forEach { (category, checkBox) ->
            checkBox.isSelected = category in enabledCategories
        }
        updateCategoryToggleButtonText()
    }

    override fun disposeUIResources() {
        startupCheckBox = null
        periodicCheckBox = null
        intervalSpinner = null
        categoryToggleButton = null
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
            categoryToggleButton = button(categoryToggleButtonText()) {
                setAllCategoriesSelected(!areAllCategoriesSelected())
            }.component
        }

        row {
            cell(createCategoryScrollPane(categories))
                .align(AlignX.FILL)
        }
    }

    private fun createIntervalSpinner(): JSpinner {
        return JSpinner(
            SpinnerNumberModel(
                screenState.tipIntervalHours,
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
        return JCheckBox(category).also {
            it.addItemListener { updateCategoryToggleButtonText() }
            categoryCheckBoxes[category] = it
        }
    }

    private fun setAllCategoriesSelected(selected: Boolean) {
        categoryCheckBoxes.values.forEach { it.isSelected = selected }
        updateCategoryToggleButtonText()
    }

    private fun updateCategoryToggleButtonText() {
        categoryToggleButton?.text = categoryToggleButtonText()
    }

    private fun categoryToggleButtonText(): String {
        val messageKey = if (areAllCategoriesSelected()) {
            "settingsDeselectAllCategories"
        } else {
            "settingsSelectAllCategories"
        }
        return MyBundle.message(messageKey)
    }

    private fun areAllCategoriesSelected(): Boolean {
        return categoryCheckBoxes.isNotEmpty() && categoryCheckBoxes.values.all { it.isSelected }
    }

    private fun defaultScreenState(): VimCoachSettingsScreenState {
        return VimCoachSettingsScreenState(
            showTipsOnStartup = true,
            periodicTipsEnabled = false,
            tipIntervalHours = MIN_TIP_INTERVAL_HOURS,
            availableCategories = emptyList(),
            enabledCategories = emptyList()
        )
    }

    private fun currentScreenState(): VimCoachSettingsScreenState {
        return VimCoachSettingsScreenState(
            showTipsOnStartup = startupCheckBox?.isSelected ?: screenState.showTipsOnStartup,
            periodicTipsEnabled = periodicCheckBox?.isSelected ?: screenState.periodicTipsEnabled,
            tipIntervalHours = currentIntervalValue(),
            availableCategories = availableCategories(),
            enabledCategories = selectedCategories()
        )
    }

    private fun settingsScreenService(): VimCoachSettingsScreenService = service()

    private fun currentIntervalValue(): Int {
        return (intervalSpinner?.value as? Number)?.toInt() ?: screenState.tipIntervalHours
    }

    private fun availableCategories(): List<String> = categoryCheckBoxes.keys.toList()

    private fun selectedCategories(): List<String> = availableCategories().filter { categoryCheckBoxes[it]?.isSelected == true }

    private companion object {
        const val ID = "com.github.pooryam92.vimcoach.settings"
        const val MIN_TIP_INTERVAL_HOURS = 1
        const val MAX_TIP_INTERVAL_HOURS = 24 * 7
        const val CATEGORY_COLUMN_COUNT = 3
        const val CATEGORY_H_GAP = 12
        const val CATEGORY_V_GAP = 6
    }
}
