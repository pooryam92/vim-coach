package com.github.pooryam92.vimcoach.features.tips.integration.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStore
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipServiceIntTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        tipService().saveTips(emptyList())
    }

    fun testCountTipsAfterSave() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
        )
        tipService().saveTips(tips)

        assertEquals(2, tipService().countTips())
    }

    fun testGetRandomTipReturnsSavedTip() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
        )
        tipService().saveTips(tips)

        assertTrue(tips.contains(tipService().getRandomTip()))
    }

    fun testGetRandomTipFiltersByCategories() {
        val service = tipService()
        service.saveTips(
            listOf(
                VimTip("summary-1", listOf("details-1"), listOf("basics")),
                VimTip("summary-2", listOf("details-2"), listOf("editing"))
            )
        )

        val randomTip = service.getRandomTip(listOf("editing"))

        assertEquals("summary-2", randomTip.summary)
    }

    fun testGetRandomTipReturnsFilteredFallbackWhenNoCategoriesMatch() {
        val service = tipService()
        service.saveTips(
            listOf(VimTip("summary-1", listOf("details-1"), listOf("basics")))
        )

        val randomTip = service.getRandomTip(listOf("editing"))

        assertEquals("No tips match the selected categories.", randomTip.summary)
        assertEquals(
            "Enable at least one matching category in Vim Coach settings.",
            randomTip.details.single()
        )
    }

    fun testLoadStateReplacesTips() {
        val service = tipService()
        service.saveTips(listOf(VimTip("old-summary", listOf("old-details"))))

        tipStore().loadState(
            VimTipStore.State(
                tips = listOf(VimTip("new-summary", listOf("new-details")))
            )
        )

        assertEquals(1, service.countTips())
    }

    fun testSaveTipsStoresDerivedCategories() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1"), listOf("motions", "editing")),
            VimTip("summary-2", listOf("details-2"), listOf("editing", "search"))
        )

        tipService().saveTips(tips)

        assertEquals(
            TipCategories(listOf("motions", "editing", "search")),
            tipService().getCategories()
        )
    }

    fun testGetCategoriesBackfillsStoredCategoriesWhenCategoryCacheIsEmpty() {
        tipStore().loadState(
            VimTipStore.State(
                tips = listOf(
                    VimTip("summary-1", listOf("details-1"), listOf("motions")),
                    VimTip("summary-2", listOf("details-2"), listOf("editing", "motions"))
                ),
                categories = TipCategories(),
                metadata = TipMetadata()
            )
        )

        assertEquals(
            TipCategories(listOf("motions", "editing")),
            tipService().getCategories()
        )
        assertEquals(
            TipCategories(listOf("motions", "editing")),
            tipStore().state?.categories
        )
    }

    fun testGetCategoriesReparsesStoredTipsWhenTipsExistWithoutCategories() {
        tipStore().loadState(
            VimTipStore.State(
                tips = listOf(
                    VimTip("summary-1", listOf("details-1"), listOf("basics")),
                    VimTip("summary-2", listOf("details-2"), listOf("editing", "basics"))
                ),
                categories = TipCategories()
            )
        )

        val categories = tipService().getCategories()

        assertEquals(
            TipCategories(listOf("basics", "editing")),
            categories
        )
        assertEquals(
            TipCategories(listOf("basics", "editing")),
            tipStore().state?.categories
        )
    }

    fun testGetRandomTipReturnsEmptyMessageWhenEmpty() {
        val randomTip = tipService().getRandomTip()

        assertEquals("No tips found.", randomTip.summary)
        assertEquals("Tips have not been loaded yet.", randomTip.details.single())
    }

    private fun tipService(): VimTipService = service()

    private fun tipStore(): VimTipStore = service()
}
