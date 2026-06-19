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

    override fun getRandomTip(): VimTip {
        return randomTipOrFallback(currentState().tips, FALLBACK_TIP)
    }

    override fun getRandomTip(categories: List<String>): VimTip {
        val matchingTips = tipSelectionIndex(currentState().tips).matchingTips(categories)
        return randomTipOrFallback(matchingTips, FILTERED_FALLBACK_TIP)
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

    private fun randomTipOrFallback(tips: List<VimTip>, fallbackTip: VimTip): VimTip {
        val visibleTips = visibleTips(tips)
        if (visibleTips.isEmpty()) {
            return fallbackTip
        }
        return visibleTips.random()
    }

    private fun visibleTips(tips: List<VimTip>): List<VimTip> {
        val hiddenHashes = hiddenTipHashes()
        return tips
            .asSequence()
            .filterNot { TipHash.fromTip(it).value in hiddenHashes }
            .toList()
    }

    private fun hiddenTipHashes(): Set<String> {
        val settingsService = settingsServiceOrNull()
            ?: return emptySet()
        return settingsService.getHiddenTipHashes().toSet()
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
            details = listOf("Enable at least one matching category in Vim Coach settings.")
        )
    }
}
