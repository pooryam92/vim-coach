package com.github.pooryam92.vimcoach.features.tips.integration.entrypoints.startup

import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderService
import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderServiceImpl
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.entrypoints.startup.VimTipStartupActivity
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipServiceImpl
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipService
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
            project.registerServiceInstance(VimTipService::class.java, VimTipServiceImpl())
            project.registerServiceInstance(TipLoaderService::class.java, TipLoaderServiceImpl(project))
        } finally {
            super.tearDown()
        }
    }

    fun testExecuteShowsTipWhenStartupSettingIsEnabled() {
        settingsService().setShowTipsOnStartupEnabled(true)

        val fakeTipService = registerFakeTipService()
        val fakeLoader = registerFakeLoader()

        runBlocking {
            VimTipStartupActivity().execute(project)
        }

        assertTrue(fakeLoader.awaitCheckForUpdatesCall())
        waitForTipRequests(fakeTipService, expectedCalls = 1)
    }

    fun testExecuteDoesNotShowTipWhenStartupSettingIsDisabled() {
        settingsService().setShowTipsOnStartupEnabled(false)

        val fakeTipService = registerFakeTipService()
        val fakeLoader = registerFakeLoader()

        runBlocking {
            VimTipStartupActivity().execute(project)
        }

        assertTrue(fakeLoader.awaitCheckForUpdatesCall())
        Thread.sleep(NO_TIP_WAIT_MS)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        assertEquals(0, fakeTipService.getRandomTipCalls)
    }

    private fun registerFakeTipService(): FakeVimTipService {
        val fakeTipService = FakeVimTipService(
            initialTips = listOf(VimTip("startup-tip", listOf("startup-details")))
        )
        project.registerServiceInstance(
            VimTipService::class.java,
            fakeTipService
        )
        return fakeTipService
    }

    private fun registerFakeLoader(): FakeTipLoaderService {
        val fakeLoader = FakeTipLoaderService()
        project.registerServiceInstance(
            TipLoaderService::class.java,
            fakeLoader
        )
        return fakeLoader
    }

    private fun waitForTipRequests(fakeTipService: FakeVimTipService, expectedCalls: Int) {
        val deadline = System.currentTimeMillis() + TIP_WAIT_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
            if (fakeTipService.getRandomTipCalls == expectedCalls) {
                return
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }
        assertEquals(expectedCalls, fakeTipService.getRandomTipCalls)
    }

    private fun settingsService(): VimCoachSettingsService = service()

    private class FakeTipLoaderService : TipLoaderService {
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

    private companion object {
        const val CHECK_FOR_UPDATES_TIMEOUT_SECONDS = 5L
        const val TIP_WAIT_TIMEOUT_MS = 5_000L
        const val POLL_INTERVAL_MS = 20L
        const val NO_TIP_WAIT_MS = 200L
    }
}
