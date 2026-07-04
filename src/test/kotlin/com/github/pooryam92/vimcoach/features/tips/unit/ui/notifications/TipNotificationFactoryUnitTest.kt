package com.github.pooryam92.vimcoach.features.tips.unit.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationActions
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
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
    fun createNotificationDimsOnlyTheMnemonic() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Change inner word ciw",
            details = listOf("ciw replaces the word under the cursor"),
            mnemonic = "change inner word"
        )

        val notification = notifier.createNotification(tip)

        val dimmedBlocks = Regex("color:#[0-9a-fA-F]{6}").findAll(notification.content).count()
        assertEquals(1, dimmedBlocks)
        assertTrue(notification.content.contains("<div style=\"margin-top:8px;margin-bottom:8px;\">"))
    }

    @Test
    fun createNotificationRendersMnemonicInItalicWhenPresent() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Change inner word ciw",
            details = listOf("ciw replaces the word under the cursor"),
            mnemonic = "change inner word"
        )

        val notification = notifier.createNotification(tip)

        assertTrue(notification.content.contains("font-style:italic"))
        assertTrue(notification.content.contains(TipNotificationFactory.TIP_MNEMONIC_LABEL))
        assertTrue(notification.content.contains("change inner word"))
    }

    @Test
    fun createNotificationEscapesHtmlInMnemonic() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(
            summary = "Delete to end D",
            details = listOf("D deletes to end of line"),
            mnemonic = "<Delete> & \"go\""
        )

        val notification = notifier.createNotification(tip)

        assertTrue(notification.content.contains("&lt;Delete&gt;"))
        assertTrue(notification.content.contains("&amp;"))
        assertTrue(notification.content.contains("&quot;"))
    }

    @Test
    fun createNotificationOmitsMnemonicBlockWhenAbsent() {
        val notifier = TipNotificationFactory()
        val tip = VimTip(summary = "jump", details = listOf("use %"))

        val notification = notifier.createNotification(tip)

        assertFalse(notification.content.contains("font-style:italic"))
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
}
