package com.github.pooryam92.vimcoach.notifications

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

        val notification = notifier.createNotification(tip)

        assertEquals("Vim Coach", notification.title)
        assertTrue(notification.content.contains("Move by word with w/b/e."))
        assertTrue(notification.content.contains("w next word start."))
    }
}
