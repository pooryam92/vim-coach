package com.github.pooryam92.vimcoach.features.tips.domain

data class VimTip(
    var summary: String = "",
    var details: List<String> = emptyList(),
    var category: List<String> = emptyList(),
    var config: TipConfig? = null,
    var mnemonic: String? = null,
    var advanced: Boolean = false
)
