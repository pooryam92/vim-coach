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
        var excludedTipsManagementHintShown: Boolean = false,
        // Off by default: a pre-feature store has no field and deserializes to off, so existing
        // users keep seeing only normal tips until they opt in.
        var showAdvancedTips: Boolean = false,
        var advancedTipsHintShown: Boolean = false,
        // Counts tips shown so the advanced-tips nudge holds off until the user has seen a few,
        // rather than firing on the very first tip after install. Stops advancing once eligible.
        var tipsShownForAdvancedNudge: Int = 0
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

    fun setShowAdvancedTips(enabled: Boolean) {
        updateState { it.copy(showAdvancedTips = enabled) }
    }

    fun setAdvancedTipsHintShown(shown: Boolean) {
        updateState { it.copy(advancedTipsHintShown = shown) }
    }

    fun setTipsShownForAdvancedNudge(count: Int) {
        updateState { it.copy(tipsShownForAdvancedNudge = count) }
    }
}
