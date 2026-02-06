package com.github.pooryam92.plugin.services

import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class TipCache {
    @Volatile
    private var cachedTips: List<VimTipService.VimTip>? = null

    fun get(): List<VimTipService.VimTip>? = cachedTips

    fun set(tips: List<VimTipService.VimTip>) {
        cachedTips = tips
    }

    fun clear() {
        cachedTips = null
    }
}
