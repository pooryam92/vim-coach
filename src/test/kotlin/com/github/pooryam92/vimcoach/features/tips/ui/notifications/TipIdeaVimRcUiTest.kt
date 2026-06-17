package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.TipIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.io.path.Path
import kotlin.io.path.writeText

class TipIdeaVimRcUiTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            val manager = FileEditorManager.getInstance(project)
            manager.openFiles.forEach(manager::closeFile)
        } finally {
            super.tearDown()
        }
    }

    fun testGetActionReturnsNullWhenVimRcNotAvailable() {
        val tip = VimTip("surround", listOf("details"), config = TipConfig(lines = listOf("set surround")))
        val sut = sut(findPath = { null })

        assertNull(sut.getAction(tip))
    }

    fun testGetActionReturnsNullWhenTipHasNoConfig() {
        val tip = VimTip("surround", listOf("details"), config = null)
        val sut = sut(findPath = { tempVimRc("") })

        assertNull(sut.getAction(tip))
    }

    fun testGetActionReturnsNullWhenConfigHasNoLines() {
        val tip = VimTip("surround", listOf("details"), config = TipConfig(name = "Install x", lines = emptyList()))
        val sut = sut(findPath = { tempVimRc("") })

        assertNull(sut.getAction(tip))
    }

    fun testHandleFailedShowsWarningNotification() {
        val shownNotifications = captureProjectNotifications()
        val tip = VimTip("surround", listOf("details"), config = TipConfig(lines = listOf("set surround")))
        // Path exists as far as isAvailable() is concerned, but VFS can't find it → Failed
        val sut = sut(findPath = { Path("/nonexistent/path/.ideavimrc") })

        sut.getAction(tip)?.invoke()

        assertTrue(shownNotifications.any { it.type == NotificationType.WARNING })
    }

    fun testHandleAlreadyPresentOpensFileAtExistingLineAndShowsAlreadyInNotification() {
        val shownNotifications = captureProjectNotifications()
        val configLine = "set surround"
        // The existing block sits on line 2, after two unrelated lines.
        val ideavimrcPath = tempVimRc("set a\nset b\n$configLine\n")
        val tip = VimTip("surround", listOf("details"), config = TipConfig(lines = listOf(configLine)))
        val sut = sut(findPath = { ideavimrcPath })

        sut.getAction(tip)?.invoke()

        assertTrue(shownNotifications.any { it.content == TipNotificationFactory.TIP_ALREADY_IN_IDEAVIMRC_TEXT })
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        assertNotNull(editor)
        assertEquals(2, editor!!.caretModel.logicalPosition.line)
    }

    fun testReloadCallbackInvokedAndReloadNotificationShownOnReloadButtonClick() {
        val shownNotifications = captureProjectNotifications()
        val tip = VimTip("surround", listOf("details"), config = TipConfig(lines = listOf("set surround")))
        var reloadCalled = false
        val sut = sut(findPath = { tempVimRc("") }, reloadIdeaVimRc = { reloadCalled = true })

        sut.getAction(tip)?.invoke()

        val addedNotification = shownNotifications.first {
            it.content == TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT
        }
        val reloadAction = addedNotification.actions.first {
            it.templateText == TipNotificationFactory.TIP_RELOAD_IDEAVIMRC_ACTION_TEXT
        }
        invokeNotificationAction(reloadAction, addedNotification)

        assertTrue(reloadCalled)
        assertTrue(shownNotifications.any { it.content == TipNotificationFactory.TIP_RELOADED_IDEAVIMRC_TEXT })
    }

    private fun sut(
        findPath: () -> java.nio.file.Path?,
        reloadIdeaVimRc: (() -> Unit)? = null
    ) = TipIdeaVimRc(
        project,
        IntelliJTipNotifier(project),
        AddTipToIdeaVimRc(project, findPath = findPath),
        reloadIdeaVimRc
    )

    private fun tempVimRc(content: String): java.nio.file.Path =
        Path(FileUtil.createTempDirectory("vimcoach", "home", true).absolutePath, ".ideavimrc")
            .also { it.writeText(content) }

    private fun captureProjectNotifications(): MutableList<Notification> {
        val captured = mutableListOf<Notification>()
        project.messageBus.connect(testRootDisposable).subscribe(
            Notifications.TOPIC,
            object : Notifications {
                override fun notify(notification: Notification) { captured.add(notification) }
            }
        )
        return captured
    }

    private fun invokeNotificationAction(action: AnAction, notification: Notification) {
        val event = TestActionEvent.createTestEvent(action, DataContext.EMPTY_CONTEXT)
        val actionPerformed = action.javaClass.methods.first { method ->
            method.name == "actionPerformed" &&
                method.parameterTypes.contentEquals(arrayOf(AnActionEvent::class.java, Notification::class.java))
        }
        actionPerformed.invoke(action, event, notification)
    }
}
