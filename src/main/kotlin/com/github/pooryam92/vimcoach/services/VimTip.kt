package com.github.pooryam92.vimcoach.services

data class VimTip(
    var summary: String = "",
    var details: String = "",
    var category: String? = null,
    var mode: String? = null
)