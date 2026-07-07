package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import org.junit.Assert.assertEquals
import org.junit.Test

class VimTipPreferenceSelectionUnitTest {

    @Test
    fun hiddenTipsAreExcludedFromRandomSelection() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val visibleTip = VimTip("visible", listOf("visible-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(TipHash.fromTip(hiddenTip).value)
        }
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore(), settingsService).apply {
            saveTips(listOf(hiddenTip, visibleTip))
        }

        repeat(20) {
            assertEquals("visible", tipService.getRandomTip().summary)
        }
    }

    @Test
    fun configTipsAreExcludedWhenIncludeConfigTipsIsFalse() {
        val configTip = VimTip(
            "config", listOf("config-details"), listOf("editing"),
            config = TipConfig(lines = listOf("set scrolloff=5"))
        )
        val plainTip = VimTip("plain", listOf("plain-details"), listOf("editing"))
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(listOf(configTip, plainTip))
        }

        repeat(20) {
            assertEquals("plain", tipService.getRandomTip(includeConfigTips = false).summary)
            assertEquals("plain", tipService.getRandomTip(listOf("editing"), includeConfigTips = false).summary)
        }
    }

    @Test
    fun advancedTipsAreExcludedFromRandomSelectionWhenSettingIsOff() {
        val advancedTip = VimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val normalTip = VimTip("normal", listOf("normal-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore())
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore(), settingsService).apply {
            saveTips(listOf(advancedTip, normalTip))
        }

        repeat(20) {
            assertEquals("normal", tipService.getRandomTip(listOf("editing")).summary)
        }
    }

    @Test
    fun advancedTipsAreIncludedInRandomSelectionWhenSettingIsOn() {
        val advancedTip = VimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            setShowAdvancedTipsEnabled(true)
        }
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore(), settingsService).apply {
            saveTips(listOf(advancedTip))
        }

        repeat(20) {
            assertEquals("advanced", tipService.getRandomTip(listOf("editing")).summary)
        }
    }

    @Test
    fun filteredFallbackIsReturnedWhenOnlyAdvancedTipsMatchAndSettingIsOff() {
        val advancedTip = VimTip("advanced", listOf("advanced-details"), listOf("editing"), advanced = true)
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore())
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore(), settingsService).apply {
            saveTips(listOf(advancedTip))
        }

        val selectedTip = tipService.getRandomTip(listOf("editing"))

        assertEquals("No tips match the selected categories.", selectedTip.summary)
    }

    @Test
    fun filteredFallbackIsReturnedWhenAllMatchingTipsAreHidden() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(TipHash.fromTip(hiddenTip).value)
        }
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore(), settingsService).apply {
            saveTips(listOf(hiddenTip))
        }

        val selectedTip = tipService.getRandomTip(listOf("editing"))

        assertEquals("No tips match the selected categories.", selectedTip.summary)
    }
}
