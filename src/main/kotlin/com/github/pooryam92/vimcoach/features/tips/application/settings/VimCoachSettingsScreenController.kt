package com.github.pooryam92.vimcoach.features.tips.application.settings

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.intellij.openapi.components.service

class VimCoachSettingsScreenController() {
    private var injectedSettingsService: SettingsRepository? = null
    private var injectedTipService: VimTipRepository? = null
    private var injectedRefreshTips: RefreshTips? = null

    internal constructor(
        settingsService: SettingsRepository,
        tipService: VimTipRepository,
        refreshTips: RefreshTips? = null
    ) : this() {
        injectedSettingsService = settingsService
        injectedTipService = tipService
        injectedRefreshTips = refreshTips
    }

    fun loadState(): VimCoachSettingsScreenState {
        val settingsService = settingsService()
        val availableCategories = loadAvailableCategories()

        return VimCoachSettingsScreenState(
            showTipsOnStartup = settingsService.isShowTipsOnStartupEnabled(),
            periodicTipsEnabled = settingsService.isPeriodicTipsEnabled(),
            tipIntervalHours = settingsService.getTipIntervalHours(),
            availableCategories = availableCategories,
            enabledCategories = settingsService.getEnabledTipCategories(availableCategories),
            showAdvancedTips = settingsService.isShowAdvancedTipsEnabled(),
            excludedTips = loadExcludedTips(settingsService.getHiddenTipIds())
        )
    }

    fun saveState(state: VimCoachSettingsScreenState) {
        val settingsService = settingsService()
        settingsService.setShowTipsOnStartupEnabled(state.showTipsOnStartup)
        settingsService.setTipIntervalHours(state.tipIntervalHours)
        settingsService.setPeriodicTipsEnabled(state.periodicTipsEnabled)
        settingsService.setEnabledTipCategories(state.availableCategories, state.enabledCategories)
        settingsService.setShowAdvancedTipsEnabled(state.showAdvancedTips)
        restoreTipsFromSettings(state.restoredExcludedTipIds)
    }

    private fun loadAvailableCategories(): List<String> {
        val tipService = tipService()
        val categories = tipService.getCategories().values
        if (categories.isNotEmpty() || tipService.countTips() == 0) {
            return categories
        }

        // Legacy caches from pre-category versions need a full reload to recover category data.
        // TODO(1.6.0 upgrade bridge): unreachable since loadState drops pre-id (and so pre-category) caches.
        refreshTips().refetchTips()
        return tipService.getCategories().values
    }

    private fun loadExcludedTips(hiddenTipIds: List<String>): List<ExcludedTipSettingsItem> {
        return tipService().getTipsByIds(hiddenTipIds).map { tip ->
            ExcludedTipSettingsItem(
                id = tip.id,
                summary = tip.summary
            )
        }
    }

    private fun restoreTipsFromSettings(ids: List<String>) {
        val settingsService = settingsService()
        ids
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .forEach(settingsService::restoreTip)
    }

    private fun settingsService(): SettingsRepository = injectedSettingsService ?: service()

    private fun tipService(): VimTipRepository = injectedTipService ?: service()

    private fun refreshTips(): RefreshTips = injectedRefreshTips ?: service()
}
