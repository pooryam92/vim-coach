package com.github.pooryam92.vimcoach.features.tips.integration.entrypoints.notifications

import com.github.pooryam92.vimcoach.features.tips.application.notifications.ShowTips
import com.github.pooryam92.vimcoach.features.tips.application.search.FindTipEntry
import com.github.pooryam92.vimcoach.features.tips.application.search.FindTips
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.entrypoints.notifications.FindTipAction
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeSettingsService
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipRepository
import com.github.pooryam92.vimcoach.features.tips.ui.find.FindTipView
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class FindTipActionIntTest : BasePlatformTestCase() {

    private val view = FakeFindTipView()
    private val showTips = FakeShowTips()

    fun testEmptyCacheShowsNoTipsMessageInsteadOfChooser() {
        val action = action(tips = emptyList())

        action.actionPerformed(projectEvent(action))

        assertEquals(listOf(project), view.noTipsShownFor)
        assertNull(view.shownEntries)
    }

    fun testChosenTipIsShownDirectlyInsteadOfDrawnAtRandom() {
        val first = VimTip("first")
        val second = VimTip("second")
        val action = action(tips = listOf(first, second))

        action.actionPerformed(projectEvent(action))
        view.onChosen!!(second)

        assertSame(project, view.shownFor)
        assertEquals(listOf(first, second), view.shownEntries!!.map { it.tip })
        assertTrue(view.noTipsShownFor.isEmpty())
        assertEquals(listOf(second), showTips.shownTips)
        assertEquals(0, showTips.randomTipCalls)
    }

    fun testEnabledOnlyWithProject() {
        val action = action(tips = emptyList())
        val withProject = projectEvent(action)
        val withoutProject = TestActionEvent.createTestEvent(action, DataContext.EMPTY_CONTEXT)

        action.update(withProject)
        action.update(withoutProject)

        assertTrue(withProject.presentation.isEnabled)
        assertFalse(withoutProject.presentation.isEnabled)
    }

    private fun action(tips: List<VimTip>) = FindTipAction(
        findTips = { FindTips(FakeVimTipRepository(initialTips = tips), FakeSettingsService()) },
        view = view,
        showTips = { showTips },
    )

    private fun projectEvent(action: FindTipAction): AnActionEvent =
        TestActionEvent.createTestEvent(action, SimpleDataContext.getProjectContext(project))

    private class FakeFindTipView : FindTipView {
        var shownFor: Project? = null
        var shownEntries: List<FindTipEntry>? = null
        var onChosen: ((VimTip) -> Unit)? = null
        val noTipsShownFor = mutableListOf<Project>()

        override fun show(project: Project, entries: List<FindTipEntry>, onChosen: (VimTip) -> Unit) {
            shownFor = project
            shownEntries = entries
            this.onChosen = onChosen
        }

        override fun showNoTipsLoaded(project: Project) {
            noTipsShownFor.add(project)
        }
    }

    private class FakeShowTips : ShowTips {
        val shownTips = mutableListOf<VimTip>()
        var randomTipCalls = 0

        override fun showRandomTip() {
            randomTipCalls += 1
        }

        override fun showRandomTipIfNoneActive(): Boolean {
            randomTipCalls += 1
            return true
        }

        override fun showTip(tip: VimTip) {
            shownTips.add(tip)
        }
    }
}
