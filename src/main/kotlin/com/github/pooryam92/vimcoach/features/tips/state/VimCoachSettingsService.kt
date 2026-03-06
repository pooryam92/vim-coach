package com.github.pooryam92.vimcoach.features.tips.state

import com.intellij.openapi.components.PersistentStateComponent

interface VimCoachSettingsService : PersistentStateComponent<VimCoachSettingsService.State> {
    fun isShowTipsOnStartupEnabled(): Boolean
    fun setShowTipsOnStartupEnabled(enabled: Boolean)

    data class State(
        var showTipsOnStartup: Boolean = true
    )
}
