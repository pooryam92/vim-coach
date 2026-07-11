package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.intellij.openapi.components.service

class VimTipRepositoryImpl() : VimTipRepository {
    private var injectedTipStore: PersistentVimTipStore? = null

    internal constructor(tipStore: PersistentVimTipStore) : this() {
        injectedTipStore = tipStore
    }

    override fun countTips(): Int {
        return currentState().tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        saveTipCache(tips)
    }

    override fun getTips(): List<VimTip> {
        return currentState().tips
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

    private fun tipStore(): PersistentVimTipStore {
        return injectedTipStore ?: service()
    }
}
