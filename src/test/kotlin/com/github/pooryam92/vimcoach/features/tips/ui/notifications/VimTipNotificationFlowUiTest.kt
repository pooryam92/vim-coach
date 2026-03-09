package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipService
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipNotificationFlowUiTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        VimTipNotifier.activeTipNotifications.clear()
    }

    override fun tearDown() {
        try {
            VimTipNotifier.activeTipNotifications.clear()
        } finally {
            super.tearDown()
        }
    }

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

    fun testShowRandomTipIfNoneActiveShowsWhenNoActiveNotificationExists() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip 1", listOf("Details 1")))
        )
        val notifier = VimTipNotifier(mockTipService)

        val shown = notifier.showRandomTipIfNoneActive(project)
        val notification = VimTipNotifier.activeTipNotifications[project]

        assertTrue(shown)
        assertEquals(1, mockTipService.getRandomTipCalls)
        assertNotNull(notification)
        assertFalse(notification!!.isExpired)
    }

    fun testShowRandomTipReplacesPreviousTipNotificationForProject() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
                VimTip("Tip 1", listOf("Details 1")),
                VimTip("Tip 2", listOf("Details 2"))
            )
        )
        val notifier = VimTipNotifier(mockTipService)

        notifier.showRandomTip(project)
        val firstNotification = VimTipNotifier.activeTipNotifications[project]
        notifier.showRandomTip(project)
        val secondNotification = VimTipNotifier.activeTipNotifications[project]

        assertNotNull(firstNotification)
        assertNotNull(secondNotification)
        assertEquals(1, VimTipNotifier.activeTipNotifications.size)
        assertTrue(firstNotification!!.isExpired)
        assertFalse(secondNotification!!.isExpired)
    }

    fun testShowRandomTipIfNoneActiveDoesNotReplaceVisibleNotification() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
                VimTip("Tip 1", listOf("Details 1")),
                VimTip("Tip 2", listOf("Details 2"))
            )
        )
        val notifier = VimTipNotifier(mockTipService)

        notifier.showRandomTip(project)
        val firstNotification = VimTipNotifier.activeTipNotifications[project]

        val shown = notifier.showRandomTipIfNoneActive(project)
        val secondNotification = VimTipNotifier.activeTipNotifications[project]

        assertFalse(shown)
        assertEquals(1, mockTipService.getRandomTipCalls)
        assertSame(firstNotification, secondNotification)
        assertFalse(firstNotification!!.isExpired)
    }

    fun testShowRandomTipIfNoneActiveShowsAfterTrackedNotificationExpires() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
                VimTip("Tip 1", listOf("Details 1")),
                VimTip("Tip 2", listOf("Details 2"))
            )
        )
        val notifier = VimTipNotifier(mockTipService)

        notifier.showRandomTip(project)
        val firstNotification = VimTipNotifier.activeTipNotifications[project]
        firstNotification!!.expire()

        val shown = notifier.showRandomTipIfNoneActive(project)
        val secondNotification = VimTipNotifier.activeTipNotifications[project]

        assertTrue(shown)
        assertEquals(2, mockTipService.getRandomTipCalls)
        assertNotNull(secondNotification)
        assertNotSame(firstNotification, secondNotification)
        assertFalse(secondNotification!!.isExpired)
    }
}
