package com.github.pooryam92.vimcoach.features.tips.unit.application.selection

import com.github.pooryam92.vimcoach.features.tips.application.selection.SelectNextTip
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SelectNextTipRotationUnitTest {

    @Test
    fun cyclesThroughAllTipsBeforeRepeating() {
        val tips = (1..5).map { VimTip("tip-$it", listOf("details-$it"), listOf("editing")) }
        val selectNextTip = selectNextTip(tips)

        val firstCycle = (1..tips.size).map { selectNextTip.select(includeConfigTips = true).summary }
        val secondCycle = (1..tips.size).map { selectNextTip.select(includeConfigTips = true).summary }

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
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply {
            saveTips(visibleTips + hiddenTip)
        }
        val selectNextTip = SelectNextTip(tipRepository, settingsService)

        val twoFullCycles = (1..visibleTips.size * 2).map { selectNextTip.select(includeConfigTips = true).summary }

        assertFalse(twoFullCycles.contains("hidden"))
        assertEquals(visibleTips.map { it.summary }.toSet(), twoFullCycles.take(visibleTips.size).toSet())
    }

    private fun selectNextTip(tips: List<VimTip>): SelectNextTip {
        val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore()).apply { saveTips(tips) }
        return SelectNextTip(tipRepository, SettingsRepositoryImpl(PersistentSettingsStore()))
    }
}
