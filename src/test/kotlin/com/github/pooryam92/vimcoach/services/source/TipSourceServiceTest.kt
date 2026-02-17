package com.github.pooryam92.vimcoach.services.source

import com.github.pooryam92.vimcoach.services.TipMetadata
import com.github.pooryam92.vimcoach.services.VimTip
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TipSourceServiceTest : BasePlatformTestCase() {

    fun testUsesRemoteSourceByDefault() {
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("r", "d")), TipMetadata())
        )
        val fakeFile = FakeFileTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("f", "d")), TipMetadata())
        )
        val sourceService = registerSourceService(fakeRemote, fakeFile, { null })

        val result = sourceService.loadTips()

        assertTrue(result is TipSourceLoadResult.Success)
        assertEquals(1, fakeRemote.loadCalls)
        assertEquals(0, fakeFile.loadCalls)
    }

    fun testUsesFileSourceWhenModeIsFile() {
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("r", "d")), TipMetadata())
        )
        val fakeFile = FakeFileTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("f", "d")), TipMetadata())
        )
        val sourceService = registerSourceService(fakeRemote, fakeFile, { "file" })

        val result = sourceService.loadTips()

        assertTrue(result is TipSourceLoadResult.Success)
        assertEquals(0, fakeRemote.loadCalls)
        assertEquals(1, fakeFile.loadCalls)
    }

    private fun registerSourceService(
        fakeRemote: RemoteTipSourceService,
        fakeFile: FileTipSourceService,
        modeProvider: () -> String?
    ): TipSourceService {
        return TipSourceServiceImpl(fakeRemote, fakeFile, modeProvider)
    }

    private class FakeRemoteTipSource(
        private val result: TipSourceLoadResult
    ) : RemoteTipSourceService {
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

    private class FakeFileTipSource(
        private val result: TipSourceLoadResult
    ) : FileTipSourceService {
        var loadCalls = 0
            private set

        override fun loadTips(): TipSourceLoadResult {
            loadCalls += 1
            return result
        }
    }
}
