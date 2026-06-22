package com.github.pooryam92.vimcoach.features.tips.testsupport

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository

class FakeVimTipRepository(
    initialTips: List<VimTip> = listOf(DEFAULT_TIP),
    initialMetadata: TipMetadata = TipMetadata()
) : VimTipRepository {
    private var tips = initialTips.toMutableList()
    private var categories = TipCategories.fromTips(initialTips)
    private var metadata = initialMetadata
    private var currentIndex = 0

    var getRandomTipCalls = 0
        private set
    var getRandomTipByCategoryCalls = 0
        private set
    var lastRequestedCategories: List<String>? = null
        private set
    var lastIncludeConfigTips: Boolean? = null
        private set

    override fun countTips(): Int {
        return tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        this.tips = tips.toMutableList()
        categories = TipCategories.fromTips(tips)
        currentIndex = 0
    }

    override fun getRandomTip(includeConfigTips: Boolean): VimTip {
        getRandomTipCalls += 1
        lastRequestedCategories = null
        lastIncludeConfigTips = includeConfigTips
        val tipPool = configFiltered(tips, includeConfigTips).ifEmpty { listOf(DEFAULT_TIP) }
        val tip = tipPool[currentIndex % tipPool.size]
        currentIndex += 1
        return tip
    }

    override fun getRandomTip(categories: List<String>, includeConfigTips: Boolean): VimTip {
        getRandomTipByCategoryCalls += 1
        lastRequestedCategories = categories.toList()
        lastIncludeConfigTips = includeConfigTips

        val allowedCategories = categories.toSet()
        val tipPool = configFiltered(tips.filter { tip ->
            tip.category.any(allowedCategories::contains)
        }, includeConfigTips).ifEmpty { listOf(FILTERED_DEFAULT_TIP) }

        val tip = tipPool[currentIndex % tipPool.size]
        currentIndex += 1
        return tip
    }

    private fun configFiltered(tips: List<VimTip>, includeConfigTips: Boolean): List<VimTip> =
        tips.filter { includeConfigTips || it.config?.lines.isNullOrEmpty() }

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
        val FILTERED_DEFAULT_TIP = VimTip(
            summary = "filtered-fallback",
            details = listOf("filtered-fallback-details")
        )
    }
}
