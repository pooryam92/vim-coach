package com.github.pooryam92.vimcoach.features.tips.application.search

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.TipMode
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.intellij.openapi.components.service

/**
 * Lists every cached tip for hand-picking. Unlike [com.github.pooryam92.vimcoach.features.tips.application.selection.SelectNextTip]
 * nothing is filtered out: category, advanced, muted and config-tip settings shape the random
 * rotation, not what a user may look up on purpose, so muted tips are only flagged.
 */
class FindTips internal constructor(
    private val repository: VimTipRepository,
    private val settings: SettingsRepository,
) {
    constructor() : this(service(), service())

    fun entries(): List<FindTipEntry> {
        val tips = repository.getTips()
        if (tips.isEmpty()) return emptyList()

        val hiddenTipHashes = settings.getHiddenTipHashes().toSet()
        return tips.map { tip ->
            val muted = TipHash.fromTip(tip).value in hiddenTipHashes
            FindTipEntry(tip = tip, muted = muted, searchText = searchTextOf(tip, muted))
        }
    }

    // Original case is kept on purpose: the popup matches case-insensitively, and camel-hump word
    // starts such as "NERDTree" only survive if the text isn't lower-cased.
    private fun searchTextOf(tip: VimTip, muted: Boolean): String {
        val parts = buildList {
            add(tip.summary)
            addAll(tip.details)
            addAll(tip.category)
            tip.mnemonic?.let(::add)
            TipMode.fromWire(tip.mode)?.let { add(it.label) }
            if (tip.advanced) add(ADVANCED_KEYWORD)
            if (muted) add(MUTED_KEYWORD)
        }
        return parts
            .map(String::trim)
            .filter(String::isNotEmpty)
            .joinToString(" ")
    }

    private companion object {
        const val ADVANCED_KEYWORD = "advanced"
        const val MUTED_KEYWORD = "muted"
    }
}

/** A tip as listed by Find Tip. [searchText] is the text the popup filters on, case-insensitively. */
data class FindTipEntry(
    val tip: VimTip,
    val muted: Boolean,
    val searchText: String,
)
