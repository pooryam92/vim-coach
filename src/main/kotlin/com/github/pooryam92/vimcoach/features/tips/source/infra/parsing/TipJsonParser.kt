package com.github.pooryam92.vimcoach.features.tips.source.infra.parsing

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader

object TipJsonParser {
    private const val TIPS_FIELD = "tips"
    private val gson = Gson()

    fun parseTipsJson(stream: InputStream): List<VimTip> {
        return InputStreamReader(stream, Charsets.UTF_8).use { reader ->
            val root = JsonParser.parseReader(reader)
            if (!root.isJsonObject) {
                return@use emptyList()
            }
            parseTips(root.asJsonObject)
        }
    }

    private fun parseTips(root: JsonObject): List<VimTip> {
        val element = root.get(TIPS_FIELD)
        if (element == null || !element.isJsonArray) {
            return emptyList()
        }
        val tips = gson.fromJson(element, Array<VimTip>::class.java) ?: return emptyList()
        return tips.mapNotNull(::normalizeTip)
    }

    private fun normalizeTip(tip: VimTip): VimTip? {
        val summary = tip.summary.trim()
        val details = normalizeStrings(tip.details)
        if (summary.isBlank() || details.isEmpty()) {
            return null
        }
        val normalizedCategories = normalizeStrings(tip.category)
        return VimTip(summary, details, normalizedCategories)
    }

    private fun normalizeStrings(values: List<String>): List<String> {
        return values
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
    }
}
