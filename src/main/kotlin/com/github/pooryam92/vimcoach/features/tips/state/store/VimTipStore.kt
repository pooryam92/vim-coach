package com.github.pooryam92.vimcoach.features.tips.state.store

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.components.PersistentStateComponent

interface VimTipStore : PersistentStateComponent<VimTipStore.State> {
    data class State(
        var tips: List<VimTip> = emptyList(),
        var categories: TipCategories = TipCategories(),
        var metadata: TipMetadata = TipMetadata()
    )

    fun setTipCache(tips: List<VimTip>, categories: TipCategories)
    fun setMetadata(metadata: TipMetadata)
}
