package com.github.pooryam92.vimcoach.services.source

import com.github.pooryam92.vimcoach.config.VimTipConfig
import com.github.pooryam92.vimcoach.services.TipJsonParser
import com.intellij.util.io.HttpRequests

class RemoteTipSourceServiceImpl : RemoteTipSourceService {
    override fun loadTips(): TipSourceLoadResult {
        return try {
            val tips = HttpRequests.request(VimTipConfig.REMOTE_TIPS_URL)
                .connect { request ->
                    TipJsonParser.parseTipsJson(request.inputStream)
                }

            if (tips.isEmpty()) {
                TipSourceLoadResult.Empty
            } else {
                TipSourceLoadResult.Success(tips)
            }
        } catch (e: Exception) {
            TipSourceLoadResult.Failure(e.message ?: "Unknown error", e)
        }
    }
}
