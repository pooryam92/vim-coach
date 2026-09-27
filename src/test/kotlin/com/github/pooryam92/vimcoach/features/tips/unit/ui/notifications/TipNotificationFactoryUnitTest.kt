package com.github.pooryam92.vimcoach.features.tips.unit.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipKeySpan
import com.github.pooryam92.vimcoach.features.tips.domain.TipMode
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationActions
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.intellij.ui.ColorUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun createNotificationLeavesTitlePlainWhenNoLabels() {
        val notifier = TipNotificationFactory()
        val normalTip = VimTip(summary = "jump", details = listOf("use %"))

        val notification = notifier.createNotification(normalTip)

        assertEquals(TipNotificationFactory.APP_TITLE, notification.title)
    }

    @Test
    fun createNotificationMarksAdvancedTipsInTheTitle() {
        val notifier = TipNotificationFactory()
        val advancedTip = VimTip(
            summary = "Paste last search Ctrl-r /",
            details = listOf("Ctrl-r / pastes the last search"),
            advanced = true
        )

        val title = notifier.createNotification(advancedTip).title

        assertTrue("advanced title renders HTML", title.startsWith("<html>"))
        assertTrue(title.contains(TipNotificationFactory.APP_TITLE))
        assertTrue("dimmed separator precedes the label", followsDimmedSeparator(title, TipNotificationFactory.ADVANCED_LABEL))
    }

    @Test
    fun createNotificationLabelsTheModeInTheTitle() {
        val notifier = TipNotificationFactory()
        for (mode in TipMode.entries) {
            val tip = VimTip(summary = "tip ${mode.wireValue}", details = listOf("d"), mode = mode.wireValue)

            val title = notifier.createNotification(tip).title

            assertTrue("mode ${mode.wireValue} renders HTML", title.startsWith("<html>"))
            assertTrue("mode ${mode.wireValue} shows ${mode.label} after a separator", followsDimmedSeparator(title, mode.label))
        }
    }

    // The keys in the body carry the balloon's accent colour, so the title labels stay uncoloured.
    @Test
    fun createNotificationKeepsTitleLabelsUncoloured() {
        val notifier = TipNotificationFactory()
        for (mode in TipMode.entries) {
            val tip = VimTip(summary = "m ${mode.wireValue}", details = listOf("d"), advanced = true, mode = mode.wireValue)

            val title = notifier.createNotification(tip).title

            assertNull("advanced label is plain", labelColor(title, TipNotificationFactory.ADVANCED_LABEL))
            assertNull("${mode.label} is plain", labelColor(title, mode.label))
            assertEquals("only the two separators are coloured", 2, Regex("<span ").findAll(title).count())
        }
    }

    @Test
    fun createNotificationOrdersAdvancedBeforeMode() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Paste register Ctrl-r",
            details = listOf("Ctrl-r pastes a register in Insert"),
            advanced = true,
            mode = "insert"
        )

        val title = notifier.createNotification(tip).title

        val advancedAt = title.indexOf(TipNotificationFactory.ADVANCED_LABEL)
        val modeAt = title.indexOf(TipMode.INSERT.label)
        assertTrue("advanced label present", advancedAt >= 0)
        assertTrue("advanced precedes mode", advancedAt < modeAt)
    }

    @Test
    fun createNotificationLeavesTitlePlainForUnknownMode() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(summary = "jump", details = listOf("use %"), mode = "normal")

        val notification = notifier.createNotification(tip)

        assertEquals(TipNotificationFactory.APP_TITLE, notification.title)
    }

    @Test
    fun advancedTipsAvailableNotificationOffersSettingsAction() {
        val notifier = TipNotificationFactory()

        val notification = notifier.createAdvancedTipsAvailableNotification {}

        assertEquals(TipNotificationFactory.ADVANCED_TIPS_AVAILABLE_TEXT, notification.content)
        assertEquals(1, notification.actions.size)
        assertEquals(
            TipNotificationFactory.ADVANCED_TIPS_OPEN_SETTINGS_ACTION_TEXT,
            notification.actions.single().templateText
        )
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
    fun createNotificationStylesKeysInSummaryAndDetails() {
        val summary = "Pick completions Ctrl-n / Ctrl-p"
        val details = listOf("As you type, completions pop up", "Ctrl-y accepts")
        val tip = VimTip(
            summary = summary,
            details = details,
            keys = listOf(keySpan(0, summary, "Ctrl-n"), keySpan(0, summary, "Ctrl-p"), keySpan(2, details[1], "Ctrl-y"))
        )

        val content = TipNotificationFactory().createNotification(tip).content

        assertEquals(listOf("Ctrl-n", "Ctrl-p", "Ctrl-y"), styledKeys(content))
        assertTrue(content.contains("Pick completions "))
        assertTrue(content.contains("As you type, completions pop up"))
        assertTrue(content.contains(" accepts"))
    }

    @Test
    fun createNotificationEscapesHtmlInsideStyledKeys() {
        val summary = "Indent lines >> and <<"
        val tip = VimTip(
            summary = summary,
            details = listOf("d"),
            keys = listOf(keySpan(0, summary, ">>"), keySpan(0, summary, "<<"))
        )

        val content = TipNotificationFactory().createNotification(tip).content

        assertEquals(listOf("&gt;&gt;", "&lt;&lt;"), styledKeys(content))
    }

    @Test
    fun createNotificationSkipsOverlappingAndOutOfRangeKeysKeepingTheText() {
        val tip = VimTip(
            summary = "dd deletes",
            details = listOf("x"),
            keys = listOf(
                TipKeySpan(0, 1, 3),
                TipKeySpan(0, 0, 2),
                TipKeySpan(0, 5, 99),
                TipKeySpan(7, 0, 1),
                TipKeySpan(1, 1, 1)
            )
        )

        val content = TipNotificationFactory().createNotification(tip).content

        assertEquals(listOf("dd"), styledKeys(content))
        assertTrue(content.contains("</span> deletes"))
        assertTrue(content.contains("x"))
    }

    @Test
    fun createNotificationLeavesTextUnstyledWithoutKeys() {
        val tip = VimTip(summary = "Delete lines dd", details = listOf("dd deletes"))

        val content = TipNotificationFactory().createNotification(tip).content

        assertEquals(emptyList<String>(), styledKeys(content))
        assertTrue(content.contains("Delete lines dd"))
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

    @Test
    fun notificationWithIdeaVimRcCallbackShowsThreeActionButtons() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(summary = "surround", details = listOf("edit surroundings"))

        val notification = notifier.createNotificationWithActions(
            tip,
            TipNotificationActions(
                onShowNextTip = {},
                onExcludeTip = {},
                onAddToIdeaVimRc = {}
            )
        )

        assertEquals(3, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
        assertEquals(TipNotificationFactory.TIP_DONT_SHOW_AGAIN_ACTION_TEXT, notification.actions[1].templateText)
        assertEquals(TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT, notification.actions[2].templateText)
    }

    @Test
    fun namedConfigApplyButtonShowsTheNameVerbatim() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Add surroundings ys{motion}",
            details = listOf("ysiw) wraps a word in parens"),
            category = listOf("plugins", "editing"),
            config = TipConfig(name = "Install vim-surround", lines = listOf("Plug 'tpope/vim-surround'"))
        )

        val notification = notifier.createNotificationWithActions(
            tip,
            TipNotificationActions(onShowNextTip = {}, onExcludeTip = {}, onAddToIdeaVimRc = {})
        )

        assertEquals("Install vim-surround", notification.actions[2].templateText)
    }

    @Test
    fun unnamedConfigApplyButtonUsesGenericLabel() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Keep lines visible while scrolling",
            details = listOf("set scrolloff=5"),
            category = listOf("options"),
            config = TipConfig(name = null, lines = listOf("set scrolloff=5"))
        )

        val notification = notifier.createNotificationWithActions(
            tip,
            TipNotificationActions(onShowNextTip = {}, onExcludeTip = {}, onAddToIdeaVimRc = {})
        )

        assertEquals(
            TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT,
            notification.actions[2].templateText
        )
    }

    @Test
    fun noteActionIsAddedLastWhenRecordNoteCallbackProvided() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(summary = "surround", details = listOf("edit surroundings"))

        val notification = notifier.createNotificationWithActions(
            tip,
            TipNotificationActions(onShowNextTip = {}, onExcludeTip = {}, onAddToIdeaVimRc = {}, onRecordNote = {})
        )

        assertEquals(4, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_NOTE_ACTION_TEXT, notification.actions.last().templateText)
    }

    @Test
    fun noNoteActionWhenRecordNoteCallbackAbsent() {
        val notifier = TipNotificationFactory()

        val notification = notifier.createNotificationWithActions(
            VimTip(summary = "tip"),
            TipNotificationActions(onShowNextTip = {}, onExcludeTip = {})
        )

        assertFalse(notification.actions.any { it.templateText == TipNotificationFactory.TIP_NOTE_ACTION_TEXT })
    }

    @Test
    fun notificationWithoutIdeaVimRcCallbackHasTwoActionButtons() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(summary = "jump", details = listOf("use %"))

        val notification = notifier.createNotificationWithActions(
            tip,
            TipNotificationActions(onShowNextTip = {}, onExcludeTip = {})
        )

        assertEquals(2, notification.actions.size)
        assertNull(notification.listener)
    }

    @Test
    fun categoriesAreNotRenderedInContent() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Add, change, delete surroundings",
            details = listOf("ys/cs/ds add, change, delete"),
            category = listOf("plugin", "editing"),
            config = TipConfig(name = "vim-surround", lines = listOf("Plug 'tpope/vim-surround'"))
        )

        val notification = notifier.createNotification(tip)

        assertFalse(notification.content.contains("editing"))
    }

    @Test
    fun addedToIdeaVimRcNotificationIsPlainConfirmation() {
        val notifier = TipNotificationFactory()

        val notification = notifier.createAddedToIdeaVimRcNotification(
            TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT
        )

        assertEquals(TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT, notification.content)
        assertTrue(notification.actions.isEmpty())
    }

    @Test
    fun excludedTipNotificationOffersSettingsAction() {
        val notifier = TipNotificationFactory()

        val notification = notifier.createTipExcludedNotification {}

        assertEquals(TipNotificationFactory.TIP_EXCLUDED_WITH_MANAGEMENT_TEXT, notification.content)
        assertEquals(1, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_MANAGE_EXCLUDED_ACTION_TEXT, notification.actions.single().templateText)
    }

    private fun followsDimmedSeparator(title: String, label: String): Boolean {
        return Regex("<span style=\"color:#[0-9a-fA-F]{6};\"> · </span>${Regex.escape(label)}").containsMatchIn(title)
    }

    private fun keySpan(line: Int, text: String, key: String): TipKeySpan {
        val start = text.indexOf(key)
        return TipKeySpan(line, start, start + key.length)
    }

    private fun styledKeys(content: String): List<String> {
        val keyColor = ColorUtil.toHex(TipNotificationFactory.KEY_COLOR)
        return Regex("<span style=\"color:#$keyColor;\">(.*?)</span>")
            .findAll(content).map { it.groupValues[1] }.toList()
    }

    private fun labelColor(title: String, label: String): String? {
        return Regex("<span style=\"color:#([0-9a-fA-F]{6});\">${Regex.escape(label)}</span>")
            .find(title)?.groupValues?.get(1)
    }
}
