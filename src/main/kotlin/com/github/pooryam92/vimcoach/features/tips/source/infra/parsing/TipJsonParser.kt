package com.github.pooryam92.vimcoach.features.tips.source.infra.parsing

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader

object TipJsonParser {
    private val gson = Gson()

    fun parseTipsJson(stream: InputStream): List<VimTip> {
        return InputStreamReader(stream, Charsets.UTF_8).use { reader ->
            val root = JsonParser.parseReader(reader)
            when {
                root.isJsonArray -> parseTipArray(root)
                root.isJsonObject -> parseGroupedTips(root)
                else -> emptyList()
            }
        }
    }

    private fun parseTipArray(root: JsonElement): List<VimTip> {
        val tips = gson.fromJson(root, Array<VimTip>::class.java) ?: return emptyList()
        return tips.mapNotNull(::normalizeTip)
    }

    private fun parseGroupedTips(root: JsonElement): List<VimTip> {
        val normalizedTips = mutableListOf<VimTip>()
        root.asJsonObject.entrySet().forEach { (_, tipsElement) ->
            if (!tipsElement.isJsonArray) {
                return@forEach
            }
            val tips = gson.fromJson(tipsElement, Array<VimTip>::class.java) ?: emptyArray()
            tips.mapNotNullTo(normalizedTips, ::normalizeTip)
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
