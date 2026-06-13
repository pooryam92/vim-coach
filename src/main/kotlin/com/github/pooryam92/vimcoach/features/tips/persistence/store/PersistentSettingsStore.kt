package com.github.pooryam92.vimcoach.features.tips.persistence.store

import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "VimCoachSettings", storages = [Storage("vim-coach-settings.xml")])
class PersistentSettingsStore :
    SerializablePersistentStateComponent<PersistentSettingsStore.State>(State()) {

    data class State(
        var showTipsOnStartup: Boolean = true,
        var periodicTipsEnabled: Boolean = false,
        var tipIntervalHours: Int = 1,
        var disabledTipCategories: List<String> = emptyList(),
        var hiddenTipHashes: List<String> = emptyList(),
        var excludedTipsManagementHintShown: Boolean = false
    )

    fun setShowTipsOnStartup(enabled: Boolean) {
        updateState { it.copy(showTipsOnStartup = enabled) }
    }

    fun setPeriodicTipsEnabled(enabled: Boolean) {
        updateState { it.copy(periodicTipsEnabled = enabled) }
    }

    fun setTipIntervalHours(hours: Int) {
        updateState { it.copy(tipIntervalHours = hours) }
    }

    fun setDisabledTipCategories(categories: List<String>) {
        updateState { it.copy(disabledTipCategories = categories.toList()) }
    }

    fun setHiddenTipHashes(hashes: List<String>) {
        updateState { it.copy(hiddenTipHashes = hashes.toList()) }
    }

    fun setExcludedTipsManagementHintShown(shown: Boolean) {
        updateState { it.copy(excludedTipsManagementHintShown = shown) }
    }
}
