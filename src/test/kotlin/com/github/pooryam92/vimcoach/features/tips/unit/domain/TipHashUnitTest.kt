package com.github.pooryam92.vimcoach.features.tips.unit.domain

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TipHashUnitTest {

    @Test
    fun sameNormalizedTipTitleProducesSameHash() {
        val first = VimTip(
            summary = " Move by word ",
            details = listOf(" w moves forward ", ""),
            category = listOf("motion", "editing")
        )
        val second = VimTip(
            summary = "Move by word",
            details = listOf("w moves forward"),
            category = listOf("editing", "motion")
        )

        assertEquals(TipHash.fromTip(first), TipHash.fromTip(second))
    }

    @Test
    fun changedTipDetailsKeepSameHash() {
        val first = VimTip("Move by word", listOf("w moves forward"), listOf("motion"))
        val second = VimTip("Move by word", listOf("b moves backward"), listOf("motion"))

        assertEquals(TipHash.fromTip(first), TipHash.fromTip(second))
    }

    @Test
    fun changedTipTitleProducesDifferentHash() {
        val first = VimTip("Move by word", listOf("w moves forward"), listOf("motion"))
        val second = VimTip("Move backward by word", listOf("w moves forward"), listOf("motion"))

        assertNotEquals(TipHash.fromTip(first), TipHash.fromTip(second))
    }
}
