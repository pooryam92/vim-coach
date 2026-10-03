package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl

import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryUnitTest {

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
    fun periodicTipsAreDisabledByDefault() {
        val service = createService()

        assertFalse(service.isPeriodicTipsEnabled())
    }

    @Test
    fun setPeriodicTipsEnabledUpdatesState() {
        val service = createService()

        service.setPeriodicTipsEnabled(true)

        assertTrue(service.isPeriodicTipsEnabled())
    }

    @Test
    fun tipIntervalHoursDefaultsTo1() {
        val service = createService()

        assertEquals(1, service.getTipIntervalHours())
    }

    @Test
    fun setTipIntervalHoursUpdatesState() {
        val service = createService()

        service.setTipIntervalHours(12)

        assertEquals(12, service.getTipIntervalHours())
    }

    @Test
    fun setTipIntervalHoursClampsToMinimumValue() {
        val service = createService()

        service.setTipIntervalHours(-1)

        assertEquals(1, service.getTipIntervalHours())
    }

    @Test
    fun enabledTipCategoriesDefaultToAllAvailableCategories() {
        val service = createService()

        val enabledCategories = service.getEnabledTipCategories(listOf("basics", "editing"))

        assertEquals(listOf("basics", "editing"), enabledCategories)
    }

    @Test
    fun setEnabledTipCategoriesUpdatesState() {
        val service = createService()

        service.setEnabledTipCategories(
            availableCategories = listOf("basics", "editing", "search"),
            enabledCategories = listOf("basics", "editing", "basics")
        )

        assertEquals(
            listOf("basics", "editing"),
            service.getEnabledTipCategories(listOf("basics", "editing", "search"))
        )
    }

    @Test
    fun removedCategoriesDoNotLeakIntoEnabledResults() {
        val service = createService()

        service.setEnabledTipCategories(
            availableCategories = listOf("basics", "editing", "search"),
            enabledCategories = listOf("editing", "search")
        )

        assertEquals(
            listOf("editing", "search", "macros"),
            service.getEnabledTipCategories(listOf("editing", "search", "macros"))
        )
    }

    @Test
    fun newCategoriesAreEnabledByDefaultAfterSavingSelection() {
        val service = createService()

        service.setEnabledTipCategories(
            availableCategories = listOf("basics", "editing"),
            enabledCategories = listOf("editing")
        )

        assertEquals(
            listOf("editing", "search"),
            service.getEnabledTipCategories(listOf("basics", "editing", "search"))
        )
    }

    @Test
    fun loadStateRestoresShowTipsOnStartupValue() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(showTipsOnStartup = false)

        store.loadState(persistedState)

        assertFalse(service.isShowTipsOnStartupEnabled())
    }

    @Test
    fun loadStateRestoresPeriodicTipsEnabledValue() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(periodicTipsEnabled = true)

        store.loadState(persistedState)

        assertTrue(service.isPeriodicTipsEnabled())
    }

    @Test
    fun loadStateRestoresTipIntervalHoursValue() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(tipIntervalHours = 8)

        store.loadState(persistedState)

        assertEquals(8, service.getTipIntervalHours())
    }

    @Test
    fun loadStateRestoresDisabledTipCategories() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(disabledTipCategories = listOf("editing"))

        store.loadState(persistedState)

        assertEquals(
            listOf("basics", "search"),
            service.getEnabledTipCategories(listOf("basics", "search", "editing"))
        )
    }

    @Test
    fun excludedTipsManagementHintIsAvailableOnceByDefault() {
        val service = createService()

        assertTrue(service.consumeExcludedTipsManagementHint())
        assertFalse(service.consumeExcludedTipsManagementHint())
    }

    @Test
    fun loadStateRestoresConsumedExcludedTipsManagementHint() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(excludedTipsManagementHintShown = true)

        store.loadState(persistedState)

        assertFalse(service.consumeExcludedTipsManagementHint())
    }

    @Test
    fun showAdvancedTipsIsDisabledByDefault() {
        val service = createService()

        assertFalse(service.isShowAdvancedTipsEnabled())
    }

    @Test
    fun setShowAdvancedTipsEnabledUpdatesState() {
        val service = createService()

        service.setShowAdvancedTipsEnabled(true)

        assertTrue(service.isShowAdvancedTipsEnabled())
    }

    @Test
    fun enablingShowAdvancedTipsRetiresTheDiscoveryHint() {
        val service = createService()

        service.setShowAdvancedTipsEnabled(true)

        assertTrue(service.isAdvancedTipsHintShown())
        assertFalse(service.consumeAdvancedTipsHint())
    }

    @Test
    fun disablingShowAdvancedTipsLeavesTheDiscoveryHintAvailable() {
        val service = createService()

        service.setShowAdvancedTipsEnabled(false)

        assertFalse(service.isAdvancedTipsHintShown())
    }

    @Test
    fun loadStateRestoresShowAdvancedTipsValue() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(showAdvancedTips = true)

        store.loadState(persistedState)

        assertTrue(service.isShowAdvancedTipsEnabled())
    }

    @Test
    fun advancedTipsHintIsAvailableOnceByDefault() {
        val service = createService()

        assertFalse(service.isAdvancedTipsHintShown())
        assertTrue(service.consumeAdvancedTipsHint())
        assertTrue(service.isAdvancedTipsHintShown())
        assertFalse(service.consumeAdvancedTipsHint())
    }

    @Test
    fun loadStateRestoresConsumedAdvancedTipsHint() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(advancedTipsHintShown = true)

        store.loadState(persistedState)

        assertFalse(service.consumeAdvancedTipsHint())
    }

    @Test
    fun tipsShownForAdvancedNudgeCountRoundTrips() {
        val service = createService()

        assertEquals(0, service.getTipsShownForAdvancedNudge())

        service.setTipsShownForAdvancedNudge(2)

        assertEquals(2, service.getTipsShownForAdvancedNudge())
    }

    @Test
    fun loadStateRestoresRecordedAdvancedNudgeProgress() {
        val store = PersistentSettingsStore()
        val service = createService(store)
        val persistedState = PersistentSettingsStore.State(tipsShownForAdvancedNudge = 2)

        store.loadState(persistedState)

        assertEquals(2, service.getTipsShownForAdvancedNudge())
    }

    @Test
    fun restoreTipRemovesExcludedTipId() {
        val service = createService()
        service.hideTip(" id-1 ")
        service.hideTip("id-2")

        service.restoreTip("id-1")

        assertEquals(listOf("id-2"), service.getHiddenTipIds())
    }

    // TODO(1.6.0 upgrade bridge)
    @Test
    fun migrateLegacyHiddenTipsHidesTheTipWithTheHashedSummary() {
        val store = PersistentSettingsStore()
        store.loadState(PersistentSettingsStore.State(hiddenTipHashes = listOf(SURROUND_A_WORD_HASH)))
        val surroundTip = vimTip("Surround a word", listOf("ysiw\""))
        val service = createService(store)

        service.migrateLegacyHiddenTips(listOf(surroundTip, vimTip("Other tip", listOf("details"))))

        assertEquals(listOf(surroundTip.id), service.getHiddenTipIds())
        assertEquals(emptyList<String>(), store.state.hiddenTipHashes)
    }

    // TODO(1.6.0 upgrade bridge)
    @Test
    fun migrateLegacyHiddenTipsKeepsCurrentIdsAndForgetsUnmatchedHashes() {
        val store = PersistentSettingsStore()
        store.loadState(
            PersistentSettingsStore.State(
                hiddenTipIds = listOf("already-hidden"),
                hiddenTipHashes = listOf(SURROUND_A_WORD_HASH, REMOVED_TIP_HASH)
            )
        )
        val surroundTip = vimTip("Surround a word", listOf("ysiw\""))
        val service = createService(store)

        service.migrateLegacyHiddenTips(listOf(surroundTip))

        assertEquals(listOf("already-hidden", surroundTip.id), service.getHiddenTipIds())
        assertEquals(emptyList<String>(), store.state.hiddenTipHashes)
    }

    // TODO(1.6.0 upgrade bridge)
    @Test
    fun migrateLegacyHiddenTipsWaitsForTips() {
        val store = PersistentSettingsStore()
        store.loadState(PersistentSettingsStore.State(hiddenTipHashes = listOf(SURROUND_A_WORD_HASH)))
        val service = createService(store)

        service.migrateLegacyHiddenTips(emptyList())

        assertEquals(listOf(SURROUND_A_WORD_HASH), store.state.hiddenTipHashes)
        assertEquals(emptyList<String>(), service.getHiddenTipIds())
    }

    private fun createService(
        store: PersistentSettingsStore = PersistentSettingsStore()
    ): SettingsRepository {
        return SettingsRepositoryImpl(store)
    }

    private companion object {
        // TODO(1.6.0 upgrade bridge): SHA-256 of the summary, exactly as 1.5.x stored it.
        const val SURROUND_A_WORD_HASH = "4e5464d16f024bd533c31046ad855e999adbf2aa7975213e7c454eaf6578d8b0"
        const val REMOVED_TIP_HASH = "1d20c90830e51ecaba0b00f6298ffa6db2ec643fdb05016e1b34eb0263b7a14c"
    }
}
