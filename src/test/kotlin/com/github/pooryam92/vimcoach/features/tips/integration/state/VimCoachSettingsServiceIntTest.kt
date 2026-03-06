package com.github.pooryam92.vimcoach.features.tips.integration.state

import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimCoachSettingsServiceIntTest : BasePlatformTestCase() {

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

    fun testShowTipsOnStartupEnabledByDefault() {
        assertTrue(settingsService().isShowTipsOnStartupEnabled())
    }

    fun testSetShowTipsOnStartupEnabledUpdatesState() {
        settingsService().setShowTipsOnStartupEnabled(false)

        assertFalse(settingsService().isShowTipsOnStartupEnabled())
    }

    fun testLoadStateRestoresShowTipsOnStartupValue() {
        settingsService().loadState(VimCoachSettingsService.State(showTipsOnStartup = false))

        assertFalse(settingsService().isShowTipsOnStartupEnabled())
    }

    private fun settingsService(): VimCoachSettingsService = service()
}
