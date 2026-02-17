package com.github.pooryam92.vimcoach.services

import com.github.pooryam92.vimcoach.services.source.TipSourceLoadResult
import com.github.pooryam92.vimcoach.services.source.TipSourceService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

class TipLoaderServiceRefetchTest : BasePlatformTestCase() {

    fun testRefetchTipsReloadsEvenWhenTipsExist() {
        // Arrange
        val tipService = project.service<VimTipService>()
        tipService.saveTips(listOf(VimTip("old-summary", "old-details")))

        val remoteTips = listOf(
            VimTip("new-summary-1", "new-details-1"),
            VimTip("new-summary-2", "new-details-2")
        )
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(remoteTips, TipMetadata())
        )
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.refetchTips()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(2, tipService.countTips())
        val randomTip = tipService.getRandomTip()
        assertTrue(remoteTips.contains(randomTip))
    }

    fun testRefetchTipsUpdatesExistingTips() {
        // Arrange
        val tipService = project.service<VimTipService>()
        val initialTips = listOf(
            VimTip("initial-1", "initial-details-1"),
            VimTip("initial-2", "initial-details-2"),
            VimTip("initial-3", "initial-details-3")
        )
        tipService.saveTips(initialTips)
        assertEquals(3, tipService.countTips())

        val updatedTips = listOf(
            VimTip("updated-1", "updated-details-1")
        )
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata())
        )
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.refetchTips()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
        val tip = tipService.getRandomTip()
        assertEquals("updated-1", tip.summary)
    }

    fun testRefetchTipsDoesNotSaveWhenRemoteReturnsFailure() {
        // Arrange
        val tipService = project.service<VimTipService>()
        val initialTips = listOf(VimTip("existing", "existing-details"))
        tipService.saveTips(initialTips)

        val fakeRemote = FakeRemoteTipSource(TipSourceLoadResult.Failure("connection timeout"))
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.refetchTips()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.Failed("connection timeout"), result)
        assertEquals(1, tipService.countTips())
        val tip = tipService.getRandomTip()
        assertEquals("existing", tip.summary)
    }

    fun testRefetchTipsDoesNotSaveWhenRemoteReturnsEmpty() {
        // Arrange
        val tipService = project.service<VimTipService>()
        val initialTips = listOf(VimTip("existing", "existing-details"))
        tipService.saveTips(initialTips)

        val fakeRemote = FakeRemoteTipSource(TipSourceLoadResult.Empty)
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.refetchTips()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.NoData, result)
        assertEquals(1, tipService.countTips())
        val tip = tipService.getRandomTip()
        assertEquals("existing", tip.summary)
    }

    fun testRefetchTipsReturnsNotModifiedWhenNoChanges() {
        // Arrange
        val tipService = project.service<VimTipService>()
        val initialTips = listOf(VimTip("existing", "existing-details"))
        tipService.saveTips(initialTips)

        val fakeRemote = FakeRemoteTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.refetchTips()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(1, tipService.countTips())
        val tip = tipService.getRandomTip()
        assertEquals("existing", tip.summary)
    }

    fun testRefetchTipsAlwaysForcesReload() {
        // Arrange: Setup tips with metadata indicating they were just fetched
        val tipService = project.service<VimTipService>()
        val initialTips = listOf(VimTip("existing", "existing-details"))
        tipService.saveTips(initialTips)
        tipService.saveMetadata(TipMetadata(
            etag = "abc123",
            githubSha = "def456",
            lastFetchTimestamp = System.currentTimeMillis()
        ))

        val updatedTips = listOf(VimTip("new", "new-details"))
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata())
        )
        val loader = registerLoader(fakeRemote)

        // Act: Refetch should always force reload
        val result = loader.refetchTips()

        // Assert: Should call loadTips (not loadTipsConditional) and update
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
        val tip = tipService.getRandomTip()
        assertEquals("new", tip.summary)
    }

    fun testCheckForUpdatesLoadsWhenNoTipsExist() {
        // Arrange
        val tipService = project.service<VimTipService>()
        tipService.saveTips(emptyList())

        val remoteTips = listOf(VimTip("new-tip", "new-details"))
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(remoteTips, TipMetadata())
        )
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.checkForUpdates()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
    }

    fun testCheckForUpdatesUsesConditionalWhenTipsExist() {
        // Arrange
        val tipService = project.service<VimTipService>()
        val initialTips = listOf(VimTip("existing", "existing-details"))
        tipService.saveTips(initialTips)
        tipService.saveMetadata(TipMetadata(
            etag = "abc123",
            githubSha = "def456",
            lastFetchTimestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
        ))

        val fakeRemote = FakeRemoteTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.checkForUpdates()

        // Assert: Should use conditional loading and return NotModified
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(1, tipService.countTips())
        val tip = tipService.getRandomTip()
        assertEquals("existing", tip.summary)
    }

    fun testCheckForUpdatesUpdatesWhenChangesDetected() {
        // Arrange
        val tipService = project.service<VimTipService>()
        val initialTips = listOf(VimTip("old", "old-details"))
        tipService.saveTips(initialTips)
        tipService.saveMetadata(TipMetadata(etag = "old-etag"))

        val updatedTips = listOf(
            VimTip("new-1", "new-details-1"),
            VimTip("new-2", "new-details-2")
        )
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata(etag = "new-etag"))
        )
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.checkForUpdates()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(2, tipService.countTips())
    }

    private class FakeRemoteTipSource(
        private val result: TipSourceLoadResult
    ) : TipSourceService {
        var loadCalls = 0
            private set

        override fun loadTips(): TipSourceLoadResult {
            loadCalls += 1
            return result
        }

        override fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult {
            loadCalls += 1
            return result
        }
    }

    private fun registerLoader(fakeRemote: TipSourceService): TipLoaderService {
        project.registerServiceInstance(
            TipSourceService::class.java,
            fakeRemote
        )
        val loader = TipLoaderServiceImpl(project)
        project.registerServiceInstance(
            TipLoaderService::class.java,
            loader
        )
        return loader
    }
}