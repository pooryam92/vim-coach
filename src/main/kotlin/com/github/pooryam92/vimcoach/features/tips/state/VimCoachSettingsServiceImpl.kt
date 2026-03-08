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

    override fun isPeriodicTipsEnabled(): Boolean {
        return currentState().periodicTipsEnabled
    }

    override fun setPeriodicTipsEnabled(enabled: Boolean) {
        settingsStore.setPeriodicTipsEnabled(enabled)
    }

    override fun getTipIntervalHours(): Int {
        return currentState().tipIntervalHours.coerceAtLeast(MIN_TIP_INTERVAL_HOURS)
    }

    override fun setTipIntervalHours(hours: Int) {
        settingsStore.setTipIntervalHours(hours.coerceAtLeast(MIN_TIP_INTERVAL_HOURS))
    }

    private fun currentState(): VimCoachSettingsStore.State {
        return settingsStore.state ?: VimCoachSettingsStore.State()
    }

    private companion object {
        const val MIN_TIP_INTERVAL_HOURS = 1
    }
}
