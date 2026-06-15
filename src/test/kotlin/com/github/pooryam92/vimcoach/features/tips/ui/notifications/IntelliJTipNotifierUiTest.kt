package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipActions
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.lang.reflect.Proxy

/**
 * Tests the IntelliJ adapter [IntelliJTipNotifier]: real `Notification` rendering, the active-tip
 * lifecycle (replace/expire/visibility), and the message affordances. Application logic lives in
 * TipNotificationsUnitTest.
 */
class IntelliJTipNotifierUiTest : BasePlatformTestCase() {

    private val noActions = TipActions(onShowNextTip = {}, onExcludeTip = {}, onAddToIdeaVimRc = null)

    private fun notifier() = IntelliJTipNotifier(project)

    fun testShowTipAddsNextAndExcludeActionsButNoAddWhenUnavailable() {
        val notifier = notifier()
        notifier.showTip(tip(), noActions)

        val notification = notifier.activeTipNotification!!
        assertEquals(2, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
        assertEquals(TipNotificationFactory.TIP_DONT_SHOW_AGAIN_ACTION_TEXT, notification.actions[1].templateText)
    }

    fun testShowTipAddsAddToIdeaVimRcActionWhenProvided() {
        val notifier = notifier()
        notifier.showTip(tip(), TipActions(onShowNextTip = {}, onExcludeTip = {}, onAddToIdeaVimRc = {}))

        val notification = notifier.activeTipNotification!!
        assertEquals(3, notification.actions.size)
        assertTrue(notification.actions.any { it.templateText == TipNotificationFactory.TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT })
    }

    fun testShowTipReplacesAndExpiresPreviousTip() {
        val notifier = notifier()
        notifier.showTip(tip("Tip 1"), noActions)
        val first = notifier.activeTipNotification!!
        notifier.showTip(tip("Tip 2"), noActions)
        val second = notifier.activeTipNotification!!

        assertNotSame(first, second)
        assertTrue(first.isExpired)
        assertFalse(second.isExpired)
    }

    fun testExcludeActionRunsCallbackAndDismissesTip() {
        val notifier = notifier()
        var excluded = false
        notifier.showTip(tip(), TipActions(onShowNextTip = {}, onExcludeTip = { excluded = true }, onAddToIdeaVimRc = null))
        val notification = notifier.activeTipNotification!!

        invokeNotificationAction(notification.actions[1], notification)

        assertTrue(excluded)
        assertTrue(notification.isExpired)
        assertNull(notifier.activeTipNotification)
    }

    fun testStaleExcludeActionDoesNotClearNewerActiveTip() {
        val notifier = notifier()
        notifier.showTip(tip("Tip 1"), noActions)
        val first = notifier.activeTipNotification!!
        notifier.showTip(tip("Tip 2"), noActions)
        val second = notifier.activeTipNotification!!

        invokeNotificationAction(first.actions[1], first)

        assertSame(second, notifier.activeTipNotification)
        assertFalse(second.isExpired)
    }

    fun testHasVisibleTipIsFalseWithoutBalloon() {
        val notifier = notifier()
        notifier.showTip(tip(), noActions)

        assertFalse(notifier.hasVisibleTip())
    }

    fun testHasVisibleTipIsTrueWithVisibleBalloon() {
        val notifier = notifier()
        notifier.showTip(tip(), noActions)
        notifier.activeTipNotification!!.setBalloon(visibleBalloon())

        assertTrue(notifier.hasVisibleTip())
    }

    fun testShowAddedToIdeaVimRcShowsReloadActionAndHandleDismisses() {
        val captured = captureProjectNotifications()
        val handle = notifier().showAddedToIdeaVimRc(onReload = {})

        val added = captured.first { it.content == TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT }
        assertTrue(added.actions.any { it.templateText == TipNotificationFactory.TIP_RELOAD_IDEAVIMRC_ACTION_TEXT })

        assertFalse(added.isExpired)
        handle.dismiss()
        assertTrue(added.isExpired)
    }

    fun testShowAddedToIdeaVimRcOmitsReloadActionWhenNull() {
        val captured = captureProjectNotifications()
        notifier().showAddedToIdeaVimRc(onReload = null)

        val added = captured.first { it.content == TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT }
        assertTrue(added.actions.none { it.templateText == TipNotificationFactory.TIP_RELOAD_IDEAVIMRC_ACTION_TEXT })
    }

    fun testShowAddToIdeaVimRcFailedEmitsWarning() {
        val captured = captureProjectNotifications()
        notifier().showAddToIdeaVimRcFailed()

        assertTrue(captured.any { it.type == NotificationType.WARNING })
    }

    fun testShowTipExcludedOffersManageAction() {
        val captured = captureProjectNotifications()
        notifier().showTipExcluded(onManage = {})

        val excluded = captured.first { it.content == TipNotificationFactory.TIP_EXCLUDED_WITH_MANAGEMENT_TEXT }
        assertTrue(excluded.actions.any { it.templateText == TipNotificationFactory.TIP_MANAGE_EXCLUDED_ACTION_TEXT })
    }

    private fun tip(summary: String = "tip") = VimTip(summary, listOf("details"))

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

    private fun visibleBalloon(): Balloon =
        Proxy.newProxyInstance(
            Balloon::class.java.classLoader,
            arrayOf(Balloon::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "isDisposed" -> false
                "addListener" -> null
                else -> defaultValue(method.returnType)
            }
        } as Balloon

    private fun defaultValue(returnType: Class<*>): Any? = when (returnType) {
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
