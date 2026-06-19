package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.application.scheduling.ScheduleTips
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager

class SettingsRepositoryImpl() : SettingsRepository {
    private var injectedSettingsStore: PersistentSettingsStore? = null

    internal constructor(settingsStore: PersistentSettingsStore) : this() {
        injectedSettingsStore = settingsStore
    }

    override fun isShowTipsOnStartupEnabled(): Boolean {
        return currentState().showTipsOnStartup
    }

    override fun setShowTipsOnStartupEnabled(enabled: Boolean) {
        settingsStore().setShowTipsOnStartup(enabled)
    }

    override fun isPeriodicTipsEnabled(): Boolean {
        return currentState().periodicTipsEnabled
    }

    override fun setPeriodicTipsEnabled(enabled: Boolean) {
        if (currentState().periodicTipsEnabled == enabled) {
            return
        }
        settingsStore().setPeriodicTipsEnabled(enabled)
        notifyPeriodicSchedulerSettingsChanged()
    }

    override fun getTipIntervalHours(): Int {
        return currentState().tipIntervalHours.coerceAtLeast(MIN_TIP_INTERVAL_HOURS)
    }

    override fun setTipIntervalHours(hours: Int) {
        val normalizedHours = hours.coerceAtLeast(MIN_TIP_INTERVAL_HOURS)
        val previousState = currentState()
        if (previousState.tipIntervalHours == normalizedHours) {
            return
        }

        settingsStore().setTipIntervalHours(normalizedHours)
        if (previousState.periodicTipsEnabled) {
            notifyPeriodicSchedulerSettingsChanged()
        }
    }

    override fun getEnabledTipCategories(availableCategories: List<String>): List<String> {
        val normalizedAvailable = normalizeCategoryNames(availableCategories)
        val disabledCategories = disabledCategorySet()

        return normalizedAvailable
            .asSequence()
            .filterNot(disabledCategories::contains)
            .toList()
    }

    override fun setEnabledTipCategories(availableCategories: List<String>, enabledCategories: List<String>) {
        val normalizedDisabled = disabledCategoriesFor(availableCategories, enabledCategories)

        if (disabledCategoryList() == normalizedDisabled) {
            return
        }

        settingsStore().setDisabledTipCategories(normalizedDisabled)
    }

    override fun getHiddenTipHashes(): List<String> {
        return normalizeHashes(currentState().hiddenTipHashes)
    }

    override fun hideTip(hash: String) {
        val normalizedHash = normalizeHash(hash) ?: return
        val current = getHiddenTipHashes()
        val updated = (current + normalizedHash).distinct()
        if (updated != current) {
            settingsStore().setHiddenTipHashes(updated)
        }
    }

    override fun restoreTip(hash: String) {
        val normalizedHash = normalizeHash(hash) ?: return
        val current = getHiddenTipHashes()
        val updated = current.filterNot { it == normalizedHash }
        if (updated != current) {
            settingsStore().setHiddenTipHashes(updated)
        }
    }

    override fun consumeExcludedTipsManagementHint(): Boolean {
        if (currentState().excludedTipsManagementHintShown) {
            return false
        }

        settingsStore().setExcludedTipsManagementHintShown(true)
        return true
    }

    private fun currentState(): PersistentSettingsStore.State {
        return settingsStore().state
    }

    private fun settingsStore(): PersistentSettingsStore {
        return injectedSettingsStore ?: service()
    }

    private fun disabledCategoriesFor(
        availableCategories: List<String>,
        enabledCategories: List<String>
    ): List<String> {
        val normalizedAvailable = normalizeCategoryNames(availableCategories)
        val normalizedEnabled = normalizeCategoryNames(enabledCategories).toSet()
        return normalizedAvailable.filterNot(normalizedEnabled::contains)
    }

    private fun disabledCategoryList(): List<String> {
        return normalizeCategoryNames(currentState().disabledTipCategories)
    }

    private fun disabledCategorySet(): Set<String> {
        return disabledCategoryList().toSet()
    }

    private fun normalizeCategoryNames(categories: List<String>): List<String> {
        return categories
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .toList()
    }

    private fun normalizeHashes(hashes: List<String>): List<String> {
        return hashes.mapNotNull(::normalizeHash).distinct()
    }

    private fun normalizeHash(hash: String): String? {
        return hash.trim().takeIf(String::isNotBlank)
    }

    private companion object {
        const val MIN_TIP_INTERVAL_HOURS = 1
    }

    private fun notifyPeriodicSchedulerSettingsChanged() {
        val app = ApplicationManager.getApplication() ?: return
        if (app.isDisposed) {
            return
        }

        ProjectManager.getInstance().openProjects
            .filter { !it.isDisposed }
            .forEach { project ->
                project.service<ScheduleTips>().onSettingsChanged()
            }
    }
}
