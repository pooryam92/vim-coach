package com.github.pooryam92.vimcoach.features.tips.unit.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.VimTipNotifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VimTipNotifierUnitTest {

    @Test
    fun createNotificationUsesAppTitleAndContent() {
        val notifier = VimTipNotifier(createMockTipService())
        val tip = VimTip(
            summary = "Move by word with w/b/e.",
            details = listOf("w next word start.")
        )

        val notification = notifier.createNotification(tip)

        assertEquals(VimTipNotifier.APP_TITLE, notification.title)
        assertTrue(notification.content.contains("Move by word with w/b/e."))
        assertTrue(notification.content.contains("w next word start."))
    }

    @Test
    fun createNotificationEscapesHtmlInTipContent() {
        val notifier = VimTipNotifier(createMockTipService())
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
        val notifier = VimTipNotifier(createMockTipService())
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
        val notifier = VimTipNotifier(createMockTipService(tip))

        val notification = notifier.createNotification(tip)

        assertEquals(VimTipNotifier.NOTIFICATION_GROUP_ID, notification.groupId)
        assertNotNull(notification.icon)
        assertEquals(VimTipNotifier.TIP_ICON, notification.icon)
    }

    private fun createMockTipService(vararg tips: VimTip): VimTipService {
        val tipList = if (tips.isEmpty()) {
            listOf(VimTip("fallback", listOf("fallback-details")))
        } else {
            tips.toList()
        }

        return object : VimTipService {
            private var currentIndex = 0

            override fun countTips() = tipList.size

            override fun saveTips(tips: List<VimTip>) = Unit

            override fun getRandomTip(): VimTip {
                val tip = tipList[currentIndex % tipList.size]
                currentIndex += 1
                return tip
            }

            override fun getMetadata() = TipMetadata()

            override fun saveMetadata(metadata: TipMetadata) = Unit

            override fun getState() = VimTipService.State(tipList.toMutableList())

            override fun loadState(state: VimTipService.State) = Unit
        }
    }
}
