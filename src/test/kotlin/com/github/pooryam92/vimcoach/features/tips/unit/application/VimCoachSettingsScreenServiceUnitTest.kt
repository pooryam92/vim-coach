package com.github.pooryam92.vimcoach.features.tips.unit.application

import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderService
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenService
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenServiceImpl
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenState
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
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
        settingsService.setEnabledTipCategories(
            availableCategories = listOf("basics", "editing", "search"),
            enabledCategories = listOf("editing")
        )

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

    @Test
    fun loadStateRefetchesTipsWhenLegacyCacheHasNoCategories() {
        val settingsService = createSettingsService()
        val tipService = createTipService().apply {
            saveTips(
                listOf(
                    VimTip("legacy-summary-1", listOf("legacy-details-1")),
                    VimTip("legacy-summary-2", listOf("legacy-details-2"))
                )
            )
        }
        val loader = FakeTipLoaderService {
            tipService.saveTips(
                listOf(
                    VimTip("summary-1", listOf("details-1"), listOf("basics")),
                    VimTip("summary-2", listOf("details-2"), listOf("editing", "basics"))
                )
            )
            TipLoadResult.Updated(2)
        }
        val service = createScreenService(settingsService, tipService, loader)

        val state = service.loadState()

        assertEquals(1, loader.refetchCalls)
        assertEquals(listOf("basics", "editing"), state.availableCategories)
        assertEquals(listOf("basics", "editing"), state.enabledCategories)
    }

    @Test
    fun loadStateSelectsNewCategoriesByDefault() {
        val settingsService = createSettingsService().apply {
            setEnabledTipCategories(
                availableCategories = listOf("basics", "editing"),
                enabledCategories = listOf("editing")
            )
        }
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

        assertEquals(listOf("basics", "editing", "search"), state.availableCategories)
        assertEquals(listOf("editing", "search"), state.enabledCategories)
    }

    private fun createScreenService(
        settingsService: VimCoachSettingsService,
        tipService: VimTipService,
        tipLoaderService: TipLoaderService? = null
    ): VimCoachSettingsScreenService {
        return VimCoachSettingsScreenServiceImpl(settingsService, tipService, tipLoaderService)
    }

    private fun createSettingsService(): VimCoachSettingsService {
        return VimCoachSettingsServiceImpl(VimCoachSettingsStoreImpl())
    }

    private fun createTipService(): VimTipService {
        return VimTipServiceImpl(VimTipStoreImpl())
    }

    private class FakeTipLoaderService(
        private val onRefetch: () -> TipLoadResult
    ) : TipLoaderService {
        var refetchCalls = 0
            private set

        override fun refetchTips(): TipLoadResult {
            refetchCalls += 1
            return onRefetch()
        }

        override fun checkForUpdates(): TipLoadResult {
            return TipLoadResult.NotModified
        }
    }
}
