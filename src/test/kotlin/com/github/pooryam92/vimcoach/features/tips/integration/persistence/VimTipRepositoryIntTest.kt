package com.github.pooryam92.vimcoach.features.tips.integration.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
import com.intellij.openapi.components.service
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.xmlb.XmlSerializer

class VimTipRepositoryIntTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        tipService().saveTips(emptyList())
    }

    fun testCountTipsAfterSave() {
        val tips = listOf(
            vimTip("summary-1", listOf("details-1")),
            vimTip("summary-2", listOf("details-2"))
        )
        tipService().saveTips(tips)

        assertEquals(2, tipService().countTips())
    }

    fun testGetTipsReturnsSavedTips() {
        val tips = listOf(
            vimTip("summary-1", listOf("details-1")),
            vimTip("summary-2", listOf("details-2"))
        )
        tipService().saveTips(tips)

        assertEquals(tips, tipService().getTips())
    }

    fun testLoadStateReplacesTips() {
        val service = tipService()
        service.saveTips(listOf(vimTip("old-summary", listOf("old-details"))))

        tipStore().loadState(
            PersistentVimTipStore.State(
                tips = listOf(vimTip("new-summary", listOf("new-details")))
            )
        )

        assertEquals(1, service.countTips())
    }

    fun testSaveTipsStoresDerivedCategories() {
        val tips = listOf(
            vimTip("summary-1", listOf("details-1"), listOf("motions", "editing")),
            vimTip("summary-2", listOf("details-2"), listOf("editing", "search"))
        )

        tipService().saveTips(tips)

        assertEquals(
            TipCategories(listOf("motions", "editing", "search")),
            tipService().getCategories()
        )
    }

    // TODO(1.6.0 upgrade bridge)
    fun testGetCategoriesBackfillsStoredCategoriesWhenCategoryCacheIsEmpty() {
        tipStore().loadState(
            PersistentVimTipStore.State(
                tips = listOf(
                    vimTip("summary-1", listOf("details-1"), listOf("motions")),
                    vimTip("summary-2", listOf("details-2"), listOf("editing", "motions"))
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

    // TODO(1.6.0 upgrade bridge)
    fun testGetCategoriesReparsesStoredTipsWhenTipsExistWithoutCategories() {
        tipStore().loadState(
            PersistentVimTipStore.State(
                tips = listOf(
                    vimTip("summary-1", listOf("details-1"), listOf("basics")),
                    vimTip("summary-2", listOf("details-2"), listOf("editing", "basics"))
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

    // A cache written before generated ids holds id-less tips; they must load as no tips so the
    // "No tips found." fallback shows and the next update check refetches unconditionally.
    // TODO(1.6.0 upgrade bridge)
    fun testIdLessCacheFromXmlLoadsAsNoTipsKeepingCategories() {
        tipStore().loadState(XmlSerializer.deserialize(JDOMUtil.load(PRE_ID_CACHE_XML), PersistentVimTipStore.State::class.java))

        assertEquals(emptyList<VimTip>(), tipStore().state.tips)
        assertEquals(0, tipService().countTips())
        assertEquals(TipCategories(listOf("motions")), tipService().getCategories())
    }

    fun testGetTipsByIdsReturnsTipsInRequestedOrder() {
        val first = vimTip("summary-1", listOf("details-1"), id = "id-1")
        val second = vimTip("summary-2", listOf("details-2"), id = "id-2")
        tipService().saveTips(listOf(first, second))

        assertEquals(listOf(second, first), tipService().getTipsByIds(listOf(" id-2 ", "missing", "id-1")))
    }

    // The optional `mode`, `advanced`, and `mnemonic` fields are persisted only via reflective
    // whole-object serialization of the tip cache (no explicit field wiring in PersistentVimTipStore).
    // This round-trips the store State through the same xmlb serializer the platform uses for @State
    // components, proving they survive save/load and that absent/default values stay that way — the
    // one seam unit tests can't reach.
    fun testOptionalTipFieldsSurviveStoreStateSerializationRoundTrip() {
        tipService().saveTips(
            listOf(
                vimTip(
                    "insert-tip",
                    listOf("Ctrl-r pastes a register"),
                    mnemonic = "control register",
                    advanced = true,
                    mode = "insert",
                    id = "insert-id"
                ),
                vimTip("normal-tip", listOf("use %"))
            )
        )

        val serialized = XmlSerializer.serialize(tipStore().state)
        val restored = XmlSerializer.deserialize(serialized, PersistentVimTipStore.State::class.java)

        val insertTip = restored.tips.single { it.summary == "insert-tip" }
        assertEquals("insert-id", insertTip.id)
        assertEquals("insert", insertTip.mode)
        assertTrue(insertTip.advanced)
        assertEquals("control register", insertTip.mnemonic)

        val normalTip = restored.tips.single { it.summary == "normal-tip" }
        assertNull(normalTip.mode)
        assertFalse(normalTip.advanced)
        assertNull(normalTip.mnemonic)
    }

    private fun tipService(): VimTipRepository = service()

    private fun tipStore(): PersistentVimTipStore = service()

    private companion object {
        // A tip cache as written by a plugin version before generated ids: no `id` option.
        val PRE_ID_CACHE_XML = """
            <State>
              <option name="categories">
                <TipCategories>
                  <option name="values">
                    <list>
                      <option value="motions" />
                    </list>
                  </option>
                </TipCategories>
              </option>
              <option name="tips">
                <list>
                  <VimTip>
                    <option name="category">
                      <list>
                        <option value="motions" />
                      </list>
                    </option>
                    <option name="details">
                      <list>
                        <option value="pre-id-details" />
                      </list>
                    </option>
                    <option name="summary" value="pre-id-summary" />
                  </VimTip>
                </list>
              </option>
            </State>
        """.trimIndent()
    }
}
