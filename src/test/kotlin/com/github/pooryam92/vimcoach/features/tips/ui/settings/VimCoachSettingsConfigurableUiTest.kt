package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.Container
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JSpinner

class VimCoachSettingsConfigurableUiTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        settingsService().setShowTipsOnStartupEnabled(true)
        settingsService().setPeriodicTipsEnabled(false)
        settingsService().setTipIntervalHours(1)
        settingsStore().loadState(VimCoachSettingsStore.State())
        tipService().saveTips(emptyList())
    }

    override fun tearDown() {
        try {
            settingsService().setShowTipsOnStartupEnabled(true)
            settingsService().setPeriodicTipsEnabled(false)
            settingsService().setTipIntervalHours(1)
            settingsStore().loadState(VimCoachSettingsStore.State())
            tipService().saveTips(emptyList())
        } finally {
            super.tearDown()
        }
    }

    fun testCreateComponentUsesPersistedStateAndLabel() {
        settingsService().setShowTipsOnStartupEnabled(false)
        settingsService().setPeriodicTipsEnabled(true)
        settingsService().setTipIntervalHours(6)
        val configurable = VimCoachSettingsConfigurable()

        try {
            val component = configurable.createComponent()
            val startupCheckbox = findCheckBox(component, MyBundle.message("settingsShowTipOnStartup"))
            val periodicCheckbox = findCheckBox(component, MyBundle.message("settingsEnableTipsEvery"))
            val spinner = findSpinner(component)

            assertFalse(startupCheckbox.isSelected)
            assertTrue(periodicCheckbox.isSelected)
            assertEquals(6, spinner.value)
            assertTrue(spinner.isEnabled)
            assertFalse(configurable.isModified())
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testApplyPersistsCheckboxSelection() {
        val configurable = VimCoachSettingsConfigurable()

        try {
            val component = configurable.createComponent()
            val startupCheckbox = findCheckBox(component, MyBundle.message("settingsShowTipOnStartup"))
            val periodicCheckbox = findCheckBox(component, MyBundle.message("settingsEnableTipsEvery"))
            val spinner = findSpinner(component)

            startupCheckbox.isSelected = false
            periodicCheckbox.isSelected = true
            spinner.value = 10
            assertTrue(configurable.isModified())

            configurable.apply()

            assertFalse(settingsService().isShowTipsOnStartupEnabled())
            assertTrue(settingsService().isPeriodicTipsEnabled())
            assertEquals(10, settingsService().getTipIntervalHours())
            assertFalse(configurable.isModified())
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testCreateComponentShowsCategoryCheckboxes() {
        tipService().saveTips(
            listOf(
                VimTip("summary-1", listOf("details-1"), listOf("basics", "editing")),
                VimTip("summary-2", listOf("details-2"), listOf("search", "basics"))
            )
        )
        val configurable = VimCoachSettingsConfigurable()

        try {
            val component = configurable.createComponent()
            val basicsCheckBox = findCheckBox(component, "basics")
            val editingCheckBox = findCheckBox(component, "editing")
            val searchCheckBox = findCheckBox(component, "search")

            assertTrue(basicsCheckBox.isSelected)
            assertTrue(editingCheckBox.isSelected)
            assertTrue(searchCheckBox.isSelected)
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testApplyPersistsCategorySelection() {
        tipService().saveTips(
            listOf(
                VimTip("summary-1", listOf("details-1"), listOf("basics", "editing")),
                VimTip("summary-2", listOf("details-2"), listOf("search"))
            )
        )
        val configurable = VimCoachSettingsConfigurable()

        try {
            val component = configurable.createComponent()
            val basicsCheckBox = findCheckBox(component, "basics")
            val editingCheckBox = findCheckBox(component, "editing")
            val searchCheckBox = findCheckBox(component, "search")

            basicsCheckBox.isSelected = true
            editingCheckBox.isSelected = false
            searchCheckBox.isSelected = true

            assertTrue(configurable.isModified())

            configurable.apply()

            assertEquals(
                listOf("basics", "search"),
                settingsService().getEnabledTipCategories(listOf("basics", "editing", "search"))
            )
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testCategoryToggleButtonSelectsAndDeselectsAllCategories() {
        tipService().saveTips(
            listOf(
                VimTip("summary-1", listOf("details-1"), listOf("basics")),
                VimTip("summary-2", listOf("details-2"), listOf("editing")),
                VimTip("summary-3", listOf("details-3"), listOf("search"))
            )
        )
        val configurable = VimCoachSettingsConfigurable()

        try {
            val component = configurable.createComponent()
            val basicsCheckBox = findCheckBox(component, "basics")
            val editingCheckBox = findCheckBox(component, "editing")
            val searchCheckBox = findCheckBox(component, "search")
            val deselectAllButton = findButton(component, MyBundle.message("settingsDeselectAllCategories"))

            deselectAllButton.doClick()

            assertFalse(basicsCheckBox.isSelected)
            assertFalse(editingCheckBox.isSelected)
            assertFalse(searchCheckBox.isSelected)
            assertEquals(MyBundle.message("settingsSelectAllCategories"), deselectAllButton.text)
            assertTrue(configurable.isModified())

            deselectAllButton.doClick()

            assertTrue(basicsCheckBox.isSelected)
            assertTrue(editingCheckBox.isSelected)
            assertTrue(searchCheckBox.isSelected)
            assertEquals(MyBundle.message("settingsDeselectAllCategories"), deselectAllButton.text)
            assertFalse(configurable.isModified())
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testCreateComponentSelectsNewCategoriesByDefault() {
        settingsService().setEnabledTipCategories(
            availableCategories = listOf("basics", "editing"),
            enabledCategories = listOf("editing")
        )
        tipService().saveTips(
            listOf(
                VimTip("summary-1", listOf("details-1"), listOf("basics", "editing")),
                VimTip("summary-2", listOf("details-2"), listOf("search"))
            )
        )
        val configurable = VimCoachSettingsConfigurable()

        try {
            val component = configurable.createComponent()
            val basicsCheckBox = findCheckBox(component, "basics")
            val editingCheckBox = findCheckBox(component, "editing")
            val searchCheckBox = findCheckBox(component, "search")

            assertFalse(basicsCheckBox.isSelected)
            assertTrue(editingCheckBox.isSelected)
            assertTrue(searchCheckBox.isSelected)
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testResetRestoresPersistedValue() {
        settingsService().setShowTipsOnStartupEnabled(false)
        settingsService().setPeriodicTipsEnabled(false)
        settingsService().setTipIntervalHours(8)
        val configurable = VimCoachSettingsConfigurable()

        try {
            val component = configurable.createComponent()
            val startupCheckbox = findCheckBox(component, MyBundle.message("settingsShowTipOnStartup"))
            val periodicCheckbox = findCheckBox(component, MyBundle.message("settingsEnableTipsEvery"))
            val spinner = findSpinner(component)

            startupCheckbox.isSelected = true
            periodicCheckbox.isSelected = true
            spinner.value = 14
            assertTrue(configurable.isModified())

            configurable.reset()

            assertFalse(startupCheckbox.isSelected)
            assertFalse(periodicCheckbox.isSelected)
            assertEquals(8, spinner.value)
            assertFalse(spinner.isEnabled)
            assertFalse(configurable.isModified())
        } finally {
            configurable.disposeUIResources()
        }
    }

    private fun settingsService(): VimCoachSettingsService {
        return service()
    }

    private fun tipService(): VimTipService {
        return service()
    }

    private fun settingsStore(): VimCoachSettingsStore {
        return service()
    }

    private fun findCheckBox(root: JComponent, text: String): JCheckBox {
        val queue = ArrayDeque<Container>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (child in current.components) {
                if (child is JCheckBox && child.text == text) {
                    return child
                }
                if (child is Container) {
                    queue.add(child)
                }
            }
        }

        fail("Expected checkbox with text '$text' in settings UI")
        throw IllegalStateException("Unreachable")
    }

    private fun findButton(root: JComponent, text: String): JButton {
        val queue = ArrayDeque<Container>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (child in current.components) {
                if (child is JButton && child.text == text) {
                    return child
                }
                if (child is Container) {
                    queue.add(child)
                }
            }
        }

        fail("Expected button with text '$text' in settings UI")
        throw IllegalStateException("Unreachable")
    }

    private fun findSpinner(root: JComponent): JSpinner {
        val queue = ArrayDeque<Container>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (child in current.components) {
                if (child is JSpinner) {
                    return child
                }
                if (child is Container) {
                    queue.add(child)
                }
            }
        }

        fail("Expected a tip-interval spinner in settings UI")
        throw IllegalStateException("Unreachable")
    }

}
