package com.github.pooryam92.vimcoach.services

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
        val fakeRemote = FakeRemoteTipSource(RemoteTipLoadResult.Success(remoteTips))
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
        val fakeRemote = FakeRemoteTipSource(RemoteTipLoadResult.Success(updatedTips))
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

        val fakeRemote = FakeRemoteTipSource(RemoteTipLoadResult.Failure("connection timeout"))
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

        val fakeRemote = FakeRemoteTipSource(RemoteTipLoadResult.Empty)
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

    private class FakeRemoteTipSource(
        private val result: RemoteTipLoadResult
    ) : RemoteTipSourceService {
        var loadCalls = 0
            private set

        override fun loadTips(): RemoteTipLoadResult {
            loadCalls += 1
            return result
        }
    }

    private fun registerLoader(fakeRemote: RemoteTipSourceService): TipLoaderService {
        project.registerServiceInstance(
            RemoteTipSourceService::class.java,
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
