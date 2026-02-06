package com.github.pooryam92.plugin.tips.loading

import com.github.pooryam92.plugin.config.VimTipConfig
import com.github.pooryam92.plugin.services.VimTipService
import com.google.gson.Gson
import com.intellij.openapi.components.Service
import java.io.InputStream
import java.io.InputStreamReader

@Service(Service.Level.PROJECT)
class LocalTipSource : TipSource {
    override fun loadTips(): List<VimTipService.VimTip>? {
        val stream = javaClass.classLoader.getResourceAsStream(VimTipConfig.LOCAL_RESOURCE_PATH)
            ?: return emptyList()
        return stream.use { parseTipsJson(it) }
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
