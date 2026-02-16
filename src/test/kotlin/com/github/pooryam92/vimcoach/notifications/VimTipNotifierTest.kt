package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipServiceImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipNotifierTest : BasePlatformTestCase() {

    fun testCreateNotificationUsesAppTitleAndContent() {
        val notifier = VimTipNotifier(VimTipServiceImpl())
        val tip = VimTip(
            summary = "Move by word with w/b/e.",
            details = "w next word start."
        )

        val notification = notifier.createNotification(project, tip)

        assertEquals("Vim Coach", notification.title)
        assertTrue(notification.content.contains("Move by word with w/b/e."))
        assertFalse(notification.content.contains("w next word start."))
        assertEquals(1, notification.actions.size)
        assertEquals(MyBundle.message("tipMoreInfoAction"), notification.actions.single().templateText)
    }

    fun testCreateNotificationPreservesHtmlFromTipContent() {
        val notifier = VimTipNotifier(VimTipServiceImpl())
        val tip = VimTip(
            summary = "<code>w</code> motion",
            details = "line1<br/><em>line2</em>"
        )

        val notification = notifier.createNotification(project, tip)

        assertTrue(notification.content.contains("<code>w</code> motion"))
        assertFalse(notification.content.contains("line1<br/><em>line2</em>"))
        assertEquals(MyBundle.message("tipMoreInfoAction"), notification.actions.single().templateText)
    }
}
