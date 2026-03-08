package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

interface VimTipService {
    fun countTips(): Int
    fun saveTips(tips: List<VimTip>)
    fun getRandomTip(): VimTip
    fun getMetadata(): TipMetadata
    fun saveMetadata(metadata: TipMetadata)
}
