package com.github.pooryam92.vimcoach.features.tips.unit.application.search

import com.github.pooryam92.vimcoach.features.tips.application.search.FindTips
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeSettingsService
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FindTipsUnitTest {

    private fun findTips(
        tips: List<VimTip>,
        settings: FakeSettingsService = FakeSettingsService(),
    ) = FindTips(FakeVimTipRepository(initialTips = tips), settings)

    @Test
    fun entriesKeepCorpusOrder() {
        val tips = listOf(VimTip("zeta"), VimTip("alpha"), VimTip("mid"))

        val entries = findTips(tips).entries()

        assertEquals(tips, entries.map { it.tip })
    }

    @Test
    fun searchTextCoversSummaryDetailsCategoriesMnemonicModeAndAdvancedInOriginalCase() {
        val tip = VimTip(
            summary = "Paste from Register",
            details = listOf("Press Ctrl-R then a register.", "Works in the command line too."),
            category = listOf("editing", "registers"),
            mnemonic = "R for register",
            advanced = true,
            mode = "insert",
        )

        val searchText = findTips(listOf(tip)).entries().single().searchText

        assertEquals(
            "Paste from Register Press Ctrl-R then a register. Works in the command line too. " +
                "editing registers R for register Insert mode advanced",
            searchText,
        )
    }

    @Test
    fun searchTextOmitsAbsentMnemonicModeAndAdvanced() {
        val tip = VimTip("Delete word", listOf("dw deletes a word."), listOf("editing"), mode = "unknown")

        val searchText = findTips(listOf(tip)).entries().single().searchText

        assertEquals("Delete word dw deletes a word. editing", searchText)
    }

    @Test
    fun searchTextIncludesMutedForMutedTipsOnly() {
        val muted = VimTip("Muted tip", category = listOf("editing"))
        val visible = VimTip("Visible tip", category = listOf("editing"))
        val settings = FakeSettingsService().apply { hideTip(TipHash.fromTip(muted).value) }

        val entries = findTips(listOf(muted, visible), settings).entries()

        assertEquals("Muted tip editing muted", entries[0].searchText)
        assertEquals("Visible tip editing", entries[1].searchText)
    }

    @Test
    fun mutedFlagFollowsHiddenTipHashes() {
        val muted = VimTip("muted tip")
        val visible = VimTip("visible tip")
        val settings = FakeSettingsService().apply { hideTip(TipHash.fromTip(muted).value) }

        val entries = findTips(listOf(muted, visible), settings).entries()

        assertTrue(entries[0].muted)
        assertFalse(entries[1].muted)
    }

    @Test
    fun emptyRepositoryGivesNoEntries() {
        assertTrue(findTips(emptyList()).entries().isEmpty())
    }

    @Test
    fun includesTipsTheRotationWouldFilterOut() {
        val muted = VimTip("muted tip", category = listOf("editing"))
        val advanced = VimTip("advanced tip", category = listOf("editing"), advanced = true)
        val config = VimTip("config tip", category = listOf("plugins"), config = TipConfig(lines = listOf("set number")))
        val settings = FakeSettingsService(enabledCategories = emptyList(), showAdvancedTips = false).apply {
            hideTip(TipHash.fromTip(muted).value)
        }

        val entries = findTips(listOf(muted, advanced, config), settings).entries()

        assertEquals(listOf(muted, advanced, config), entries.map { it.tip })
    }
}
