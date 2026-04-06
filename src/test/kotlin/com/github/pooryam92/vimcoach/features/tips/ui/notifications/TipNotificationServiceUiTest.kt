package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.application.TipNotificationServiceImpl
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipService
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.lang.reflect.Proxy

class TipNotificationServiceUiTest : BasePlatformTestCase() {

    fun testNotificationAddsNextTipAction() {
        val notifier = TipNotificationFactory()
        val notification = notifier.createNotificationWithActions(
            VimTip(summary = "Tip 1", details = listOf("Details 1"))
        ) {}

        assertEquals(1, notification.actions.size)
        assertEquals(TipNotificationFactory.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
    }

    fun testShowRandomTipRequestsTipFromService() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip", listOf("Details"), listOf("basics")))
        )
        val controller = TipNotificationServiceImpl(
            project,
            mockTipService,
            TipNotificationFactory(),
            FakeSettingsService(listOf("basics"))
        )

        controller.showRandomTip()

        assertEquals(1, mockTipService.getRandomTipByCategoryCalls)
        assertEquals(listOf("basics"), mockTipService.lastRequestedCategories)
    }

    fun testShowRandomTipIfNoneActiveShowsWhenNoActiveNotificationExists() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(VimTip("Tip 1", listOf("Details 1")))
        )
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

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
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

        controller.showRandomTip()
        val firstNotification = controller.activeNotification
        controller.showRandomTip()
        val secondNotification = controller.activeNotification

        assertNotNull(firstNotification)
        assertNotNull(secondNotification)
        assertTrue(firstNotification!!.isExpired)
        assertFalse(secondNotification!!.isExpired)
    }

    fun testShowRandomTipIfNoneActiveDoesNotReplaceVisibleNotification() {
        val mockTipService = FakeVimTipService(
            initialTips = listOf(
                VimTip("Tip 1", listOf("Details 1")),
                VimTip("Tip 2", listOf("Details 2"))
            )
        )
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

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
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

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
        val controller = TipNotificationServiceImpl(project, mockTipService, TipNotificationFactory())

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
        override fun isShowTipsOnStartupEnabled(): Boolean = true

        override fun setShowTipsOnStartupEnabled(enabled: Boolean) = Unit

        override fun isPeriodicTipsEnabled(): Boolean = false

        override fun setPeriodicTipsEnabled(enabled: Boolean) = Unit

        override fun getTipIntervalHours(): Int = 1

        override fun setTipIntervalHours(hours: Int) = Unit

        override fun getEnabledTipCategories(availableCategories: List<String>): List<String> {
            return enabledCategories
        }

        override fun setEnabledTipCategories(categories: List<String>) = Unit
    }
}
