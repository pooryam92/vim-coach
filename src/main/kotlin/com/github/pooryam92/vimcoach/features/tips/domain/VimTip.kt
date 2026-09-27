package com.github.pooryam92.vimcoach.features.tips.domain

data class VimTip(
    var summary: String = "",
    var details: List<String> = emptyList(),
    var category: List<String> = emptyList(),
    var config: TipConfig? = null,
    var advanced: Boolean = false,
    var mode: String? = null,
    var keys: List<TipKeySpan> = emptyList()
) {
    /** The summary followed by the details, indexed the way [TipKeySpan.line] counts. */
    fun textLines(): List<String> = listOf(summary) + details
}
