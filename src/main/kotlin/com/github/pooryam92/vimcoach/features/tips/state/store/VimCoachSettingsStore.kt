package com.github.pooryam92.vimcoach.features.tips.state.store

import com.intellij.openapi.components.PersistentStateComponent

interface VimCoachSettingsStore : PersistentStateComponent<VimCoachSettingsStore.State> {
    data class State(
        var showTipsOnStartup: Boolean = true
    )
}
