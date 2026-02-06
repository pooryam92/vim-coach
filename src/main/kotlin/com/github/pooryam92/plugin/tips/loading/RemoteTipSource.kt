package com.github.pooryam92.plugin.tips.loading

import com.github.pooryam92.plugin.config.VimTipConfig
import com.github.pooryam92.plugin.services.VimTipService
import com.google.gson.Gson
import com.intellij.openapi.components.Service
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

@Service(Service.Level.PROJECT)
class RemoteTipSource : TipSource {
    override fun loadTips(): List<VimTipService.VimTip>? {
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

    private fun parseTipsJson(stream: InputStream): List<VimTipService.VimTip> {
        val tips = GSON.fromJson(
            InputStreamReader(stream, Charsets.UTF_8),
            Array<VimTipJson>::class.java
        ) ?: return emptyList()
        return tips.mapNotNull { toTip(it) }
    }

    private fun toTip(json: VimTipJson): VimTipService.VimTip? {
        val summary = json.summary?.trim().orEmpty()
        val details = json.details?.trim().orEmpty()
        if (summary.isBlank() || details.isBlank()) {
            return null
        }
        val category = json.category?.trim()?.takeIf { it.isNotEmpty() }
        return VimTipService.VimTip(summary, details, category)
    }

    private data class VimTipJson(
        val summary: String?,
        val details: String?,
        val category: String? = null
    )

    companion object {
        private val GSON = Gson()
    }
}
