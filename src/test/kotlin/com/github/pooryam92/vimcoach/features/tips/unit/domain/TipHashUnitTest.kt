package com.github.pooryam92.vimcoach.features.tips.unit.domain

import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TipHashUnitTest {
    private val fullTip = VimTip(
        summary = "Surround a word",
        details = listOf("ysiw\" wraps the word in quotes", "ds\" removes them", "Works on any text object — café"),
        category = listOf("plugins"),
        config = TipConfig("vim-surround", listOf("set surround", "nmap s ys")),
        mnemonic = "you surround"
    )

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

    @Test
    fun editingSummaryOrDetailChangesContentKey() {
        assertNotEquals(key(fullTip), key(fullTip.copy(summary = "Surround a word with quotes")))
        assertNotEquals(key(fullTip), key(fullTip.copy(details = fullTip.details.dropLast(1))))
    }

    @Test
    fun otherFieldsKeepContentKey() {
        assertEquals(key(fullTip), key(fullTip.copy(mnemonic = null)))
        assertEquals(key(fullTip), key(fullTip.copy(config = null)))
        assertEquals(key(fullTip), key(fullTip.copy(category = listOf("editing"))))
        assertEquals(key(fullTip), key(fullTip.copy(advanced = true)))
        assertEquals(key(fullTip), key(fullTip.copy(mode = "visual")))
    }

    @Test
    fun contentKeyKeepsFieldBoundaries() {
        assertNotEquals(key(VimTip("ab", listOf("c"))), key(VimTip("a", listOf("bc"))))
        assertNotEquals(key(VimTip("s", listOf("ab", "c"))), key(VimTip("s", listOf("a", "bc"))))
        assertNotEquals(key(VimTip("adx", listOf("y"))), key(VimTip("a", listOf("x", "y"))))
    }

    // A changed golden value resets every user's tip rotation; note it in the CHANGELOG.
    @Test
    fun contentKeyDefinitionIsPinned() {
        assertEquals("14ec7dc78c3b81619551d2ea2f96863f7673d213941f5e9f9fc0d34cc865e5ff", key(fullTip).value)
    }

    private fun key(tip: VimTip): TipHash = TipHash.fromContent(tip)
}
