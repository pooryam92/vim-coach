package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.entrypoints.settings.VimCoachSettingsConfigurable
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.Container
import javax.swing.JCheckBox
import javax.swing.JComponent

class VimCoachSettingsConfigurableUiTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        settingsService().setShowTipsOnStartupEnabled(true)
    }

    override fun tearDown() {
        try {
            settingsService().setShowTipsOnStartupEnabled(true)
        } finally {
            super.tearDown()
        }
    }

    fun testCreateComponentUsesPersistedStateAndLabel() {
        settingsService().setShowTipsOnStartupEnabled(false)
        val configurable = VimCoachSettingsConfigurable()

        try {
            val checkbox = findCheckBox(configurable.createComponent())

            assertEquals(MyBundle.message("settingsShowTipOnStartup"), checkbox.text)
            assertFalse(checkbox.isSelected)
            assertFalse(configurable.isModified())
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testApplyPersistsCheckboxSelection() {
        val configurable = VimCoachSettingsConfigurable()

        try {
            val checkbox = findCheckBox(configurable.createComponent())

            checkbox.isSelected = false
            assertTrue(configurable.isModified())

            configurable.apply()

            assertFalse(settingsService().isShowTipsOnStartupEnabled())
            assertFalse(configurable.isModified())
        } finally {
            configurable.disposeUIResources()
        }
    }

    fun testResetRestoresPersistedValue() {
        settingsService().setShowTipsOnStartupEnabled(false)
        val configurable = VimCoachSettingsConfigurable()

        try {
            val checkbox = findCheckBox(configurable.createComponent())

            checkbox.isSelected = true
            assertTrue(configurable.isModified())

            configurable.reset()

            assertFalse(checkbox.isSelected)
            assertFalse(configurable.isModified())
        } finally {
            configurable.disposeUIResources()
        }
    }

    private fun settingsService(): VimCoachSettingsService {
        return service()
    }

    private fun findCheckBox(root: JComponent): JCheckBox {
        val queue = ArrayDeque<Container>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (child in current.components) {
                if (child is JCheckBox) {
                    return child
                }
                if (child is Container) {
                    queue.add(child)
                }
            }
        }

        fail("Expected a startup-tip checkbox in settings UI")
        throw IllegalStateException("Unreachable")
    }
}
