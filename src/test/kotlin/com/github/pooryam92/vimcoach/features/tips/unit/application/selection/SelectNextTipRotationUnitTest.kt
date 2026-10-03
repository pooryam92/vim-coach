package com.github.pooryam92.vimcoach.features.tips.unit.application.selection

import com.github.pooryam92.vimcoach.features.tips.application.selection.SelectNextTip
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.RotationProgress
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.TipRotationRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentTipRotationStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectNextTipRotationUnitTest {

    private val tipRepository = VimTipRepositoryImpl(PersistentVimTipStore())
    private val settings = SettingsRepositoryImpl(PersistentSettingsStore())
    private val rotationStore = PersistentTipRotationStore()

    @Test
    fun cyclesThroughAllTipsBeforeRepeating() {
        val tips = tips(5)
        tipRepository.saveTips(tips)
        val selectNextTip = selectNextTip()

        val firstCycle = draw(selectNextTip, tips.size)
        val secondCycle = draw(selectNextTip, tips.size)

        assertEquals(summaries(tips), firstCycle.toSet())
        assertEquals(summaries(tips), secondCycle.toSet())
    }

    @Test
    fun progressSurvivesARestart() {
        val tips = tips(6)
        tipRepository.saveTips(tips)
        val seen = draw(selectNextTip(), times = 2)

        val afterRestart = draw(selectNextTip(), times = tips.size - seen.size)

        assertEquals(summaries(tips) - seen.toSet(), afterRestart.toSet())
    }

    @Test
    fun anEditedTipReturnsBeforeTheCycleCompletes() {
        val tips = tips(6)
        tipRepository.saveTips(tips)
        val seen = draw(selectNextTip(), times = 3)
        val edited = tips.single { it.summary == seen.first() }
        tipRepository.saveTips(tips - edited + vimTip(edited.summary, listOf("reworded"), edited.category))

        val restOfCycle = draw(selectNextTip(), times = tips.size - seen.size + 1)

        assertTrue(edited.summary in restOfCycle)
    }

    @Test
    fun aCategoryRetagKeepsTheTipsProgress() {
        val tips = tips(6)
        tipRepository.saveTips(tips)
        val seen = draw(selectNextTip(), times = 3)
        tipRepository.saveTips(tips.map { if (it.summary == seen.first()) it.copy(category = listOf("search")) else it })

        val restOfCycle = draw(selectNextTip(), times = tips.size - seen.size)

        assertEquals(summaries(tips) - seen.toSet(), restOfCycle.toSet())
    }

    @Test
    fun disablingAndReEnablingACategoryKeepsItsProgress() {
        val editing = tips(3, category = "editing")
        val search = tips(3, category = "search")
        tipRepository.saveTips(editing + search)
        val selectNextTip = selectNextTip()
        enableOnly("editing")
        draw(selectNextTip, times = 3)
        enableOnly("search")
        val seenSearch = draw(selectNextTip, times = 1)
        enableOnly("editing", "search")

        val restOfCycle = draw(selectNextTip, times = 2)

        assertEquals(listOf(1, 1, 1), timesShown(editing))
        assertEquals(summaries(search) - seenSearch.toSet(), restOfCycle.toSet())
    }

    @Test
    fun excludingAndRestoringATipKeepsItsProgress() {
        val tips = tips(4)
        tipRepository.saveTips(tips)
        val selectNextTip = selectNextTip()
        val seen = draw(selectNextTip, times = 2)
        val excluded = tips.single { it.summary == seen.first() }
        settings.hideTip(excluded.id)
        val drawnWhileExcluded = draw(selectNextTip, times = 1)
        settings.restoreTip(excluded.id)

        val restOfCycle = draw(selectNextTip, times = 1)

        assertEquals(listOf(1), timesShown(listOf(excluded)))
        assertEquals(summaries(tips) - seen.toSet() - drawnWhileExcluded.toSet(), restOfCycle.toSet())
    }

    @Test
    fun aTipRemovedByRefreshIsPrunedOnTheNextDraw() {
        val tips = tips(3)
        tipRepository.saveTips(tips)
        draw(selectNextTip(), times = 3)
        val removed = tips.first()
        tipRepository.saveTips(tips - removed)

        draw(selectNextTip(), times = 1)

        assertFalse(removed.id in rotationRepository().getProgress().timesShown)
    }

    @Test
    fun anEmptyCacheLeavesTheStoreUntouched() {
        val shown = selectNextTip().select(includeConfigTips = true)

        assertEquals("No tips found.", shown.summary)
        assertEquals(PersistentTipRotationStore.State(), rotationStore.state)
    }

    @Test
    fun aFullyFilteredPoolLeavesTheStoreUntouched() {
        tipRepository.saveTips(tips(2, category = "editing"))
        settings.setEnabledTipCategories(listOf("editing"), emptyList())

        val shown = selectNextTip().select(includeConfigTips = true)

        assertEquals("No tips match the selected categories.", shown.summary)
        assertEquals(PersistentTipRotationStore.State(), rotationStore.state)
    }

    @Test
    fun aRealShowIsRecorded() {
        tipRepository.saveTips(tips(1))

        val shown = selectNextTip().select(includeConfigTips = true)

        val key = shown.id
        assertEquals(RotationProgress(mapOf(key to 1), key), rotationRepository().getProgress())
    }

    private fun selectNextTip(): SelectNextTip {
        return SelectNextTip(tipRepository, rotationRepository(), settings)
    }

    private fun rotationRepository() = TipRotationRepositoryImpl(rotationStore)

    private fun draw(selectNextTip: SelectNextTip, times: Int): List<String> {
        return (1..times).map { selectNextTip.select(includeConfigTips = true).summary }
    }

    private fun enableOnly(vararg categories: String) {
        settings.setEnabledTipCategories(listOf("editing", "search"), categories.toList())
    }

    private fun timesShown(tips: List<VimTip>): List<Int?> {
        val counts = rotationRepository().getProgress().timesShown
        return tips.map { counts[it.id] }
    }

    private fun tips(count: Int, category: String = "editing"): List<VimTip> {
        return (1..count).map { vimTip("$category-$it", listOf("$category-details-$it"), listOf(category)) }
    }

    private fun summaries(tips: List<VimTip>): Set<String> = tips.map { it.summary }.toSet()
}
