package com.github.pooryam92.vimcoach.features.tips.persistence.store

import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "VimCoachTipRotation",
    storages = [Storage("vim-coach-tip-rotation.xml", roamingType = RoamingType.DISABLED)])
class PersistentTipRotationStore :
    SerializablePersistentStateComponent<PersistentTipRotationStore.State>(State()) {

    data class State(
        var timesShown: Map<String, Int> = emptyMap(),
        var lastShownKey: String? = null
    )

    /** [transform] may run more than once under contention; only its last run is saved. */
    fun updateProgress(transform: (State) -> State) {
        updateState(transform)
    }
}
