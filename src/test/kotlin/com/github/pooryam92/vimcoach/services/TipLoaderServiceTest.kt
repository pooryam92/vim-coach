package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

class TipLoaderServiceTest : BasePlatformTestCase() {

    fun testLoadTipsSkipsWhenTipsAlreadyPresent() {
        // Arrange
        val tipService = project.service<VimTipService>()
        tipService.saveTips(listOf(VimTip("summary", "details")))

        val fakeRemote = FakeRemoteTipSource(
            RemoteTipLoadResult.Success(listOf(VimTip("remote-summary", "remote-details")))
        )

        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.loadTips()

        // Assert
        assertEquals(TipLoadResult.SkippedAlreadyLoaded, result)
        assertEquals(0, fakeRemote.loadCalls)
        assertEquals(1, tipService.countTips())
    }

    fun testLoadTipsSavesRemoteTipsWhenEmpty() {
        // Arrange
        val tipService = project.service<VimTipService>()
        tipService.saveTips(emptyList())

        val remoteTips = listOf(
            VimTip("summary-1", "details-1"),
            VimTip("summary-2", "details-2")
        )
        val fakeRemote = FakeRemoteTipSource(RemoteTipLoadResult.Success(remoteTips))
        val loader = registerLoader(fakeRemote)

        // Act
        val result = loader.loadTips()

        // Assert
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(2, tipService.countTips())
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
