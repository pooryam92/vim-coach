package com.github.pooryam92.plugin.services

import com.github.pooryam92.plugin.services.VimTipService.VimTip

interface TipProvider {
    fun getRandomTip(): VimTip
    fun getAllTips(): List<VimTip>
}
