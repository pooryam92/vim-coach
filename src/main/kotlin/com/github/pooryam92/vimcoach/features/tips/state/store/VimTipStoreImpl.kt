package com.github.pooryam92.vimcoach.features.tips.state.store

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros

@State(name = "VimTipCache",
    storages = [Storage(StoragePathMacros.CACHE_FILE, roamingType = RoamingType.DISABLED)])
class VimTipStoreImpl :
    SerializablePersistentStateComponent<VimTipStore.State>(VimTipStore.State()),
    VimTipStore {
    override fun setTipCache(tips: List<VimTip>, categories: TipCategories) {
        updateState { currentState ->
            currentState.copy(
                tips = tips.toList(),
                categories = categories.copy()
            )
        }
    }

    override fun setMetadata(metadata: TipMetadata) {
        updateState { currentState ->
            currentState.copy(metadata = metadata.copy())
        }
    }
}
