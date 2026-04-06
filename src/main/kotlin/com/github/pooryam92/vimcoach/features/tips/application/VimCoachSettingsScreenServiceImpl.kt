package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service

class VimCoachSettingsScreenServiceImpl() : VimCoachSettingsScreenService {
    private var injectedSettingsService: VimCoachSettingsService? = null
    private var injectedTipService: VimTipService? = null
    private var injectedTipLoaderService: TipLoaderService? = null

    internal constructor(
        settingsService: VimCoachSettingsService,
        tipService: VimTipService,
        tipLoaderService: TipLoaderService? = null
    ) : this() {
        injectedSettingsService = settingsService
        injectedTipService = tipService
        injectedTipLoaderService = tipLoaderService
    }

    override fun loadState(): VimCoachSettingsScreenState {
        val settingsService = settingsService()
        val availableCategories = loadAvailableCategories()

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

    private fun loadAvailableCategories(): List<String> {
        val tipService = tipService()
        val categories = tipService.getCategories().values
        if (categories.isNotEmpty() || tipService.countTips() == 0) {
            return categories
        }

        // Legacy caches from pre-category versions need a full reload to recover category data.
        tipLoaderService().refetchTips()
        return tipService.getCategories().values
    }

    private fun settingsService(): VimCoachSettingsService = injectedSettingsService ?: service()

    private fun tipService(): VimTipService = injectedTipService ?: service()

    private fun tipLoaderService(): TipLoaderService = injectedTipLoaderService ?: service()
}
