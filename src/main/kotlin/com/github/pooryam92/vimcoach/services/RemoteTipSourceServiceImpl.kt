package com.github.pooryam92.vimcoach.services

import com.github.pooryam92.vimcoach.config.VimTipConfig
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

class RemoteTipSourceServiceImpl : RemoteTipSourceService {
    override fun loadTips(): List<VimTip>? {
        val remoteUrl = VimTipConfig.REMOTE_URL
        return runCatching {
            val connection = URI(remoteUrl).toURL().openConnection() as HttpURLConnection
            connection.connectTimeout = VimTipConfig.CONNECT_TIMEOUT_MS
            connection.readTimeout = VimTipConfig.READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.inputStream.use { stream ->
                TipJsonParser.parseTipsJson(stream)
            }
        }.getOrNull()
    }
}
