package com.github.pooryam92.vimcoach.features.tips.persistence.store

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros

@State(name = "VimTipCache",
    storages = [Storage(StoragePathMacros.CACHE_FILE, roamingType = RoamingType.DISABLED)])
class PersistentVimTipStore :
    SerializablePersistentStateComponent<PersistentVimTipStore.State>(State()) {

    data class State(
        var tips: List<VimTip> = emptyList(),
        var categories: TipCategories = TipCategories(),
        var metadata: TipMetadata = TipMetadata()
    )

    // A cache written before generated ids holds id-less tips; reading them as no tips shows
    // "No tips found." until the upgrade refetch lands. Categories stay so Settings → Apply
    // can't wipe the user's disabled categories while that refetch is failing.
    override fun loadState(state: State) {
        val hasIdLessTips = state.tips.any { it.id.isBlank() }
        super.loadState(if (hasIdLessTips) state.copy(tips = emptyList()) else state)
    }

    fun setTipCache(tips: List<VimTip>, categories: TipCategories) {
        updateState { it.copy(tips = tips.toList(), categories = categories.copy()) }
    }

    fun setMetadata(metadata: TipMetadata) {
        updateState { it.copy(metadata = metadata.copy()) }
    }
}
