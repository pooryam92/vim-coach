package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TipSelectionIndexUnitTest {

    @Test
    fun matchingTipsReturnsTipsForSingleCategory() {
        val basicsTip = VimTip("basics", listOf("details"), listOf("basics"))
        val editingTip = VimTip("editing", listOf("details"), listOf("editing"))
        val index = TipSelectionIndex.fromTips(listOf(basicsTip, editingTip))

        val matchingTips = index.matchingTips(listOf("editing"))

        assertEquals(1, matchingTips.size)
        assertSame(editingTip, matchingTips.single())
    }

    @Test
    fun matchingTipsNormalizesRequestedCategoriesAndDeduplicatesTips() {
        val sharedTip = VimTip("shared", listOf("details"), listOf("basics", "editing"))
        val searchTip = VimTip("search", listOf("details"), listOf("search"))
        val index = TipSelectionIndex.fromTips(listOf(sharedTip, searchTip))

        val matchingTips = index.matchingTips(listOf(" basics ", "editing", "editing", ""))

        assertEquals(1, matchingTips.size)
        assertSame(sharedTip, matchingTips.single())
    }

    @Test
    fun matchingTipsReturnsEmptyWhenNoCategoriesMatch() {
        val index = TipSelectionIndex.fromTips(
            listOf(VimTip("basics", listOf("details"), listOf("basics")))
        )

        val matchingTips = index.matchingTips(listOf("search"))

        assertEquals(emptyList<VimTip>(), matchingTips)
    }
}
