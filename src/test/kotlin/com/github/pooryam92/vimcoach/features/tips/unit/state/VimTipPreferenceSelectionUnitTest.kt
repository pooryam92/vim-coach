package com.github.pooryam92.vimcoach.features.tips.unit.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsServiceImpl
import com.github.pooryam92.vimcoach.features.tips.state.VimTipServiceImpl
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStoreImpl
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStoreImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class VimTipPreferenceSelectionUnitTest {

    @Test
    fun hiddenTipsAreExcludedFromRandomSelection() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val visibleTip = VimTip("visible", listOf("visible-details"), listOf("editing"))
        val settingsService = VimCoachSettingsServiceImpl(VimCoachSettingsStoreImpl()).apply {
            hideTip(TipHash.fromTip(hiddenTip).value)
        }
        val tipService = VimTipServiceImpl(VimTipStoreImpl(), settingsService).apply {
            saveTips(listOf(hiddenTip, visibleTip))
        }

        repeat(20) {
            assertEquals("visible", tipService.getRandomTip().summary)
        }
    }

    @Test
    fun filteredFallbackIsReturnedWhenAllMatchingTipsAreHidden() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val settingsService = VimCoachSettingsServiceImpl(VimCoachSettingsStoreImpl()).apply {
            hideTip(TipHash.fromTip(hiddenTip).value)
        }
        val tipService = VimTipServiceImpl(VimTipStoreImpl(), settingsService).apply {
            saveTips(listOf(hiddenTip))
        }

        val selectedTip = tipService.getRandomTip(listOf("editing"))

        assertEquals("No tips match the selected categories.", selectedTip.summary)
    }
}
