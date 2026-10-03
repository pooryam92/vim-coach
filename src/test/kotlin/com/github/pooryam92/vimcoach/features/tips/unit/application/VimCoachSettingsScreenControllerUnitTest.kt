package com.github.pooryam92.vimcoach.features.tips.unit.application

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.application.settings.ExcludedTipSettingsItem
import com.github.pooryam92.vimcoach.features.tips.application.settings.VimCoachSettingsScreenController
import com.github.pooryam92.vimcoach.features.tips.application.settings.VimCoachSettingsScreenState
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
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
                    vimTip("summary-1", listOf("details-1"), listOf("basics", "editing")),
                    vimTip("summary-2", listOf("details-2"), listOf("search"))
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
        val excludedTip = vimTip("Excluded motion tip", listOf("details"), listOf("basics"))
        val visibleTip = vimTip("Visible search tip", listOf("details"), listOf("search"))
        val settingsService = createSettingsService().apply {
            hideTip(excludedTip.id)
        }
        val tipService = createTipService().apply {
            saveTips(listOf(excludedTip, visibleTip))
        }
        val service = createScreenService(settingsService, tipService)

        val state = service.loadState()

        assertEquals(
            listOf(
                ExcludedTipSettingsItem(
                    id = excludedTip.id,
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
        val excludedTip = vimTip("Excluded motion tip", listOf("details"), listOf("basics"))
        val excludedId = excludedTip.id
        val settingsService = createSettingsService().apply {
            hideTip(excludedId)
        }
        val tipService = createTipService().apply {
            saveTips(listOf(excludedTip))
        }
        val service = createScreenService(settingsService, tipService)
        val state = service.loadState()

        service.saveState(
            state.copy(
                excludedTips = emptyList(),
                restoredExcludedTipIds = listOf(excludedId)
            )
        )

        assertEquals(emptyList<String>(), settingsService.getHiddenTipIds())
    }

    @Test
    fun saveStateDoesNotRestoreTipExcludedAfterStateWasLoaded() {
        val initiallyExcludedTip = vimTip("Initially excluded tip", listOf("details"), listOf("basics"))
        val laterExcludedTip = vimTip("Later excluded tip", listOf("details"), listOf("editing"))
        val settingsService = createSettingsService().apply {
            hideTip(initiallyExcludedTip.id)
        }
        val tipService = createTipService().apply {
            saveTips(listOf(initiallyExcludedTip, laterExcludedTip))
        }
        val service = createScreenService(settingsService, tipService)
        val state = service.loadState()
        val laterExcludedId = laterExcludedTip.id

        settingsService.hideTip(laterExcludedId)
        service.saveState(state)

        assertEquals(
            listOf(initiallyExcludedTip.id, laterExcludedId),
            settingsService.getHiddenTipIds()
        )
    }

    // TODO(1.6.0 upgrade bridge)
    @Test
    fun loadStateRefetchesTipsWhenLegacyCacheHasNoCategories() {
        val settingsService = createSettingsService()
        val tipService = createTipService().apply {
            saveTips(
                listOf(
                    vimTip("legacy-summary-1", listOf("legacy-details-1")),
                    vimTip("legacy-summary-2", listOf("legacy-details-2"))
                )
            )
        }
        val loader = FakeRefreshTips {
            tipService.saveTips(
                listOf(
                    vimTip("summary-1", listOf("details-1"), listOf("basics")),
                    vimTip("summary-2", listOf("details-2"), listOf("editing", "basics"))
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
                    vimTip("summary-1", listOf("details-1"), listOf("basics", "editing")),
                    vimTip("summary-2", listOf("details-2"), listOf("search"))
                )
            )
        }
        val service = createScreenService(settingsService, tipService)

        val state = service.loadState()

        assertEquals(listOf("basics", "editing", "search"), state.availableCategories)
        assertEquals(listOf("editing", "search"), state.enabledCategories)
    }

    private fun createScreenService(
        settingsService: SettingsRepository,
        tipService: VimTipRepository,
        refreshTips: RefreshTips? = null
    ): VimCoachSettingsScreenController {
        return VimCoachSettingsScreenController(settingsService, tipService, refreshTips)
    }

    private fun createSettingsService(): SettingsRepository {
        return SettingsRepositoryImpl(PersistentSettingsStore())
    }

    private fun createTipService(): VimTipRepository {
        return VimTipRepositoryImpl(PersistentVimTipStore())
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
