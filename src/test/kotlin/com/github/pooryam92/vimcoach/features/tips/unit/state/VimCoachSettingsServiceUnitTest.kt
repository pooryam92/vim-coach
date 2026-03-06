package com.github.pooryam92.vimcoach.features.tips.unit.state

import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsServiceImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VimCoachSettingsServiceUnitTest {

    @Test
    fun showTipsOnStartupIsEnabledByDefault() {
        val service = VimCoachSettingsServiceImpl()

        assertTrue(service.isShowTipsOnStartupEnabled())
    }

    @Test
    fun setShowTipsOnStartupEnabledUpdatesState() {
        val service = VimCoachSettingsServiceImpl()

        service.setShowTipsOnStartupEnabled(false)

        assertFalse(service.isShowTipsOnStartupEnabled())
    }

    @Test
    fun loadStateRestoresShowTipsOnStartupValue() {
        val service = VimCoachSettingsServiceImpl()
        val persistedState = VimCoachSettingsService.State(showTipsOnStartup = false)

        service.loadState(persistedState)

        assertFalse(service.isShowTipsOnStartupEnabled())
    }
}
