package com.github.pooryam92.vimcoach.features.tips.unit.source.application

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceServiceImpl
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.source.infra.file.FileTipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.infra.remote.RemoteTipSourceService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TipSourceServiceUnitTest {

    @Test
    fun usesRemoteSourceByDefault() {
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("r", listOf("d"))), TipMetadata())
        )
        val fakeFile = FakeFileTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("f", listOf("d"))), TipMetadata())
        )
        val sourceService = createSourceService(fakeRemote, fakeFile) { null }

        val result = sourceService.loadTips()

        assertTrue(result is TipSourceLoadResult.Success)
        assertEquals(1, fakeRemote.loadTipsCalls)
        assertEquals(0, fakeRemote.loadTipsConditionalCalls)
        assertEquals(0, fakeFile.loadCalls)
    }

    @Test
    fun usesFileSourceWhenModeIsFile() {
        val fakeRemote = FakeRemoteTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("r", listOf("d"))), TipMetadata())
        )
        val fakeFile = FakeFileTipSource(
            TipSourceLoadResult.Success(listOf(VimTip("f", listOf("d"))), TipMetadata())
        )
        val sourceService = createSourceService(fakeRemote, fakeFile) { "file" }

        val result = sourceService.loadTips()

        assertTrue(result is TipSourceLoadResult.Success)
        assertEquals(0, fakeRemote.loadTipsCalls)
        assertEquals(0, fakeRemote.loadTipsConditionalCalls)
        assertEquals(1, fakeFile.loadCalls)
    }

    @Test
    fun conditionalLoadUsesRemoteByDefault() {
        val fakeRemote = FakeRemoteTipSource(TipSourceLoadResult.Empty)
        val fakeFile = FakeFileTipSource(TipSourceLoadResult.Empty)
        val sourceService = createSourceService(fakeRemote, fakeFile) { null }

        sourceService.loadTipsConditional(TipMetadata(etag = "etag"))

        assertEquals(0, fakeRemote.loadTipsCalls)
        assertEquals(1, fakeRemote.loadTipsConditionalCalls)
        assertEquals(0, fakeFile.loadCalls)
    }

    @Test
    fun conditionalLoadUsesFileWhenModeIsFile() {
        val fakeRemote = FakeRemoteTipSource(TipSourceLoadResult.Empty)
        val fakeFile = FakeFileTipSource(TipSourceLoadResult.Empty)
        val sourceService = createSourceService(fakeRemote, fakeFile) { "file" }

        sourceService.loadTipsConditional(TipMetadata(etag = "etag"))

        assertEquals(0, fakeRemote.loadTipsCalls)
        assertEquals(0, fakeRemote.loadTipsConditionalCalls)
        assertEquals(1, fakeFile.loadCalls)
    }

    private fun createSourceService(
        fakeRemote: RemoteTipSourceService,
        fakeFile: FileTipSourceService,
        modeProvider: () -> String?
    ): TipSourceService {
        return TipSourceServiceImpl(fakeRemote, fakeFile, modeProvider)
    }

    private class FakeRemoteTipSource(
        private val result: TipSourceLoadResult
    ) : RemoteTipSourceService {
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
