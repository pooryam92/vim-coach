package com.github.pooryam92.vimcoach.features.tips.application.loading.infra.parsing

import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipMode
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Type

object TipJsonParser {
    private const val TIPS_FIELD = "tips"
    private const val CONFIG_NAME_FIELD = "name"
    private const val CONFIG_LINES_FIELD = "lines"
    private const val ADVANCED_FIELD = "advanced"
    private const val MODE_FIELD = "mode"

    private val logger = Logger.getInstance(TipJsonParser::class.java)

    private val gson = GsonBuilder()
        .registerTypeAdapter(TipConfig::class.java, TipConfigDeserializer)
        .create()

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
        element.asJsonArray.forEach(::dropMalformedAdvancedFlag)
        element.asJsonArray.forEach(::dropUnknownMode)
        val tips = gson.fromJson(element, Array<VimTip>::class.java) ?: return emptyList()
        return dropDuplicateSummaries(tips.mapNotNull(::normalizeTip))
    }

    // `advanced` is the only strictly-typed known field: Gson aborts the whole tips array on a
    // non-boolean value, where every other malformed field degrades to a single dropped tip at
    // worst. Parsing must stay lenient (see docs/tips/tips-pipeline.md, "Schema evolution"), so a
    // malformed flag is stripped here and falls back to the default (not advanced).
    private fun dropMalformedAdvancedFlag(tipElement: JsonElement) {
        val tip = tipElement.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return
        val advanced = tip.get(ADVANCED_FIELD) ?: return
        if (!advanced.isJsonPrimitive || !advanced.asJsonPrimitive.isBoolean) {
            logger.warn("Ignoring non-boolean \"$ADVANCED_FIELD\" value on tip: $advanced")
            tip.remove(ADVANCED_FIELD)
        }
    }

    // `mode` names the mode the reader must be in to press the tip's keys; it renders as a quiet
    // title label. Leniency mirrors `advanced` (see docs/tips/tips-pipeline.md, "Schema evolution"):
    // an unknown or malformed value (a mode this plugin version doesn't know, or a non-string) is
    // stripped so it falls back to no label, and a JSON object/array is removed before Gson would
    // otherwise abort the whole tips array trying to coerce it into a String.
    private fun dropUnknownMode(tipElement: JsonElement) {
        val tip = tipElement.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return
        val mode = tip.get(MODE_FIELD) ?: return
        val value = mode.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
        if (TipMode.fromWire(value) == null) {
            logger.warn("Ignoring unknown \"$MODE_FIELD\" value on tip: $mode")
            tip.remove(MODE_FIELD)
        }
    }

    // A tip's trimmed summary is its identity downstream (see TipHash): it keys hidden-tip
    // filtering and hash->tip matching. Two tips sharing a summary would make one unreachable
    // and hiding one would hide both, so we keep the first and drop the rest loudly here.
    private fun dropDuplicateSummaries(tips: List<VimTip>): List<VimTip> {
        val seenSummaries = HashSet<String>(tips.size)
        return tips.filter { tip ->
            seenSummaries.add(tip.summary).also { isUnique ->
                if (!isUnique) {
                    logger.warn("Dropping Vim tip with duplicate summary: \"${tip.summary}\"")
                }
            }
        }
    }

    private fun normalizeTip(tip: VimTip): VimTip? {
        val summary = tip.summary.trim()
        val details = normalizeStrings(tip.details)
        if (summary.isBlank() || details.isEmpty()) {
            return null
        }
        val normalizedCategories = normalizeStrings(tip.category)
        val normalizedConfig = normalizeConfig(tip.config)
        val normalizedMnemonic = tip.mnemonic?.trim()?.takeIf(String::isNotBlank)
        val normalizedMode = TipMode.fromWire(tip.mode)?.wireValue
        return VimTip(
            summary,
            details,
            normalizedCategories,
            normalizedConfig,
            normalizedMnemonic,
            tip.advanced,
            normalizedMode
        )
    }

    private fun normalizeStrings(values: List<String>): List<String> {
        return values
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
    }

    // Config lines are written verbatim into .ideavimrc, so keep order and duplicates
    // (a snippet may legitimately repeat a line); only trim and drop blank lines. A config
    // with no usable lines is dropped entirely so the tip has no apply affordance.
    private fun normalizeConfig(config: TipConfig?): TipConfig? {
        if (config == null) return null
        val lines = config.lines
            .map(String::trim)
            .filter(String::isNotBlank)
        if (lines.isEmpty()) return null
        val name = config.name?.trim()?.takeIf(String::isNotBlank)
        return TipConfig(name, lines)
    }

    /**
     * Accepts either the object form `{ "name": ..., "lines": [...] }` or the legacy array form
     * `["line", ...]` (treated as lines with no name), so older sources keep parsing.
     */
    private object TipConfigDeserializer : JsonDeserializer<TipConfig> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): TipConfig {
            return when {
                json.isJsonArray -> TipConfig(name = null, lines = readLines(json))
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    val name = obj.get(CONFIG_NAME_FIELD)
                        ?.takeIf { it.isJsonPrimitive }
                        ?.asString
                    TipConfig(name = name, lines = readLines(obj.get(CONFIG_LINES_FIELD)))
                }
                else -> TipConfig()
            }
        }

        private fun readLines(element: JsonElement?): List<String> {
            if (element == null || !element.isJsonArray) return emptyList()
            return element.asJsonArray.mapNotNull { line ->
                line.takeIf { it.isJsonPrimitive }?.asString
            }
        }
    }
}
