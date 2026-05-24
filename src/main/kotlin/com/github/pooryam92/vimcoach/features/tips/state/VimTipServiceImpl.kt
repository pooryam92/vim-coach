package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStore
import com.intellij.openapi.components.service

class VimTipServiceImpl() : VimTipService {
    private var injectedTipStore: VimTipStore? = null
    private var injectedSettingsService: VimCoachSettingsService? = null
    private var cachedTipSelection: TipSelectionIndex? = null

    internal constructor(tipStore: VimTipStore) : this() {
        injectedTipStore = tipStore
    }

    internal constructor(tipStore: VimTipStore, settingsService: VimCoachSettingsService) : this(tipStore) {
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

    private fun currentState(): VimTipStore.State {
        return tipStore().state ?: VimTipStore.State()
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

    private fun tipStore(): VimTipStore {
        return injectedTipStore ?: service()
    }

    private fun settingsServiceOrNull(): VimCoachSettingsService? {
        injectedSettingsService?.let { return it }
        return runCatching { service<VimCoachSettingsService>() }.getOrNull()
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
