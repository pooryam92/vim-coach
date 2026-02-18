package com.github.pooryam92.vimcoach.services

import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader

object TipJsonParser {
    private val gson = Gson()

    fun parseTipsJson(stream: InputStream): List<VimTip> {
        val reader = InputStreamReader(stream, Charsets.UTF_8)
        val root = JsonParser.parseReader(reader)

        if (root.isJsonArray) {
            val tips = gson.fromJson(root, Array<VimTip>::class.java) ?: return emptyList()
            return tips.mapNotNull { normalizeTip(it) }
        }

        if (!root.isJsonObject) {
            return emptyList()
        }

        val normalizedTips = mutableListOf<VimTip>()
        root.asJsonObject.entrySet().forEach { (_, tipsElement) ->
            if (!tipsElement.isJsonArray) {
                return@forEach
            }
            val tips = gson.fromJson(tipsElement, Array<VimTip>::class.java) ?: emptyArray()
            tips.mapNotNullTo(normalizedTips) { normalizeTip(it) }
        }
        return normalizedTips
    }

    private fun normalizeTip(tip: VimTip): VimTip? {
        val summary = (tip.summary as String?)?.trim().orEmpty()
        val details = tip.details.map {
            it.trim()
        }.filter { it.isNotBlank() }
        if (summary.isBlank() || details.isEmpty()) {
            return null
        }
        return VimTip(summary, details)
    }
}
