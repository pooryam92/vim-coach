package com.github.pooryam92.vimcoach.features.tips.integration.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.xmlb.XmlSerializer

class VimTipRepositoryIntTest : BasePlatformTestCase() {

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
            "Enable a matching category, or turn on \"Show advanced tips\", in Vim Coach settings.",
            randomTip.details.single()
        )
    }

    fun testLoadStateReplacesTips() {
        val service = tipService()
        service.saveTips(listOf(VimTip("old-summary", listOf("old-details"))))

        tipStore().loadState(
            PersistentVimTipStore.State(
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
            PersistentVimTipStore.State(
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
            tipStore().state.categories
        )
    }

    fun testGetCategoriesReparsesStoredTipsWhenTipsExistWithoutCategories() {
        tipStore().loadState(
            PersistentVimTipStore.State(
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
            tipStore().state.categories
        )
    }

    // The optional `mode` field is persisted only via reflective whole-object serialization of the
    // tip cache (no explicit field wiring in PersistentVimTipStore). This round-trips the store State
    // through the same xmlb serializer the platform uses for @State components, proving mode survives
    // save/load and that an absent mode stays absent — the one seam unit tests can't reach.
    fun testModeSurvivesStoreStateSerializationRoundTrip() {
        tipService().saveTips(
            listOf(
                VimTip("insert-tip", listOf("Ctrl-r pastes a register"), mode = "insert"),
                VimTip("normal-tip", listOf("use %"))
            )
        )

        val serialized = XmlSerializer.serialize(tipStore().state)
        val restored = XmlSerializer.deserialize(serialized, PersistentVimTipStore.State::class.java)

        assertEquals("insert", restored.tips.single { it.summary == "insert-tip" }.mode)
        assertNull(restored.tips.single { it.summary == "normal-tip" }.mode)
    }

    fun testGetRandomTipReturnsEmptyMessageWhenEmpty() {
        val randomTip = tipService().getRandomTip()

        assertEquals("No tips found.", randomTip.summary)
        assertEquals("Tips have not been loaded yet.", randomTip.details.single())
    }

    private fun tipService(): VimTipRepository = service()

    private fun tipStore(): PersistentVimTipStore = service()
}
