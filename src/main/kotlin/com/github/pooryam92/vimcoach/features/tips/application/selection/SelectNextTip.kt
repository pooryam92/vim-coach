package com.github.pooryam92.vimcoach.features.tips.application.selection

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.intellij.openapi.components.service

/**
 * Single chokepoint for "which tip does the user see next": builds a [TipSelectionContext] from
 * [SettingsRepository], runs it through the [TipFilter] chain, then draws from [TipRotation].
 * Registered as an application service so the rotation it owns — deliberately in-memory — is
 * shared across every project and entry point, the same way it was as part of the repository
 * before this class existed.
 */
class SelectNextTip() {
    private var injectedTipRepository: VimTipRepository? = null
    private var injectedSettingsService: SettingsRepository? = null

    internal constructor(tipRepository: VimTipRepository) : this() {
        injectedTipRepository = tipRepository
    }

    internal constructor(
        tipRepository: VimTipRepository,
        settingsService: SettingsRepository
    ) : this() {
        injectedTipRepository = tipRepository
        injectedSettingsService = settingsService
    }

    private val filters: List<TipFilter> =
        listOf(categoryFilter, excludedTipsFilter, configTipsFilter, advancedTipsFilter)
    private val rotation = TipRotation()

    fun select(includeConfigTips: Boolean): VimTip {
        val allTips = tipRepository().getTips()
        if (allTips.isEmpty()) return FALLBACK_TIP

        val context = buildContext(includeConfigTips)
        val filteredPool = filters.fold(allTips) { pool, filter -> filter.apply(pool, context) }
        return rotation.selectFrom(filteredPool) ?: FILTERED_FALLBACK_TIP
    }

    private fun buildContext(includeConfigTips: Boolean): TipSelectionContext {
        val settings = settingsService()
        val availableCategories = tipRepository().getCategories().values
        val enabledCategories = when {
            availableCategories.isEmpty() -> emptyList()
            settings == null -> availableCategories
            else -> settings.getEnabledTipCategories(availableCategories)
        }

        return TipSelectionContext(
            availableCategories = availableCategories,
            enabledCategories = enabledCategories,
            hiddenTipHashes = settings?.getHiddenTipHashes()?.toSet() ?: emptySet(),
            showAdvancedTips = settings?.isShowAdvancedTipsEnabled() ?: false,
            includeConfigTips = includeConfigTips,
        )
    }

    private fun tipRepository(): VimTipRepository = injectedTipRepository ?: service()

    // No settings service (e.g. an unconfigured cache outside a project) means we cannot know the
    // user's opt-in, so we fall back to the safe defaults: hide advanced tips, hide nothing else.
    private fun settingsService(): SettingsRepository? {
        injectedSettingsService?.let { return it }
        return runCatching { service<SettingsRepository>() }.getOrNull()
    }

    private companion object {
        val FALLBACK_TIP = VimTip(
            summary = "No tips found.",
            details = listOf("Tips have not been loaded yet.")
        )
        val FILTERED_FALLBACK_TIP = VimTip(
            summary = "No tips match the selected categories.",
            details = listOf(
                "Enable a matching category, or turn on \"Show advanced tips\", in Vim Coach settings."
            )
        )
    }
}
