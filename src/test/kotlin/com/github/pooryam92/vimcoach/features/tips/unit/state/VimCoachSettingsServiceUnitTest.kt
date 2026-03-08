package com.github.pooryam92.vimcoach.features.tips.unit.state

import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsServiceImpl
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStoreImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VimCoachSettingsServiceUnitTest {

    @Test
    fun showTipsOnStartupIsEnabledByDefault() {
        val service = createService()

        assertTrue(service.isShowTipsOnStartupEnabled())
    }

    @Test
    fun setShowTipsOnStartupEnabledUpdatesState() {
        val service = createService()

        service.setShowTipsOnStartupEnabled(false)

        assertFalse(service.isShowTipsOnStartupEnabled())
    }

    @Test
    fun loadStateRestoresShowTipsOnStartupValue() {
        val store = VimCoachSettingsStoreImpl()
        val service = createService(store)
        val persistedState = VimCoachSettingsStore.State(showTipsOnStartup = false)

        store.loadState(persistedState)

        assertFalse(service.isShowTipsOnStartupEnabled())
    }

    private fun createService(
        store: VimCoachSettingsStore = VimCoachSettingsStoreImpl()
    ): VimCoachSettingsService {
        return VimCoachSettingsServiceImpl(store)
    }
}
