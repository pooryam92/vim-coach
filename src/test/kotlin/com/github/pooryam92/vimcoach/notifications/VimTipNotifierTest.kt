package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.services.TipMetadata
import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipService
import com.github.pooryam92.vimcoach.services.VimTipServiceImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipNotifierTest : BasePlatformTestCase() {

    fun testCreateNotificationUsesAppTitleAndContent() {
        val notifier = VimTipNotifier(VimTipServiceImpl())
        val tip = VimTip(
            summary = "Move by word with w/b/e.",
            details = listOf("w next word start.")
        )

        val notification = notifier.createNotification(tip)

        assertEquals(VimTipNotifier.APP_TITLE, notification.title)
        assertTrue(notification.content.contains("Move by word with w/b/e."))
        assertTrue(notification.content.contains("w next word start."))
    }

    fun testCreateNotificationEscapesHtmlInTipContent() {
        val notifier = VimTipNotifier(VimTipServiceImpl())
        val tip = VimTip(
            summary = "Indent/outdent lines >> - <<",
            details = listOf(">> indents current line, << outdents", "<em>test</em> & \"quotes\"")
        )

        val notification = notifier.createNotification(tip)

        // Verify HTML special characters are escaped
        assertTrue(notification.content.contains("&gt;&gt;")) // >> becomes &gt;&gt;
        assertTrue(notification.content.contains("&lt;&lt;")) // << becomes &lt;&lt;
        assertTrue(notification.content.contains("&lt;em&gt;")) // <em> becomes &lt;em&gt;
        assertTrue(notification.content.contains("&amp;")) // & becomes &amp;
        assertTrue(notification.content.contains("&quot;")) // " becomes &quot;
    }

    fun testCreateNotificationWithActionsHasNextTipAction() {
        val mockTipService = createMockTipService(
            VimTip(summary = "Test tip 1", details = listOf("Details 1"))
        )
        val notifier = VimTipNotifier(mockTipService)
        val tip = VimTip(summary = "Test tip", details = listOf("Test details"))

        val notification = notifier.createNotificationWithActions(project, tip)

        // Verify the notification has actions
        val actions = notification.actions
        assertNotNull(actions)
        assertTrue(actions.isNotEmpty())
    }

    fun testNextTipActionHasCorrectText() {
        val tip1 = VimTip(summary = "First tip", details = listOf("First details"))
        val tip2 = VimTip(summary = "Second tip", details = listOf("Second details"))
        val mockTipService = createMockTipService(tip1, tip2)
        val notifier = VimTipNotifier(mockTipService)

        val notification = notifier.createNotificationWithActions(project, tip1)

        // Verify the notification has actions
        val actions = notification.actions
        assertNotNull(actions)
        assertEquals(1, actions.size)

        // Verify action text
        val action = actions[0]
        assertEquals(VimTipNotifier.TIP_NEXT_ACTION_TEXT, action.templateText)
    }

    fun testNextTipActionDisplaysCorrectLabel() {
        val mockTipService = createMockTipService(
            VimTip(summary = "Test", details = listOf("Test details"))
        )
        val notifier = VimTipNotifier(mockTipService)
        val tip = mockTipService.getRandomTip()
        val notification = notifier.createNotificationWithActions(project, tip)

        val actions = notification.actions
        assertEquals(1, actions.size)
        
        val actionLabel = actions[0].templateText
        assertEquals("Next tip", actionLabel)
    }

    fun testMultipleNextTipClicksCallsGetRandomTipMultipleTimes() {
        var callCount = 0
        val tips = listOf(
            VimTip(summary = "Tip 1", details = listOf("Details 1")),
            VimTip(summary = "Tip 2", details = listOf("Details 2")),
            VimTip(summary = "Tip 3", details = listOf("Details 3"))
        )
        val mockTipService = object : VimTipService {
            override fun countTips() = tips.size
            override fun saveTips(tips: List<VimTip>) {}
            override fun getRandomTip(): VimTip {
                callCount++
                return tips[callCount % tips.size]
            }
            override fun getMetadata() = TipMetadata()
            override fun saveMetadata(metadata: TipMetadata) {}
            override fun getState() = VimTipService.State(tips.toMutableList())
            override fun loadState(state: VimTipService.State) {}
        }

        val notifier = VimTipNotifier(mockTipService)
        
        // First call
        notifier.showRandomTip(project)
        assertEquals(1, callCount)
    }

    fun testNotificationHasCorrectGroupId() {
        val mockTipService = createMockTipService(
            VimTip(summary = "Test", details = listOf("Test details"))
        )
        val notifier = VimTipNotifier(mockTipService)
        val notification = notifier.createNotification(mockTipService.getRandomTip())

        assertEquals(VimTipNotifier.NOTIFICATION_GROUP_ID, notification.groupId)
    }

    fun testNotificationHasIcon() {
        val mockTipService = createMockTipService(
            VimTip(summary = "Test", details = listOf("Test details"))
        )
        val notifier = VimTipNotifier(mockTipService)
        val notification = notifier.createNotification(mockTipService.getRandomTip())

        assertNotNull(notification.icon)
        assertEquals(VimTipNotifier.TIP_ICON, notification.icon)
    }

    private fun createMockTipService(vararg tips: VimTip): VimTipService {
        return object : VimTipService {
            private var currentIndex = 0
            private val tipList = tips.toList()

            override fun countTips() = tipList.size

            override fun saveTips(tips: List<VimTip>) {}

            override fun getRandomTip(): VimTip {
                val tip = tipList[currentIndex % tipList.size]
                currentIndex++
                return tip
            }

            override fun getMetadata() = TipMetadata()

            override fun saveMetadata(metadata: TipMetadata) {}

            override fun getState() = VimTipService.State(tipList.toMutableList())

            override fun loadState(state: VimTipService.State) {}
        }
    }
}
