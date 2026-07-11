package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.intellij.openapi.components.service

class VimTipRepositoryImpl() : VimTipRepository {
    private var injectedTipStore: PersistentVimTipStore? = null
    private var injectedSettingsService: SettingsRepository? = null
    private var cachedTipSelection: TipSelectionIndex? = null
    private val tipSelector = TipSelector()

    internal constructor(tipStore: PersistentVimTipStore) : this() {
        injectedTipStore = tipStore
    }

    internal constructor(tipStore: PersistentVimTipStore, settingsService: SettingsRepository) : this(tipStore) {
        injectedSettingsService = settingsService
    }

    override fun countTips(): Int {
        return currentState().tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        saveTipCache(tips)
    }

    override fun getRandomTip(includeConfigTips: Boolean): VimTip {
        return randomTipOrFallback(currentState().tips, FALLBACK_TIP, includeConfigTips)
    }

    override fun getRandomTip(categories: List<String>, includeConfigTips: Boolean): VimTip {
        val matchingTips = tipSelectionIndex(currentState().tips).matchingTips(categories)
        return randomTipOrFallback(matchingTips, FILTERED_FALLBACK_TIP, includeConfigTips)
    }

    override fun hasAdvancedTips(): Boolean {
        return currentState().tips.any { it.advanced }
    }

    override fun getTipsByHashes(hashes: List<String>): List<VimTip> {
        val requestedHashes = hashes
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .toList()
        if (requestedHashes.isEmpty()) {
            return emptyList()
        }

        val tipsByHash = currentState().tips.associateBy { TipHash.fromTip(it).value }
        return requestedHashes.mapNotNull(tipsByHash::get)
    }

    override fun getCategories(): TipCategories {
        val state = currentState()
        if (state.categories.isNotEmpty() || state.tips.isEmpty()) {
            return state.categories
        }

        return backfillCategories(state.tips)
    }

    override fun getMetadata(): TipMetadata {
        return currentState().metadata
    }

    override fun saveMetadata(metadata: TipMetadata) {
        tipStore().setMetadata(metadata)
    }

    private fun currentState(): PersistentVimTipStore.State {
        return tipStore().state
    }

    private fun saveTipCache(tips: List<VimTip>) {
        tipStore().setTipCache(
            tips = tips,
            categories = TipCategories.fromTips(tips)
        )
    }

    private fun backfillCategories(tips: List<VimTip>): TipCategories {
        val categories = TipCategories.fromTips(tips)
        tipStore().setTipCache(tips, categories)
        return categories
    }

    private fun tipSelectionIndex(tips: List<VimTip>): TipSelectionIndex {
        val existingIndex = cachedTipSelection
        if (existingIndex != null && existingIndex.isFor(tips)) {
            return existingIndex
        }

        return TipSelectionIndex.fromTips(tips).also { cachedTipSelection = it }
    }

    private fun randomTipOrFallback(tips: List<VimTip>, fallbackTip: VimTip, includeConfigTips: Boolean): VimTip {
        return tipSelector.select(tips, visibilityCriteria(includeConfigTips)) ?: fallbackTip
    }

    private fun visibilityCriteria(includeConfigTips: Boolean): TipVisibilityCriteria {
        val settingsService = settingsServiceOrNull()
        return TipVisibilityCriteria(
            hiddenTipHashes = settingsService?.getHiddenTipHashes()?.toSet() ?: emptySet(),
            includeConfigTips = includeConfigTips,
            // No settings service (e.g. an unconfigured cache) means we cannot know the opt-in, so we
            // default to hiding advanced tips — the safe, spec-mandated default.
            showAdvancedTips = settingsService?.isShowAdvancedTipsEnabled() ?: false
        )
    }

    private fun tipStore(): PersistentVimTipStore {
        return injectedTipStore ?: service()
    }

    private fun settingsServiceOrNull(): SettingsRepository? {
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
