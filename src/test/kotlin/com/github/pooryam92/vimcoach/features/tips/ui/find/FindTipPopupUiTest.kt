package com.github.pooryam92.vimcoach.features.tips.ui.find

import com.github.pooryam92.vimcoach.features.tips.application.search.FindTipEntry
import com.github.pooryam92.vimcoach.features.tips.application.search.FindTips
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeSettingsService
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipRepository
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.speedSearch.ListWithFilter
import com.intellij.util.ui.UIUtil
import javax.swing.JList

class FindTipPopupUiTest : BasePlatformTestCase() {

    fun testRowShowsSummaryFirstDetailLineAndDimmedLabels() {
        val entry = entry(
            VimTip(
                summary = "Paste from register",
                details = listOf("Press Ctrl-R then a register.", "Second line."),
                category = listOf("editing", "registers"),
                advanced = true,
                mode = "insert",
            ),
            muted = true,
        )

        val renderer = render(entry)

        assertEquals("Paste from register", renderer.summary.text())
        assertEquals("Press Ctrl-R then a register.", renderer.detail.text())
        assertEquals("Advanced · Insert mode · Muted · editing, registers", renderer.labels.text())
        assertEquals(SimpleTextAttributes.GRAYED_ATTRIBUTES, renderer.labels.firstAttributes())
        assertEquals(SimpleTextAttributes.GRAYED_ATTRIBUTES, renderer.detail.firstAttributes())
    }

    fun testLabelsAppearOnlyWhenPresent() {
        assertEquals("", render(entry(VimTip("plain"))).labels.text())
        assertEquals("Visual mode", render(entry(VimTip("visual", mode = "visual"))).labels.text())
        assertEquals("Muted · navigation", render(entry(VimTip("muted", category = listOf("navigation")), muted = true)).labels.text())
    }

    fun testRowAccessibleNameReadsSummaryLabelsThenDetail() {
        val entry = entry(
            VimTip("Paste from register", listOf("Press Ctrl-R then a register."), listOf("registers"), mode = "insert"),
            muted = true,
        )

        val row = FindTipRenderer().getListCellRendererComponent(JBList(listOf(entry)), entry, 0, false, false)

        assertEquals(
            "Paste from register Insert mode · Muted · registers Press Ctrl-R then a register.",
            row.accessibleContext.accessibleName,
        )
    }

    fun testRowWithoutDetailsLeavesSecondLineEmpty() {
        assertEquals("", render(entry(VimTip("no details"))).detail.text())
    }

    fun testRendererIsReusedWithoutLeakingPreviousRow() {
        val renderer = FindTipRenderer()
        val list = JBList(emptyList<FindTipEntry>())
        renderer.getListCellRendererComponent(list, entry(VimTip("first", listOf("d1"), advanced = true)), 0, false, false)

        renderer.getListCellRendererComponent(list, entry(VimTip("second")), 1, false, false)

        assertEquals("second", renderer.summary.text())
        assertEquals("", renderer.labels.text())
        assertEquals("", renderer.detail.text())
    }

    fun testChoosingRowPassesItsTipToCallback() {
        val entries = listOf(entry(VimTip("first")), entry(VimTip("second")))
        var chosen: VimTip? = null
        val popup = FindTipPopup.create(entries) { chosen = it }
        try {
            val list = UIUtil.findComponentOfType(popup.content, JList::class.java)!!
            list.selectedIndex = 1

            popup.closeOk(null)
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        } finally {
            if (!popup.isDisposed) Disposer.dispose(popup)
        }

        assertSame(entries[1].tip, chosen)
    }

    fun testTypingFiltersRowsOnSearchTextIncludingCategoryWords() {
        val tips = listOf(
            VimTip("Record a macro", listOf("Press q then a letter."), listOf("macros", "registers")),
            VimTip("Jump to line", listOf("Type 42G."), listOf("navigation")),
            VimTip("Named registers", listOf("Prefix a yank with a quote and a letter."), listOf("editing")),
        )
        val entries = FindTips(FakeVimTipRepository(initialTips = tips), FakeSettingsService()).entries()
        val popup = FindTipPopup.create(entries) {}
        try {
            val filtered = UIUtil.findComponentOfType(popup.content, ListWithFilter::class.java)!!
            assertEquals(3, filtered.list.model.size)

            filtered.speedSearch.type("registers")
            filtered.speedSearch.update()

            val shown = (0 until filtered.list.model.size).map {
                (filtered.list.model.getElementAt(it) as FindTipEntry).tip.summary
            }
            assertEquals(listOf("Record a macro", "Named registers"), shown)
        } finally {
            Disposer.dispose(popup)
        }
    }

    private fun entry(tip: VimTip, muted: Boolean = false) =
        FindTipEntry(tip = tip, muted = muted, searchText = tip.summary.lowercase())

    private fun render(entry: FindTipEntry): FindTipRenderer {
        val renderer = FindTipRenderer()
        renderer.getListCellRendererComponent(JBList(listOf(entry)), entry, 0, false, false)
        return renderer
    }

    private fun SimpleColoredComponent.text(): String = getCharSequence(false).toString()

    private fun SimpleColoredComponent.firstAttributes(): SimpleTextAttributes =
        iterator().also { it.next() }.textAttributes
}
