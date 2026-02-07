package com.github.pooryam92.plugin.services

import com.github.pooryam92.plugin.config.VimTipConfig
import com.google.gson.Gson
import com.intellij.openapi.components.Service
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

@Service(Service.Level.PROJECT)
class RemoteTipSource {
    fun loadTips(): List<VimTip>? {
        val remoteUrl = VimTipConfig.REMOTE_URL
        return runCatching {
            val connection = URI(remoteUrl).toURL().openConnection() as HttpURLConnection
            connection.connectTimeout = VimTipConfig.CONNECT_TIMEOUT_MS
            connection.readTimeout = VimTipConfig.READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.inputStream.use { stream ->
                parseTipsJson(stream)
            }
        }.getOrNull()
    }

    private fun parseTipsJson(stream: InputStream): List<VimTip> {
        val tips = GSON.fromJson(
            InputStreamReader(stream, Charsets.UTF_8),
            Array<VimTip>::class.java
        ) ?: return emptyList()
        return tips.mapNotNull { normalizeTip(it) }
    }

    private fun normalizeTip(tip: VimTip): VimTip? {
        val summary = (tip.summary as String?)?.trim().orEmpty()
        val details = (tip.details as String?)?.trim().orEmpty()
        if (summary.isBlank() || details.isBlank()) {
            return null
        }
        val category = (tip.category)?.trim()?.takeIf { it.isNotEmpty() }
        return VimTip(summary, details, category)
    }

    companion object {
        private val GSON = Gson()
    }
}
