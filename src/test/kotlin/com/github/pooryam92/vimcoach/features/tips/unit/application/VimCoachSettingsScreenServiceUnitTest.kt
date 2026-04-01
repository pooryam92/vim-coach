package com.github.pooryam92.vimcoach.features.tips.unit.application

import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenService
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenServiceImpl
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenState
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsServiceImpl
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipServiceImpl
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStoreImpl
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStoreImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VimCoachSettingsScreenServiceUnitTest {

    @Test
    fun loadStateCombinesSettingsAndTipCategories() {
        val settingsService = createSettingsService()
        settingsService.setShowTipsOnStartupEnabled(false)
        settingsService.setPeriodicTipsEnabled(true)
        settingsService.setTipIntervalHours(6)
        settingsService.setEnabledTipCategories(listOf("editing"))

        val tipService = createTipService().apply {
            saveTips(
                listOf(
                    VimTip("summary-1", listOf("details-1"), listOf("basics", "editing")),
                    VimTip("summary-2", listOf("details-2"), listOf("search"))
                )
            )
        }
        val service = createScreenService(settingsService, tipService)

        val state = service.loadState()

        assertFalse(state.showTipsOnStartup)
        assertTrue(state.periodicTipsEnabled)
        assertEquals(6, state.tipIntervalHours)
        assertEquals(listOf("basics", "editing", "search"), state.availableCategories)
        assertEquals(listOf("editing"), state.enabledCategories)
    }

    @Test
    fun saveStatePersistsFormValues() {
        val settingsService = createSettingsService()
        val tipService = createTipService()
        val service = createScreenService(settingsService, tipService)

        service.saveState(
            VimCoachSettingsScreenState(
                showTipsOnStartup = false,
                periodicTipsEnabled = true,
                tipIntervalHours = 8,
                availableCategories = listOf("basics", "editing", "search"),
                enabledCategories = listOf("basics", "search")
            )
        )

        assertFalse(settingsService.isShowTipsOnStartupEnabled())
        assertTrue(settingsService.isPeriodicTipsEnabled())
        assertEquals(8, settingsService.getTipIntervalHours())
        assertEquals(
            listOf("basics", "search"),
            settingsService.getEnabledTipCategories(listOf("basics", "editing", "search"))
        )
    }

    private fun createScreenService(
        settingsService: VimCoachSettingsService,
        tipService: VimTipService
    ): VimCoachSettingsScreenService {
        return VimCoachSettingsScreenServiceImpl(settingsService, tipService)
    }

    private fun createSettingsService(): VimCoachSettingsService {
        return VimCoachSettingsServiceImpl(VimCoachSettingsStoreImpl())
    }

    private fun createTipService(): VimTipService {
        return VimTipServiceImpl(VimTipStoreImpl())
    }
}
