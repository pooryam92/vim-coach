package com.github.pooryam92.vimcoach.features.tips.integration.application

import com.github.pooryam92.vimcoach.features.tips.application.PeriodicTipSchedulerServiceImpl
import com.github.pooryam92.vimcoach.features.tips.application.TipNotificationService
import com.github.pooryam92.vimcoach.features.tips.application.TipNotificationServiceImpl
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsServiceImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

class PeriodicTipSchedulerServiceIntTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            ApplicationManager.getApplication().registerServiceInstance(
                VimCoachSettingsService::class.java,
                VimCoachSettingsServiceImpl()
            )
            project.registerServiceInstance(
                TipNotificationService::class.java,
                TipNotificationServiceImpl(project)
            )
        } finally {
            super.tearDown()
        }
    }

    fun testShowPeriodicTipIfEnabledShowsTipWhenSchedulingIsEnabled() {
        val settingsService = FakeSettingsService(
            periodicTipsEnabled = true,
            tipIntervalHours = 1
        )
        val tipNotificationService = FakeTipNotificationService()
        registerDependencies(settingsService, tipNotificationService)

        val scheduler = PeriodicTipSchedulerServiceImpl(project)

        scheduler.showPeriodicTipIfEnabled()
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        assertEquals(1, tipNotificationService.showIfNoneActiveCalls)
    }

    fun testShowPeriodicTipIfEnabledSkipsWhenSchedulingIsDisabled() {
        val settingsService = FakeSettingsService(
            periodicTipsEnabled = false,
            tipIntervalHours = 1
        )
        val tipNotificationService = FakeTipNotificationService()
        registerDependencies(settingsService, tipNotificationService)

        val scheduler = PeriodicTipSchedulerServiceImpl(project)

        scheduler.showPeriodicTipIfEnabled()
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        assertEquals(0, tipNotificationService.showIfNoneActiveCalls)
    }

    private fun registerDependencies(
        settingsService: VimCoachSettingsService,
        tipNotificationService: TipNotificationService
    ) {
        ApplicationManager.getApplication().registerServiceInstance(
            VimCoachSettingsService::class.java,
            settingsService
        )
        project.registerServiceInstance(
            TipNotificationService::class.java,
            tipNotificationService
        )
    }

    private class FakeTipNotificationService : TipNotificationService {
        var showIfNoneActiveCalls = 0
            private set

        override fun showRandomTip() {
            error("Not expected in periodic scheduler tests")
        }

        override fun showRandomTipIfNoneActive(): Boolean {
            showIfNoneActiveCalls += 1
            return true
        }
    }

    private open class FakeSettingsService(
        private val periodicTipsEnabled: Boolean,
        private val tipIntervalHours: Int
    ) : VimCoachSettingsService {
        override fun isShowTipsOnStartupEnabled(): Boolean = true

        override fun setShowTipsOnStartupEnabled(enabled: Boolean) = Unit

        override fun isPeriodicTipsEnabled(): Boolean = periodicTipsEnabled

        override fun setPeriodicTipsEnabled(enabled: Boolean) = Unit

        override fun getTipIntervalHours(): Int = tipIntervalHours

        override fun setTipIntervalHours(hours: Int) = Unit
    }
}
