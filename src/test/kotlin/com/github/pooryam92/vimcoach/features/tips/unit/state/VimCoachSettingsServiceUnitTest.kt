package com.github.pooryam92.vimcoach.features.tips.unit.state

import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsServiceImpl
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStoreImpl
import org.junit.Assert.assertEquals
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
        val store = VimCoachSettingsStoreImpl()
        val service = createService(store)
        val persistedState = VimCoachSettingsStore.State(showTipsOnStartup = false)

        store.loadState(persistedState)

        assertFalse(service.isShowTipsOnStartupEnabled())
    }

    @Test
    fun loadStateRestoresPeriodicTipsEnabledValue() {
        val store = VimCoachSettingsStoreImpl()
        val service = createService(store)
        val persistedState = VimCoachSettingsStore.State(periodicTipsEnabled = true)

        store.loadState(persistedState)

        assertTrue(service.isPeriodicTipsEnabled())
    }

    @Test
    fun loadStateRestoresTipIntervalHoursValue() {
        val store = VimCoachSettingsStoreImpl()
        val service = createService(store)
        val persistedState = VimCoachSettingsStore.State(tipIntervalHours = 8)

        store.loadState(persistedState)

        assertEquals(8, service.getTipIntervalHours())
    }

    @Test
    fun loadStateRestoresDisabledTipCategories() {
        val store = VimCoachSettingsStoreImpl()
        val service = createService(store)
        val persistedState = VimCoachSettingsStore.State(disabledTipCategories = listOf("editing"))

        store.loadState(persistedState)

        assertEquals(
            listOf("basics", "search"),
            service.getEnabledTipCategories(listOf("basics", "search", "editing"))
        )
    }

    private fun createService(
        store: VimCoachSettingsStore = VimCoachSettingsStoreImpl()
    ): VimCoachSettingsService {
        return VimCoachSettingsServiceImpl(store)
    }
}
