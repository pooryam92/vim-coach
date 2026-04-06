package com.github.pooryam92.vimcoach.features.tips.state.store

import com.intellij.openapi.components.State
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Storage

@State(name = "VimCoachSettings", storages = [Storage("vim-coach-settings.xml")])
class VimCoachSettingsStoreImpl :
    SerializablePersistentStateComponent<VimCoachSettingsStore.State>(VimCoachSettingsStore.State()),
    VimCoachSettingsStore {

    override fun setShowTipsOnStartup(enabled: Boolean) {
        updateState { currentState ->
            currentState.copy(showTipsOnStartup = enabled)
        }
    }

    override fun setPeriodicTipsEnabled(enabled: Boolean) {
        updateState { currentState ->
            currentState.copy(periodicTipsEnabled = enabled)
        }
    }

    override fun setTipIntervalHours(hours: Int) {
        updateState { currentState ->
            currentState.copy(tipIntervalHours = hours)
        }
    }

    override fun setEnabledTipCategories(categories: List<String>) {
        updateState { currentState ->
            currentState.copy(enabledTipCategories = categories.toList())
        }
    }
}
