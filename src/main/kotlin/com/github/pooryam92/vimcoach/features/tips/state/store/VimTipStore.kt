package com.github.pooryam92.vimcoach.features.tips.state.store

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.components.PersistentStateComponent

interface VimTipStore : PersistentStateComponent<VimTipStore.State> {
    data class State(
        var tips: List<VimTip> = emptyList(),
        var metadata: TipMetadata = TipMetadata()
    )

    fun setTips(tips: List<VimTip>)
    fun setMetadata(metadata: TipMetadata)
}
