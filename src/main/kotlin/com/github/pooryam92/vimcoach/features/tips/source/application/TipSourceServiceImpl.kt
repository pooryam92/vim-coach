package com.github.pooryam92.vimcoach.features.tips.source.application

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.source.infra.file.FileTipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.infra.file.FileTipSourceServiceImpl
import com.github.pooryam92.vimcoach.features.tips.source.infra.remote.RemoteTipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.infra.remote.RemoteTipSourceServiceImpl
import com.intellij.openapi.diagnostic.Logger

class TipSourceServiceImpl(
    // TODO: Move remote loading orchestration to an application-level service and keep this source service focused on source selection.
    private val remoteTipSource: RemoteTipSourceService = RemoteTipSourceServiceImpl(),
    private val fileTipSource: FileTipSourceService = FileTipSourceServiceImpl(),
    private val sourceModeProvider: () -> String? = { System.getProperty(TIP_SOURCE_MODE_PROPERTY) }
) : TipSourceService {

    override fun loadTips(): TipSourceLoadResult {
        return loadFromSelectedSource(
            loadFromFile = fileTipSource::loadTips,
            loadFromRemote = remoteTipSource::loadTips
        )
    }

    override fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult {
        return loadFromSelectedSource(
            loadFromFile = fileTipSource::loadTips,
            loadFromRemote = { remoteTipSource.loadTipsConditional(metadata) }
        )
    }

    private fun loadFromSelectedSource(
        loadFromFile: () -> TipSourceLoadResult,
        loadFromRemote: () -> TipSourceLoadResult
    ): TipSourceLoadResult {
        val sourceMode = resolveSourceMode()
        logger.info("Selected Vim tip source mode: $sourceMode")
        return when (sourceMode) {
            TipSourceMode.FILE -> loadFromFile()
            TipSourceMode.REMOTE -> loadFromRemote()
        }
    }

    private fun resolveSourceMode(): TipSourceMode {
        return when (sourceModeProvider.invoke()?.trim()?.lowercase()) {
            MODE_FILE -> TipSourceMode.FILE
            else -> TipSourceMode.REMOTE
        }
    }

    private enum class TipSourceMode {
        REMOTE,
        FILE
    }

    private companion object {
        const val MODE_FILE = "file"
        const val TIP_SOURCE_MODE_PROPERTY = "vimcoach.tip.source"
        val logger = Logger.getInstance(TipSourceServiceImpl::class.java)
    }
}
