package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.application.TipNotificationServiceImpl
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipService
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TipNotificationServiceUiTest : BasePlatformTestCase() {

    fun testNotificationAddsNextTipAction() {
        val notifier = TipNotificationFactory()
        val notification = notifier.createNotificationWithActions(
            VimTip(summary = "Tip 1", details = listOf("Details 1"))
        ) {}

        assertEquals(1, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
    }

    fun testShowRandomTipRequestsTipFromService() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip", listOf("Details")))
        )
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()

        assertEquals(1, mockTipService.getRandomTipCalls)
    }

    fun testShowRandomTipIfNoneActiveShowsWhenNoActiveNotificationExists() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip 1", listOf("Details 1")))
        )
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

        val shown = controller.showRandomTipIfNoneActive()
        val notification = controller.activeNotification

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
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()
        val firstNotification = controller.activeNotification
        controller.showRandomTip()
        val secondNotification = controller.activeNotification

        assertNotNull(firstNotification)
        assertNotNull(secondNotification)
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
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()
        val firstNotification = controller.activeNotification

        val shown = controller.showRandomTipIfNoneActive()
        val secondNotification = controller.activeNotification

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
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()
        val firstNotification = controller.activeNotification
        firstNotification!!.expire()

        val shown = controller.showRandomTipIfNoneActive()
        val secondNotification = controller.activeNotification

        assertTrue(shown)
        assertEquals(2, mockTipService.getRandomTipCalls)
        assertNotNull(secondNotification)
        assertNotSame(firstNotification, secondNotification)
        assertFalse(secondNotification!!.isExpired)
    }

    fun testShowRandomTipIfNoneActiveShowsAfterTrackedNotificationHasNoBalloon() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
                VimTip("Tip 1", listOf("Details 1")),
                VimTip("Tip 2", listOf("Details 2"))
            )
        )
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()
        val firstNotification = controller.activeNotification

        val shown = controller.showRandomTipIfNoneActive()
        val secondNotification = controller.activeNotification

        assertTrue(shown)
        assertEquals(2, mockTipService.getRandomTipCalls)
        assertNotNull(secondNotification)
        assertNotSame(firstNotification, secondNotification)
        assertFalse(secondNotification!!.isExpired)
    }
}
