package com.github.pooryam92.vimcoach.services

import com.github.pooryam92.vimcoach.config.VimTipConfig
import com.intellij.util.io.HttpRequests

class RemoteTipSourceServiceImpl : RemoteTipSourceService {
    override fun loadTips(): List<VimTip>? {
        return HttpRequests.request(VimTipConfig.REMOTE_URL)
            .connectTimeout(VimTipConfig.CONNECT_TIMEOUT_MS)
            .readTimeout(VimTipConfig.READ_TIMEOUT_MS)
            .connect { request ->
                TipJsonParser.parseTipsJson(request.inputStream)
            }
    }
}
