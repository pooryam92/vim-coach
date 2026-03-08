package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.intellij.openapi.components.service

class VimCoachSettingsServiceImpl(
    private val settingsStore: VimCoachSettingsStore = service()
) : VimCoachSettingsService {

    override fun isShowTipsOnStartupEnabled(): Boolean {
        return currentState().showTipsOnStartup
    }

    override fun setShowTipsOnStartupEnabled(enabled: Boolean) {
        settingsStore.setShowTipsOnStartup(enabled)
    }

    private fun currentState(): VimCoachSettingsStore.State {
        return settingsStore.state ?: VimCoachSettingsStore.State()
    }
}
