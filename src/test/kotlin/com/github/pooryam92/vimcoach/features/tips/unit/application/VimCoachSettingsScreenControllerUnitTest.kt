package com.github.pooryam92.vimcoach.features.tips.unit.application

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.application.settings.ExcludedTipSettingsItem
import com.github.pooryam92.vimcoach.features.tips.application.settings.VimCoachSettingsScreenController
import com.github.pooryam92.vimcoach.features.tips.application.settings.VimCoachSettingsScreenState
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
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

class VimCoachSettingsScreenControllerUnitTest {

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
    fun loadStateIncludesExcludedTipsBySummary() {
        val excludedTip = VimTip("Excluded motion tip", listOf("details"), listOf("basics"))
        val visibleTip = VimTip("Visible search tip", listOf("details"), listOf("search"))
        val settingsService = createSettingsService().apply {
            hideTip(TipHash.fromTip(excludedTip).value)
        }
        val tipService = createTipService().apply {
            saveTips(listOf(excludedTip, visibleTip))
        }
        val service = createScreenService(settingsService, tipService)

        val state = service.loadState()

        assertEquals(
            listOf(
                ExcludedTipSettingsItem(
                    hash = TipHash.fromTip(excludedTip).value,
                    summary = "Excluded motion tip"
                )
            ),
            state.excludedTips
        )
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
    fun saveStateRestoresExplicitlyRestoredExcludedTips() {
        val excludedTip = VimTip("Excluded motion tip", listOf("details"), listOf("basics"))
        val excludedHash = TipHash.fromTip(excludedTip).value
        val settingsService = createSettingsService().apply {
            hideTip(excludedHash)
        }
        val tipService = createTipService().apply {
            saveTips(listOf(excludedTip))
        }
        val service = createScreenService(settingsService, tipService)
        val state = service.loadState()

        service.saveState(
            state.copy(
                excludedTips = emptyList(),
                restoredExcludedTipHashes = listOf(excludedHash)
            )
        )

        assertEquals(emptyList<String>(), settingsService.getHiddenTipHashes())
    }

    @Test
    fun saveStateDoesNotRestoreTipExcludedAfterStateWasLoaded() {
        val initiallyExcludedTip = VimTip("Initially excluded tip", listOf("details"), listOf("basics"))
        val laterExcludedTip = VimTip("Later excluded tip", listOf("details"), listOf("editing"))
        val settingsService = createSettingsService().apply {
            hideTip(TipHash.fromTip(initiallyExcludedTip).value)
        }
        val tipService = createTipService().apply {
            saveTips(listOf(initiallyExcludedTip, laterExcludedTip))
        }
        val service = createScreenService(settingsService, tipService)
        val state = service.loadState()
        val laterExcludedHash = TipHash.fromTip(laterExcludedTip).value

        settingsService.hideTip(laterExcludedHash)
        service.saveState(state)

        assertEquals(
            listOf(TipHash.fromTip(initiallyExcludedTip).value, laterExcludedHash),
            settingsService.getHiddenTipHashes()
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
        val loader = FakeRefreshTips {
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
        refreshTips: RefreshTips? = null
    ): VimCoachSettingsScreenController {
        return VimCoachSettingsScreenController(settingsService, tipService, refreshTips)
    }

    private fun createSettingsService(): VimCoachSettingsService {
        return VimCoachSettingsServiceImpl(VimCoachSettingsStoreImpl())
    }

    private fun createTipService(): VimTipService {
        return VimTipServiceImpl(VimTipStoreImpl())
    }

    private class FakeRefreshTips(
        private val onRefetch: () -> TipLoadResult
    ) : RefreshTips {
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
