package com.github.pooryam92.vimcoach.features.tips.integration.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element

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

    fun testGetTipsReturnsSavedTips() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
        )
        tipService().saveTips(tips)

        assertEquals(tips, tipService().getTips())
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

    // The optional `mode` and `advanced` fields are persisted only via reflective
    // whole-object serialization of the tip cache (no explicit field wiring in PersistentVimTipStore).
    // This round-trips the store State through the same xmlb serializer the platform uses for @State
    // components, proving they survive save/load and that absent/default values stay that way — the
    // one seam unit tests can't reach.
    fun testOptionalTipFieldsSurviveStoreStateSerializationRoundTrip() {
        tipService().saveTips(
            listOf(
                VimTip(
                    "insert-tip",
                    listOf("Ctrl-r pastes a register"),
                    advanced = true,
                    mode = "insert"
                ),
                VimTip("normal-tip", listOf("use %"))
            )
        )

        val serialized = XmlSerializer.serialize(tipStore().state)
        val restored = XmlSerializer.deserialize(serialized, PersistentVimTipStore.State::class.java)

        val insertTip = restored.tips.single { it.summary == "insert-tip" }
        assertEquals("insert", insertTip.mode)
        assertTrue(insertTip.advanced)

        val normalTip = restored.tips.single { it.summary == "normal-tip" }
        assertNull(normalTip.mode)
        assertFalse(normalTip.advanced)
    }

    // Caches written by 1.5.x persist a `mnemonic` option on each tip; the field has since been
    // removed, and such a cache must still load rather than lose the tips.
    fun testStoreStateWithLegacyMnemonicOptionStillDeserializes() {
        tipService().saveTips(listOf(VimTip("insert-tip", listOf("Ctrl-r pastes a register"))))
        val serialized = XmlSerializer.serialize(tipStore().state)
        val tipElement = findElements(serialized, "VimTip").single()
        tipElement.addContent(Element("option").setAttribute("name", "mnemonic").setAttribute("value", "control register"))

        val restored = XmlSerializer.deserialize(serialized, PersistentVimTipStore.State::class.java)

        assertEquals(listOf("Ctrl-r pastes a register"), restored.tips.single { it.summary == "insert-tip" }.details)
    }

    private fun findElements(root: Element, name: String): List<Element> {
        return root.children.flatMap { child ->
            (if (child.name == name) listOf(child) else emptyList()) + findElements(child, name)
        }
    }

    private fun tipService(): VimTipRepository = service()

    private fun tipStore(): PersistentVimTipStore = service()
}
