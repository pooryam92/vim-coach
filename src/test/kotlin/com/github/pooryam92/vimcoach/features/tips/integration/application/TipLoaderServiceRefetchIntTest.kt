package com.github.pooryam92.vimcoach.features.tips.integration.application

import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderService
import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderServiceImpl
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

class TipLoaderServiceRefetchIntTest : BasePlatformTestCase() {

    fun testRefetchTipsReloadsEvenWhenTipsExist() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("old-summary", listOf("old-details"))))

        val remoteTips = listOf(
            VimTip("new-summary-1", listOf("new-details-1")),
            VimTip("new-summary-2", listOf("new-details-2"))
        )
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(remoteTips, TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(2, tipService.countTips())
        assertTrue(remoteTips.contains(tipService.getRandomTip()))
    }

    fun testRefetchTipsUpdatesExistingTips() {
        val tipService = service<VimTipService>()
        tipService.saveTips(
            listOf(
                VimTip("initial-1", listOf("initial-details-1")),
                VimTip("initial-2", listOf("initial-details-2")),
                VimTip("initial-3", listOf("initial-details-3"))
            )
        )
        assertEquals(3, tipService.countTips())

        val updatedTips = listOf(VimTip("updated-1", listOf("updated-details-1")))
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
        assertEquals("updated-1", tipService.getRandomTip().summary)
    }

    fun testRefetchTipsDoesNotSaveWhenRemoteReturnsFailure() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("existing", listOf("existing-details"))))

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.Failure("connection timeout"))
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Failed("connection timeout"), result)
        assertEquals(1, tipService.countTips())
        assertEquals("existing", tipService.getRandomTip().summary)
    }

    fun testRefetchTipsDoesNotSaveWhenRemoteReturnsEmpty() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("existing", listOf("existing-details"))))

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.Empty)
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.NoData, result)
        assertEquals(1, tipService.countTips())
        assertEquals("existing", tipService.getRandomTip().summary)
    }

    fun testRefetchTipsReturnsNotModifiedWhenNoChanges() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("existing", listOf("existing-details"))))

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(1, tipService.countTips())
        assertEquals("existing", tipService.getRandomTip().summary)
    }

    fun testRefetchTipsAlwaysForcesReload() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("existing", listOf("existing-details"))))
        tipService.saveMetadata(
            TipMetadata(
                etag = "abc123",
                githubSha = "def456",
                lastFetchTimestamp = System.currentTimeMillis()
            )
        )

        val updatedTips = listOf(VimTip("new", listOf("new-details")))
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
        assertEquals("new", tipService.getRandomTip().summary)
    }

    fun testCheckForUpdatesLoadsWhenNoTipsExist() {
        val tipService = service<VimTipService>()
        tipService.saveTips(emptyList())

        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("new-tip", listOf("new-details"))), TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
    }

    fun testCheckForUpdatesUsesConditionalWhenTipsExist() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("existing", listOf("existing-details"))))
        tipService.saveMetadata(
            TipMetadata(
                etag = "abc123",
                githubSha = "def456",
                lastFetchTimestamp = System.currentTimeMillis() - 3_600_000
            )
        )

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(0, fakeTipSource.loadTipsCalls)
        assertEquals(1, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(1, tipService.countTips())
        assertEquals("existing", tipService.getRandomTip().summary)
    }

    fun testCheckForUpdatesNotModifiedRefreshesLastFetchTimestamp() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("existing", listOf("existing-details"))))
        val initialTimestamp = 1_000L
        tipService.saveMetadata(
            TipMetadata(
                etag = "etag",
                githubSha = "sha",
                lastFetchTimestamp = initialTimestamp
            )
        )

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(0, fakeTipSource.loadTipsCalls)
        assertEquals(1, fakeTipSource.loadTipsConditionalCalls)
        assertTrue(tipService.getMetadata().lastFetchTimestamp > initialTimestamp)
    }

    fun testCheckForUpdatesUpdatesWhenChangesDetected() {
        val tipService = service<VimTipService>()
        tipService.saveTips(listOf(VimTip("old", listOf("old-details"))))
        tipService.saveMetadata(TipMetadata(etag = "old-etag"))

        val updatedTips = listOf(
            VimTip("new-1", listOf("new-details-1")),
            VimTip("new-2", listOf("new-details-2"))
        )
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata(etag = "new-etag"))
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(0, fakeTipSource.loadTipsCalls)
        assertEquals(1, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(2, tipService.countTips())
    }

    private fun registerLoader(fakeTipSource: TipSourceService): TipLoaderService {
        project.registerServiceInstance(
            TipSourceService::class.java,
            fakeTipSource
        )
        val loader = TipLoaderServiceImpl(project)
        project.registerServiceInstance(
            TipLoaderService::class.java,
            loader
        )
        return loader
    }

    private class FakeTipSource(
        private val result: TipSourceLoadResult
    ) : TipSourceService {
        var loadTipsCalls = 0
            private set

        var loadTipsConditionalCalls = 0
            private set

        override fun loadTips(): TipSourceLoadResult {
            loadTipsCalls += 1
            return result
        }

        override fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult {
            loadTipsConditionalCalls += 1
            return result
        }
    }
}
