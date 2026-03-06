package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipService
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipNotificationFlowUiTest : BasePlatformTestCase() {

    fun testNotificationAddsNextTipAction() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
            VimTip(summary = "Tip 1", details = listOf("Details 1")),
            VimTip(summary = "Tip 2", details = listOf("Details 2"))
            )
        )
        val notifier = VimTipNotifier(mockTipService)

        val notification = notifier.createNotificationWithActions(project, mockTipService.getRandomTip())

        assertEquals(1, notification.actions.size)
        assertEquals(VimTipNotifier.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
    }

    fun testShowRandomTipRequestsTipFromService() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip", listOf("Details")))
        )
        val notifier = VimTipNotifier(mockTipService)

        notifier.showRandomTip(project)

        assertEquals(1, mockTipService.getRandomTipCalls)
    }
}
