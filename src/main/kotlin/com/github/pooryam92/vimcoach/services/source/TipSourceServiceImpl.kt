package com.github.pooryam92.vimcoach.services.source

class TipSourceServiceImpl(
    private val remoteTipSource: RemoteTipSourceService = RemoteTipSourceServiceImpl(),
    private val fileTipSource: FileTipSourceService = FileTipSourceServiceImpl(),
    private val sourceModeProvider: () -> String? = { System.getProperty(TIP_SOURCE_MODE_PROPERTY) }
) : TipSourceService {
    override fun loadTips(): TipSourceLoadResult {
        return when (resolveSourceMode()) {
            TipSourceMode.FILE -> fileTipSource.loadTips()
            TipSourceMode.REMOTE -> remoteTipSource.loadTips()
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
    }
}
