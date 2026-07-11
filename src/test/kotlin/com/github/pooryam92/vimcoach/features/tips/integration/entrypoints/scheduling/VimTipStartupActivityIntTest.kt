package com.github.pooryam92.vimcoach.features.tips.integration.entrypoints.scheduling

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.application.loading.TipRefresh
import com.github.pooryam92.vimcoach.features.tips.application.scheduling.ScheduleTips
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.entrypoints.scheduling.VimTipStartupActivity
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipRepository
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class VimTipStartupActivityIntTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        settingsService().setShowTipsOnStartupEnabled(true)
    }

    override fun tearDown() {
        try {
            settingsService().setShowTipsOnStartupEnabled(true)
            ApplicationManager.getApplication().registerServiceInstance(VimTipRepository::class.java, VimTipRepositoryImpl())
            ApplicationManager.getApplication().registerServiceInstance(RefreshTips::class.java, TipRefresh())
        } finally {
            super.tearDown()
        }
    }

    fun testExecuteShowsTipWhenStartupSettingIsEnabled() {
        settingsService().setShowTipsOnStartupEnabled(true)
        settingsService().setPeriodicTipsEnabled(true)

        val fakeTipService = registerFakeTipService()
        val fakeLoader = registerFakeLoader()
        val fakePeriodicScheduler = registerFakePeriodicScheduler()

        runBlocking {
            VimTipStartupActivity().execute(project)
        }

        assertTrue(fakeLoader.awaitCheckForUpdatesCall())
        assertTrue(fakePeriodicScheduler.awaitStartCall())
        waitForTipRequests(fakeTipService, expectedCalls = 1)
    }

    fun testExecuteDoesNotShowTipWhenStartupSettingIsDisabled() {
        settingsService().setShowTipsOnStartupEnabled(false)
        settingsService().setPeriodicTipsEnabled(true)

        val fakeTipService = registerFakeTipService()
        val fakeLoader = registerFakeLoader()
        val fakePeriodicScheduler = registerFakePeriodicScheduler()

        runBlocking {
            VimTipStartupActivity().execute(project)
        }

        assertTrue(fakeLoader.awaitCheckForUpdatesCall())
        assertTrue(fakePeriodicScheduler.awaitStartCall())
        Thread.sleep(NO_TIP_WAIT_MS)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        assertEquals(0, fakeTipService.getTipsCalls)
    }

    fun testExecuteDoesNotStartPeriodicSchedulerWhenPeriodicTipsAreDisabled() {
        settingsService().setPeriodicTipsEnabled(false)

        val fakeLoader = registerFakeLoader()
        val fakePeriodicScheduler = registerFakePeriodicScheduler()

        runBlocking {
            VimTipStartupActivity().execute(project)
        }

        assertTrue(fakeLoader.awaitCheckForUpdatesCall())
        assertFalse(fakePeriodicScheduler.awaitStartCall(timeoutMs = NO_TIP_WAIT_MS))
    }

    private fun registerFakeTipService(): FakeVimTipRepository {
        val fakeTipService = FakeVimTipRepository(
            initialTips = listOf(VimTip("startup-tip", listOf("startup-details")))
        )
        ApplicationManager.getApplication().registerServiceInstance(
            VimTipRepository::class.java,
            fakeTipService
        )
        return fakeTipService
    }

    private fun registerFakeLoader(): FakeRefreshTips {
        val fakeLoader = FakeRefreshTips()
        ApplicationManager.getApplication().registerServiceInstance(
            RefreshTips::class.java,
            fakeLoader
        )
        return fakeLoader
    }

    private fun registerFakePeriodicScheduler(): FakeScheduleTips {
        val fakePeriodicScheduler = FakeScheduleTips()
        project.registerServiceInstance(
            ScheduleTips::class.java,
            fakePeriodicScheduler
        )
        return fakePeriodicScheduler
    }

    private fun waitForTipRequests(fakeTipService: FakeVimTipRepository, expectedCalls: Int) {
        val deadline = System.currentTimeMillis() + TIP_WAIT_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
            if (fakeTipService.getTipsCalls == expectedCalls) {
                return
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }
        assertEquals(expectedCalls, fakeTipService.getTipsCalls)
    }

    private fun settingsService(): SettingsRepository = service()

    private class FakeRefreshTips : RefreshTips {
        private val checkForUpdatesCalled = CountDownLatch(1)

        override fun refetchTips(): TipLoadResult {
            return TipLoadResult.NotModified
        }

        override fun checkForUpdates(): TipLoadResult {
            checkForUpdatesCalled.countDown()
            return TipLoadResult.NotModified
        }

        fun awaitCheckForUpdatesCall(): Boolean {
            return checkForUpdatesCalled.await(CHECK_FOR_UPDATES_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    private class FakeScheduleTips : ScheduleTips {
        private val startCalled = CountDownLatch(1)

        override fun start() {
            startCalled.countDown()
        }

        override fun onSettingsChanged() {
            // No-op for startup flow tests.
        }

        fun awaitStartCall(timeoutMs: Long = CHECK_FOR_UPDATES_TIMEOUT_SECONDS * 1_000): Boolean {
            return startCalled.await(timeoutMs, TimeUnit.MILLISECONDS)
        }
    }

    private companion object {
        const val CHECK_FOR_UPDATES_TIMEOUT_SECONDS = 5L
        const val TIP_WAIT_TIMEOUT_MS = 5_000L
        const val POLL_INTERVAL_MS = 20L
        const val NO_TIP_WAIT_MS = 200L
    }
}
