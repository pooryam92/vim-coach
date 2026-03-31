package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service

class VimCoachSettingsScreenServiceImpl() : VimCoachSettingsScreenService {
    private var injectedSettingsService: VimCoachSettingsService? = null
    private var injectedTipService: VimTipService? = null

    internal constructor(
        settingsService: VimCoachSettingsService,
        tipService: VimTipService
    ) : this() {
        injectedSettingsService = settingsService
        injectedTipService = tipService
    }

    override fun loadState(): VimCoachSettingsScreenState {
        val settingsService = settingsService()
        val availableCategories = tipService().getCategories().values

        return VimCoachSettingsScreenState(
            showTipsOnStartup = settingsService.isShowTipsOnStartupEnabled(),
            periodicTipsEnabled = settingsService.isPeriodicTipsEnabled(),
            tipIntervalHours = settingsService.getTipIntervalHours(),
            availableCategories = availableCategories,
            enabledCategories = settingsService.getEnabledTipCategories(availableCategories)
        )
    }

    override fun saveState(state: VimCoachSettingsScreenState) {
        val settingsService = settingsService()
        settingsService.setShowTipsOnStartupEnabled(state.showTipsOnStartup)
        settingsService.setTipIntervalHours(state.tipIntervalHours)
        settingsService.setPeriodicTipsEnabled(state.periodicTipsEnabled)
        settingsService.setEnabledTipCategories(state.enabledCategories)
    }

    private fun settingsService(): VimCoachSettingsService = injectedSettingsService ?: service()

    private fun tipService(): VimTipService = injectedTipService ?: service()
}
