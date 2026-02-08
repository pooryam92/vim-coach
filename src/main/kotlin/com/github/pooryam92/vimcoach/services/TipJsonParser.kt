package com.github.pooryam92.vimcoach.services

import com.google.gson.Gson
import java.io.InputStream
import java.io.InputStreamReader

object TipJsonParser {
    private val gson = Gson()

    fun parseTipsJson(stream: InputStream): List<VimTip> {
        val tips = gson.fromJson(
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
}
