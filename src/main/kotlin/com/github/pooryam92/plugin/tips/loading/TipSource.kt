package com.github.pooryam92.plugin.tips.loading

import com.github.pooryam92.plugin.services.VimTipService

interface TipSource {
    fun loadTips(): List<VimTipService.VimTip>?
}
