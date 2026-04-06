package com.github.pooryam92.vimcoach.features.tips.state.store

import com.intellij.openapi.components.PersistentStateComponent

interface VimCoachSettingsStore : PersistentStateComponent<VimCoachSettingsStore.State> {
    data class State(
        var showTipsOnStartup: Boolean = true,
        var periodicTipsEnabled: Boolean = false,
        var tipIntervalHours: Int = 1,
        var disabledTipCategories: List<String> = emptyList()
    )

    fun setShowTipsOnStartup(enabled: Boolean)
    fun setPeriodicTipsEnabled(enabled: Boolean)
    fun setTipIntervalHours(hours: Int)
    fun setDisabledTipCategories(categories: List<String>)
}
