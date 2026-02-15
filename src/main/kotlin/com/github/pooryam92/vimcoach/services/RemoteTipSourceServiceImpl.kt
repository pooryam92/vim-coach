package com.github.pooryam92.vimcoach.services

import com.github.pooryam92.vimcoach.config.VimTipConfig
import com.intellij.util.io.HttpRequests

class RemoteTipSourceServiceImpl : RemoteTipSourceService {
    override fun loadTips(): RemoteTipLoadResult {
        return try {
            val tips = HttpRequests.request(VimTipConfig.REMOTE_URL)
                .connectTimeout(VimTipConfig.CONNECT_TIMEOUT_MS)
                .readTimeout(VimTipConfig.READ_TIMEOUT_MS)
                .connect { request ->
                    TipJsonParser.parseTipsJson(request.inputStream)
                }

            if (tips.isEmpty()) {
                RemoteTipLoadResult.Empty
            } else {
                RemoteTipLoadResult.Success(tips)
            }
        } catch (e: Exception) {
            RemoteTipLoadResult.Failure(e.message ?: "Unknown error", e)
        }
    }
}
