package com.github.pooryam92.vimcoach.features.tips.unit.persistence

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
