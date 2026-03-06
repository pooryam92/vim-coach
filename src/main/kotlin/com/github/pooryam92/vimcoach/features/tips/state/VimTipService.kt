package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.components.PersistentStateComponent

interface VimTipService : PersistentStateComponent<VimTipService.State> {
    fun countTips(): Int
    fun saveTips(tips: List<VimTip>)
    fun getRandomTip(): VimTip
    fun getMetadata(): TipMetadata
    fun saveMetadata(metadata: TipMetadata)

    data class State(
        var tips: MutableList<VimTip> = mutableListOf(),
        var metadata: TipMetadata = TipMetadata()
    )
}
