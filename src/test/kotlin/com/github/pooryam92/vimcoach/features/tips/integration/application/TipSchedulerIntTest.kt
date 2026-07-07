package com.github.pooryam92.vimcoach.features.tips.integration.application

import com.github.pooryam92.vimcoach.features.tips.application.notifications.ShowTips
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifications
import com.github.pooryam92.vimcoach.features.tips.application.scheduling.TipScheduler
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

class TipSchedulerIntTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            ApplicationManager.getApplication().registerServiceInstance(
                SettingsRepository::class.java,
                SettingsRepositoryImpl()
            )
            project.registerServiceInstance(
                ShowTips::class.java,
                TipNotifications(project)
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
        val tipNotificationService = FakeShowTips()
        registerDependencies(settingsService, tipNotificationService)

        val scheduler = TipScheduler(project)

        scheduler.showPeriodicTipIfEnabled()
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        assertEquals(1, tipNotificationService.showIfNoneActiveCalls)
    }

    fun testShowPeriodicTipIfEnabledSkipsWhenSchedulingIsDisabled() {
        val settingsService = FakeSettingsService(
            periodicTipsEnabled = false,
            tipIntervalHours = 1
        )
        val tipNotificationService = FakeShowTips()
        registerDependencies(settingsService, tipNotificationService)

        val scheduler = TipScheduler(project)

        scheduler.showPeriodicTipIfEnabled()
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        assertEquals(0, tipNotificationService.showIfNoneActiveCalls)
    }

    private fun registerDependencies(
        settingsService: SettingsRepository,
        tipNotificationService: ShowTips
    ) {
        ApplicationManager.getApplication().registerServiceInstance(
            SettingsRepository::class.java,
            settingsService
        )
        project.registerServiceInstance(
            ShowTips::class.java,
            tipNotificationService
        )
    }

    private class FakeShowTips : ShowTips {
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
    ) : SettingsRepository {
        override fun isShowTipsOnStartupEnabled(): Boolean = true

        override fun setShowTipsOnStartupEnabled(enabled: Boolean) = Unit

        override fun isPeriodicTipsEnabled(): Boolean = periodicTipsEnabled

        override fun setPeriodicTipsEnabled(enabled: Boolean) = Unit

        override fun getTipIntervalHours(): Int = tipIntervalHours

        override fun setTipIntervalHours(hours: Int) = Unit

        override fun getEnabledTipCategories(availableCategories: List<String>): List<String> = availableCategories

        override fun setEnabledTipCategories(
            availableCategories: List<String>,
            enabledCategories: List<String>
        ) = Unit

        override fun getHiddenTipHashes(): List<String> = emptyList()

        override fun hideTip(hash: String) = Unit

        override fun restoreTip(hash: String) = Unit

        override fun consumeExcludedTipsManagementHint(): Boolean = false

        override fun isShowAdvancedTipsEnabled(): Boolean = false

        override fun setShowAdvancedTipsEnabled(enabled: Boolean) = Unit

        override fun isAdvancedTipsHintShown(): Boolean = true

        override fun consumeAdvancedTipsHint(): Boolean = false

        override fun getTipsShownForAdvancedNudge(): Int = 0

        override fun setTipsShownForAdvancedNudge(count: Int) = Unit
    }
}
