package com.github.pooryam92.vimcoach.features.tips.unit.application.selection

import com.github.pooryam92.vimcoach.features.tips.application.selection.SelectNextTip
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectNextTipFilteringUnitTest {

    @Test
    fun hiddenTipsAreExcludedFromSelection() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val visibleTip = VimTip("visible", listOf("visible-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(TipHash.fromTip(hiddenTip).value)
        }
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(hiddenTip, visibleTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, settingsService)

        repeat(20) {
            assertEquals("visible", selectNextTip.select(includeConfigTips = true).summary)
        }
    }

    @Test
    fun configTipsAreExcludedWhenIncludeConfigTipsIsFalse() {
        val configTip = VimTip(
            "config", listOf("config-details"), listOf("editing"),
            config = TipConfig(lines = listOf("set scrolloff=5"))
        )
        val plainTip = VimTip("plain", listOf("plain-details"), listOf("editing"))
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(configTip, plainTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, SettingsRepositoryImpl(PersistentSettingsStore()))

        repeat(20) {
            assertEquals("plain", selectNextTip.select(includeConfigTips = false).summary)
        }
    }

    @Test
    fun advancedTipsAreExcludedFromSelectionWhenSettingIsOff() {
        val advancedTip = VimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val normalTip = VimTip("normal", listOf("normal-details"), listOf("editing"))
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip, normalTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, SettingsRepositoryImpl(PersistentSettingsStore()))

        repeat(20) {
            assertEquals("normal", selectNextTip.select(includeConfigTips = true).summary)
        }
    }

    @Test
    fun advancedTipsAreIncludedInSelectionWhenSettingIsOn() {
        val advancedTip = VimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            setShowAdvancedTipsEnabled(true)
        }
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, settingsService)

        repeat(20) {
            assertEquals("advanced", selectNextTip.select(includeConfigTips = true).summary)
        }
    }

    @Test
    fun filteredFallbackIsReturnedWhenOnlyAdvancedTipsMatchAndSettingIsOff() {
        val advancedTip = VimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, SettingsRepositoryImpl(PersistentSettingsStore()))

        val selectedTip = selectNextTip.select(includeConfigTips = true)

        assertEquals("No tips match the selected categories.", selectedTip.summary)
    }

    // No settings service (e.g. an unconfigured cache outside a project) must hide advanced tips —
    // the safe default. The single-arg constructor injects no settings and the platform service
    // lookup fails in a plain unit test, so this exercises exactly that null fallback.
    @Test
    fun hidesAdvancedTipsWhenSettingsServiceUnavailable() {
        val advancedTip = VimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(advancedTip))
        }
        val selectNextTip = SelectNextTip(tipRepository)

        assertEquals("No tips match the selected categories.", selectNextTip.select(includeConfigTips = true).summary)
    }

    @Test
    fun filteredFallbackIsReturnedWhenAllTipsAreHidden() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(TipHash.fromTip(hiddenTip).value)
        }
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(hiddenTip))
        }
        val selectNextTip = SelectNextTip(tipRepository, settingsService)

        val selectedTip = selectNextTip.select(includeConfigTips = true)

        assertEquals("No tips match the selected categories.", selectedTip.summary)
    }
}
