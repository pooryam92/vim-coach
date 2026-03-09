package com.github.pooryam92.vimcoach.features.tips.unit.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TipNotificationFactoryUnitTest {

    @Test
    fun createNotificationUsesAppTitleAndContent() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Move by word with w/b/e.",
            details = listOf("w next word start.")
        )

        val notification = notifier.createNotification(tip)

        assertEquals(TipNotificationFactory.APP_TITLE, notification.title)
        assertTrue(notification.content.contains("Move by word with w/b/e."))
        assertTrue(notification.content.contains("w next word start."))
    }

    @Test
    fun createNotificationEscapesHtmlInTipContent() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Indent/outdent lines >> - <<",
            details = listOf(">> indents current line, << outdents", "<em>test</em> & \"quotes\"")
        )

        val notification = notifier.createNotification(tip)

        assertTrue(notification.content.contains("&gt;&gt;"))
        assertTrue(notification.content.contains("&lt;&lt;"))
        assertTrue(notification.content.contains("&lt;em&gt;"))
        assertTrue(notification.content.contains("&amp;"))
        assertTrue(notification.content.contains("&quot;"))
    }

    @Test
    fun createNotificationKeepsUnicodeLiteralsAndEscapesHtmlOnly() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Repeat last change .",
            details = listOf("5j → move down 5 lines", "literal <tag>")
        )

        val notification = notifier.createNotification(tip)

        assertTrue(notification.content.contains("Repeat last change ."))
        assertTrue(notification.content.contains("5j → move down 5 lines"))
        assertTrue(notification.content.contains("literal &lt;tag&gt;"))
        assertFalse(notification.content.contains("literal <tag>"))
    }

    @Test
    fun notificationHasCorrectGroupIdAndIcon() {
        val tip = VimTip(summary = "Test", details = listOf("Test details"))
        val notifier = TipNotificationFactory()

        val notification = notifier.createNotification(tip)

        assertEquals(TipNotificationFactory.NOTIFICATION_GROUP_ID, notification.groupId)
        assertNotNull(notification.icon)
        assertEquals(TipNotificationFactory.TIP_ICON, notification.icon)
    }
}
