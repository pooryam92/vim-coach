package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class VimTipNoRepeatSelectionUnitTest {

    @Test
    fun cyclesThroughAllTipsBeforeRepeating() {
        val tips = (1..5).map { VimTip("tip-$it", listOf("details-$it"), listOf("editing")) }
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore()).apply { saveTips(tips) }

        val firstCycle = (1..tips.size).map { tipService.getRandomTip().summary }
        val secondCycle = (1..tips.size).map { tipService.getRandomTip().summary }

        assertEquals(tips.map { it.summary }.toSet(), firstCycle.toSet())
        assertEquals(tips.map { it.summary }.toSet(), secondCycle.toSet())
    }

    @Test
    fun rotationAppliesAfterTheExcludeFilter() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"), listOf("editing"))
        val visibleTips = (1..3).map { VimTip("visible-$it", listOf("details-$it"), listOf("editing")) }
        val settingsService = SettingsRepositoryImpl(PersistentSettingsStore()).apply {
            hideTip(TipHash.fromTip(hiddenTip).value)
        }
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore(), settingsService).apply {
            saveTips(visibleTips + hiddenTip)
        }

        val twoFullCycles = (1..visibleTips.size * 2).map { tipService.getRandomTip().summary }

        assertFalse(twoFullCycles.contains("hidden"))
        assertEquals(visibleTips.map { it.summary }.toSet(), twoFullCycles.take(visibleTips.size).toSet())
    }

    @Test
    fun rotationIsSharedBetweenCategoryFilteredAndUnfilteredDraws() {
        val tips = listOf(
            VimTip("tip-1", listOf("details-1"), listOf("editing")),
            VimTip("tip-2", listOf("details-2"), listOf("editing"))
        )
        val tipService = VimTipRepositoryImpl(PersistentVimTipStore()).apply { saveTips(tips) }

        val firstDraw = tipService.getRandomTip(listOf("editing")).summary
        val secondDraw = tipService.getRandomTip().summary

        assertNotEquals(firstDraw, secondDraw)
    }
}
