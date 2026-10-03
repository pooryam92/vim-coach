package com.github.pooryam92.vimcoach.features.tips.unit.application.selection

import com.github.pooryam92.vimcoach.features.tips.application.selection.SelectNextTip
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.inMemoryTipRotation
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectNextTipFilteringUnitTest {

    @Test
    fun hiddenTipsAreExcludedFromSelection() {
        val hiddenTip = vimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val visibleTip = vimTip("visible", listOf("visible-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(hiddenTip.id)
        }
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(hiddenTip, visibleTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation(), settingsService)

        repeat(20) {
            assertEquals("visible", selectNextTip.select(includeConfigTips = true).summary)
        }
    }

    @Test
    fun aHiddenTipIsShownAgainOnceItsIdChanges() {
        val hiddenTip = vimTip("reworded", listOf("old-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(hiddenTip.id)
        }
        val rewordedTip = vimTip("reworded", listOf("new-details"), listOf("editing"))
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(rewordedTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation(), settingsService)

        assertEquals(rewordedTip, selectNextTip.select(includeConfigTips = true))
    }

    @Test
    fun configTipsAreExcludedWhenIncludeConfigTipsIsFalse() {
        val configTip = vimTip(
            "config", listOf("config-details"), listOf("editing"),
            config = TipConfig(lines = listOf("set scrolloff=5"))
        )
        val plainTip = vimTip("plain", listOf("plain-details"), listOf("editing"))
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(configTip, plainTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation(), SettingsRepositoryImpl(PersistentSettingsStore()))

        repeat(20) {
            assertEquals("plain", selectNextTip.select(includeConfigTips = false).summary)
        }
    }

    @Test
    fun advancedTipsAreExcludedFromSelectionWhenSettingIsOff() {
        val advancedTip = vimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val normalTip = vimTip("normal", listOf("normal-details"), listOf("editing"))
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip, normalTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation(), SettingsRepositoryImpl(PersistentSettingsStore()))

        repeat(20) {
            assertEquals("normal", selectNextTip.select(includeConfigTips = true).summary)
        }
    }

    @Test
    fun advancedTipsAreIncludedInSelectionWhenSettingIsOn() {
        val advancedTip = vimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            setShowAdvancedTipsEnabled(true)
        }
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation(), settingsService)

        repeat(20) {
            assertEquals("advanced", selectNextTip.select(includeConfigTips = true).summary)
        }
    }

    @Test
    fun filteredFallbackIsReturnedWhenOnlyAdvancedTipsMatchAndSettingIsOff() {
        val advancedTip = vimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation(), SettingsRepositoryImpl(PersistentSettingsStore()))

        val selectedTip = selectNextTip.select(includeConfigTips = true)

        assertEquals("No tips match the selected categories.", selectedTip.summary)
    }

    // No settings service (e.g. an unconfigured cache outside a project) must hide advanced tips —
    // the safe default. The single-arg constructor injects no settings and the platform service
    // lookup fails in a plain unit test, so this exercises exactly that null fallback.
    @Test
    fun hidesAdvancedTipsWhenSettingsServiceUnavailable() {
        val advancedTip = vimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation())

        assertEquals("No tips match the selected categories.", selectNextTip.select(includeConfigTips = true).summary)
    }

    @Test
    fun filteredFallbackIsReturnedWhenAllTipsAreHidden() {
        val hiddenTip = vimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(hiddenTip.id)
        }
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(hiddenTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, inMemoryTipRotation(), settingsService)

        val selectedTip = selectNextTip.select(includeConfigTips = true)

        assertEquals("No tips match the selected categories.", selectedTip.summary)
    }
}
