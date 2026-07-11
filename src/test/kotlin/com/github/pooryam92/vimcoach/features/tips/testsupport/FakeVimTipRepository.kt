package com.github.pooryam92.vimcoach.features.tips.testsupport

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository

class FakeVimTipRepository(
    initialTips: List<VimTip> = listOf(DEFAULT_TIP),
    initialMetadata: TipMetadata = TipMetadata(),
) : VimTipRepository {
    private var tips = initialTips.toMutableList()
    private var categories = TipCategories.fromTips(initialTips)
    private var metadata = initialMetadata

    var getTipsCalls = 0
        private set

    override fun countTips(): Int {
        return tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        this.tips = tips.toMutableList()
        categories = TipCategories.fromTips(tips)
    }

    override fun getTips(): List<VimTip> {
        getTipsCalls += 1
        return tips.toList()
    }

    override fun hasAdvancedTips(): Boolean {
        return tips.any { it.advanced }
    }

    override fun getTipsByHashes(hashes: List<String>): List<VimTip> {
        val tipsByHash = tips.associateBy { TipHash.fromTip(it).value }
        return hashes.mapNotNull(tipsByHash::get)
    }

    override fun getCategories(): TipCategories {
        return categories
    }

    override fun getMetadata(): TipMetadata {
        return metadata
    }

    override fun saveMetadata(metadata: TipMetadata) {
        this.metadata = metadata
    }

    private companion object {
        val DEFAULT_TIP = VimTip(
            summary = "fallback",
            details = listOf("fallback-details")
        )
    }
}
