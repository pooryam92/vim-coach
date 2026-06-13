package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotificationController
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.ideavimrc.infra.IdeaVimRcFile
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipService
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.lang.reflect.Proxy
import com.intellij.notification.Notifications
import com.intellij.openapi.vfs.LocalFileSystem
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

class TipNotificationControllerUiTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            closeOpenedEditors()
            resetSettingsState()
        } finally {
            super.tearDown()
        }
    }

    private fun closeOpenedEditors() {
        val manager = FileEditorManager.getInstance(project)
        manager.openFiles.forEach(manager::closeFile)
    }

    fun testNotificationAddsNextTipAction() {
        val notifier = TipNotificationFactory()
        val notification = notifier.createNotificationWithActions(
            VimTip(summary = "Tip 1", details = listOf("Details 1"))
        ) {}

        assertEquals(1, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
    }

    fun testNotificationAddsNextAndHideActionsForRealTip() {
        val tip = VimTip(summary = "Tip 1", details = listOf("Details 1"))
        val settingsService = FakeSettingsService(emptyList())
        val controller = TipNotificationController(
            project,
            FakeVimTipService(initialTips = listOf(tip)),
            TipNotificationFactory(),
            settingsService
        )

        controller.showRandomTip()

        val notification = controller.activeNotification!!
        assertEquals(2, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
        assertEquals(TipNotificationFactory.TIP_DONT_SHOW_AGAIN_ACTION_TEXT, notification.actions[1].templateText)
    }

    fun testAddToIdeaVimRcActionAppendsConfigAndIsShownLast() {
        val tempHome = FileUtil.createTempDirectory("vimcoach", "home", true).absolutePath
        val tip = VimTip(
            summary = "surround",
            details = listOf("edit surroundings"),
            config = listOf("Plug 'tpope/vim-surround'")
        )
        val controller = TipNotificationController(
            project,
            FakeVimTipService(initialTips = listOf(tip)),
            TipNotificationFactory(),
            FakeSettingsService(emptyList()),
            AddTipToIdeaVimRc(IdeaVimRcFile(userHome = tempHome, xdgConfigHome = null))
        )

        controller.showRandomTip()
        val notification = controller.activeNotification!!
        assertEquals(3, notification.actions.size)
        // "Next tip" stays first; the add action is last to avoid accidental clicks.
        assertEquals(TipNotificationFactory.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
        assertEquals(
            TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT,
            notification.actions[2].templateText
        )

        invokeNotificationAction(notification.actions[2], notification)

        val written = Path(tempHome, ".ideavimrc").readText()
        assertTrue(written.contains("Plug 'tpope/vim-surround'"))
        // The file opens with the caret on the just-appended line so the change is visible.
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        assertEquals(0, editor.caretModel.logicalPosition.line)
    }

    fun testAddToIdeaVimRcUpdatesEditorDocumentWhenFileIsAlreadyOpen() {
        // Regression: NIO writes bypass IntelliJ's VFS, so the open Document was not updated
        // synchronously — the user saw no change in the editor even though the disk was written.
        val tempHome = FileUtil.createTempDirectory("vimcoach", "home", true).absolutePath
        val ideavimrcPath = Path(tempHome, ".ideavimrc")
        ideavimrcPath.writeText("\" existing config\n")

        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(ideavimrcPath)!!
        FileEditorManager.getInstance(project).openFile(virtualFile, true)

        val tip = VimTip(
            summary = "surround",
            details = listOf("edit surroundings"),
            config = listOf("Plug 'tpope/vim-surround'")
        )
        val controller = TipNotificationController(
            project,
            FakeVimTipService(initialTips = listOf(tip)),
            TipNotificationFactory(),
            FakeSettingsService(emptyList()),
            AddTipToIdeaVimRc(IdeaVimRcFile(userHome = tempHome, xdgConfigHome = null))
        )

        controller.showRandomTip()
        val notification = controller.activeNotification!!
        val addAction = notification.actions.first {
            it.templateText == TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT
        }
        invokeNotificationAction(addAction, notification)

        assertTrue(ideavimrcPath.readText().contains("Plug 'tpope/vim-surround'"))

        // The in-memory Document must reflect the change immediately — no waiting for invokeLater.
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        assertTrue(
            "Editor document should contain the appended config line when the file was open at time of add",
            editor.document.text.contains("Plug 'tpope/vim-surround'")
        )
    }

    fun testAddToIdeaVimRcShowsReloadButtonWhenIdeaVimIsAvailable() {
        val shownNotifications = captureProjectNotifications()

        val tempHome = FileUtil.createTempDirectory("vimcoach", "home", true).absolutePath
        val tip = VimTip(
            summary = "surround",
            details = listOf("edit surroundings"),
            config = listOf("Plug 'tpope/vim-surround'")
        )
        val controller = TipNotificationController(
            project,
            FakeVimTipService(initialTips = listOf(tip)),
            TipNotificationFactory(),
            FakeSettingsService(emptyList()),
            AddTipToIdeaVimRc(IdeaVimRcFile(userHome = tempHome, xdgConfigHome = null)),
            reloadIdeaVimRc = {}  // injected lambda signals IdeaVim reload is available
        )

        controller.showRandomTip()
        invokeNotificationAction(
            controller.activeNotification!!.actions.first {
                it.templateText == TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT
            },
            controller.activeNotification!!
        )

        val addedNotification = shownNotifications.first {
            it.content == TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT
        }
        assertTrue(
            "Reload now button should be present when IdeaVim reload is available",
            addedNotification.actions.any { it.templateText == TipNotificationFactory.TIP_RELOAD_IDEAVIMRC_ACTION_TEXT }
        )
    }

    fun testAddToIdeaVimRcOmitsReloadButtonWhenIdeaVimIsNotAvailable() {
        val shownNotifications = captureProjectNotifications()

        val tempHome = FileUtil.createTempDirectory("vimcoach", "home", true).absolutePath
        val tip = VimTip(
            summary = "surround",
            details = listOf("edit surroundings"),
            config = listOf("Plug 'tpope/vim-surround'")
        )
        // No reloadIdeaVimRc lambda injected; IdeaVim action also absent in the test environment.
        val controller = TipNotificationController(
            project,
            FakeVimTipService(initialTips = listOf(tip)),
            TipNotificationFactory(),
            FakeSettingsService(emptyList()),
            AddTipToIdeaVimRc(IdeaVimRcFile(userHome = tempHome, xdgConfigHome = null))
        )

        controller.showRandomTip()
        invokeNotificationAction(
            controller.activeNotification!!.actions.first {
                it.templateText == TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT
            },
            controller.activeNotification!!
        )

        val addedNotification = shownNotifications.first {
            it.content == TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT
        }
        assertTrue(
            "Reload now button should not appear when IdeaVim is not available",
            addedNotification.actions.none { it.templateText == TipNotificationFactory.TIP_RELOAD_IDEAVIMRC_ACTION_TEXT }
        )
    }

    fun testTipWithoutConfigHasNoAddToIdeaVimRcAction() {
        val tip = VimTip(summary = "Tip 1", details = listOf("Details 1"))
        val controller = TipNotificationController(
            project,
            FakeVimTipService(initialTips = listOf(tip)),
            TipNotificationFactory(),
            FakeSettingsService(emptyList())
        )

        controller.showRandomTip()

        val notification = controller.activeNotification!!
        assertEquals(2, notification.actions.size)
        assertTrue(
            notification.actions.none {
                it.templateText == TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT
            }
        )
    }

    fun testShowRandomTipRequestsTipFromService() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip", listOf("Details"), listOf("basics")))
        )
        val controller = TipNotificationController(
            project,
            mockTipService,
            TipNotificationFactory(),
            FakeSettingsService(listOf("basics"))
        )

        controller.showRandomTip()

        assertEquals(1, mockTipService.getRandomTipByCategoryCalls)
        assertEquals(listOf("basics"), mockTipService.lastRequestedCategories)
    }

    fun testShowRandomTipNotificationUsesRealSelectionAndExcludesHiddenTips() {
        resetSettingsState()
        val hiddenTip = VimTip("Hidden editing tip", listOf("Hidden details"), listOf("editing"))
        val visibleTip = VimTip("Visible editing tip", listOf("Visible details"), listOf("editing"))
        val settingsService = service<VimCoachSettingsService>()
        val tipService = service<VimTipService>().apply {
            saveTips(listOf(hiddenTip, visibleTip))
        }
        settingsService.hideTip(TipHash.fromTip(hiddenTip).value)
        val controller = TipNotificationController(
            project,
            tipService,
            TipNotificationFactory(),
            settingsService
        )

        controller.showRandomTip()

        val content = controller.activeNotification!!.content
        assertTrue(content.contains("Visible editing tip"))
        assertTrue(content.contains("Visible details"))
        assertFalse(content.contains("Hidden editing tip"))
        assertFalse(content.contains("Hidden details"))
    }

    fun testExcludeTipActionHidesCurrentTipWithoutShowingNextRandomTip() {
        val tip = VimTip(summary = "Tip 1", details = listOf("Details 1"))
        val settingsService = FakeSettingsService(emptyList())
        val mockTipService = FakeVimTipService(
            initialTips = listOf(tip, VimTip(summary = "Tip 2", details = listOf("Details 2")))
        )
        val controller = TipNotificationController(
            project,
            mockTipService,
            TipNotificationFactory(),
            settingsService
        )

        controller.showRandomTip()
        val notification = controller.activeNotification!!
        invokeNotificationAction(notification.actions[1], notification)

        assertEquals(listOf(TipHash.fromTip(tip).value), settingsService.getHiddenTipHashes())
        assertEquals(1, mockTipService.getRandomTipCalls)
        assertTrue(notification.isExpired)
        assertNull(controller.activeNotification)
    }

    fun testShowRandomTipIfNoneActiveShowsWhenNoActiveNotificationExists() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip 1", listOf("Details 1")))
        )
        val controller = TipNotificationController(project, mockTipService, TipNotificationFactory())

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
        val controller = TipNotificationController(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()
        val firstNotification = controller.activeNotification
        controller.showRandomTip()
        val secondNotification = controller.activeNotification

        assertNotNull(firstNotification)
        assertNotNull(secondNotification)
        assertTrue(firstNotification!!.isExpired)
        assertFalse(secondNotification!!.isExpired)
    }

    fun testStaleExcludeActionDoesNotExpireNewerActiveTipNotification() {
        val firstTip = VimTip("Tip 1", listOf("Details 1"))
        val secondTip = VimTip("Tip 2", listOf("Details 2"))
        val settingsService = FakeSettingsService(emptyList())
        val controller = TipNotificationController(
            project,
            FakeVimTipService(initialTips = listOf(firstTip, secondTip)),
            TipNotificationFactory(),
            settingsService
        )

        controller.showRandomTip()
        val firstNotification = controller.activeNotification!!
        controller.showRandomTip()
        val secondNotification = controller.activeNotification!!
        invokeNotificationAction(firstNotification.actions[1], firstNotification)

        assertEquals(listOf(TipHash.fromTip(firstTip).value), settingsService.getHiddenTipHashes())
        assertSame(secondNotification, controller.activeNotification)
        assertFalse(secondNotification.isExpired)
    }

    fun testShowRandomTipIfNoneActiveDoesNotReplaceVisibleNotification() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
                VimTip("Tip 1", listOf("Details 1")),
                VimTip("Tip 2", listOf("Details 2"))
            )
        )
        val controller = TipNotificationController(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()
        val firstNotification = controller.activeNotification
        firstNotification!!.setBalloon(createVisibleBalloon())

        val shown = controller.showRandomTipIfNoneActive()
        val secondNotification = controller.activeNotification

        assertFalse(shown)
        assertEquals(1, mockTipService.getRandomTipCalls)
        assertSame(firstNotification, secondNotification)
        assertFalse(firstNotification.isExpired)
    }

    fun testShowRandomTipIfNoneActiveShowsAfterTrackedNotificationExpires() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
                VimTip("Tip 1", listOf("Details 1")),
                VimTip("Tip 2", listOf("Details 2"))
            )
        )
        val controller = TipNotificationController(project, mockTipService, TipNotificationFactory())

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
        val controller = TipNotificationController(project, mockTipService, TipNotificationFactory())

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

    private fun captureProjectNotifications(): MutableList<Notification> {
        val captured = mutableListOf<Notification>()
        project.messageBus.connect(testRootDisposable).subscribe(
            Notifications.TOPIC,
            object : Notifications {
                override fun notify(notification: Notification) {
                    captured.add(notification)
                }
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

    private fun resetSettingsState() {
        service<VimCoachSettingsStore>().loadState(VimCoachSettingsStore.State())
    }

    private fun createVisibleBalloon(): Balloon {
        return Proxy.newProxyInstance(
            Balloon::class.java.classLoader,
            arrayOf(Balloon::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "isDisposed" -> false
                "addListener" -> null
                else -> defaultValue(method.returnType)
            }
        } as Balloon
    }

    private fun defaultValue(returnType: Class<*>): Any? {
        return when (returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Short.TYPE -> 0.toShort()
            java.lang.Byte.TYPE -> 0.toByte()
            java.lang.Character.TYPE -> 0.toChar()
            else -> null
        }
    }

    private class FakeSettingsService(
        private val enabledCategories: List<String>
    ) : VimCoachSettingsService {
        private val hiddenTipHashes = mutableListOf<String>()

        override fun isShowTipsOnStartupEnabled(): Boolean = true

        override fun setShowTipsOnStartupEnabled(enabled: Boolean) = Unit

        override fun isPeriodicTipsEnabled(): Boolean = false

        override fun setPeriodicTipsEnabled(enabled: Boolean) = Unit

        override fun getTipIntervalHours(): Int = 1

        override fun setTipIntervalHours(hours: Int) = Unit

        override fun getEnabledTipCategories(availableCategories: List<String>): List<String> {
            return enabledCategories
        }

        override fun setEnabledTipCategories(
            availableCategories: List<String>,
            enabledCategories: List<String>
        ) = Unit

        override fun getHiddenTipHashes(): List<String> = hiddenTipHashes.toList()

        override fun hideTip(hash: String) {
            if (hash !in hiddenTipHashes) {
                hiddenTipHashes.add(hash)
            }
        }

        override fun restoreTip(hash: String) {
            hiddenTipHashes.remove(hash)
        }

        override fun consumeExcludedTipsManagementHint(): Boolean = true
    }
}
