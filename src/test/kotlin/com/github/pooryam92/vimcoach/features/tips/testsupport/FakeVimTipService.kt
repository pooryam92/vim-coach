package com.github.pooryam92.vimcoach.features.tips.testsupport

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService

class FakeVimTipService(
    initialTips: List<VimTip> = listOf(DEFAULT_TIP),
    initialMetadata: TipMetadata = TipMetadata()
) : VimTipService {
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

    override fun countTips(): Int {
        return tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        this.tips = tips.toMutableList()
        categories = TipCategories.fromTips(tips)
        currentIndex = 0
    }

    override fun getRandomTip(): VimTip {
        getRandomTipCalls += 1
        lastRequestedCategories = null
        val tipPool = if (tips.isEmpty()) listOf(DEFAULT_TIP) else tips
        val tip = tipPool[currentIndex % tipPool.size]
        currentIndex += 1
        return tip
    }

    override fun getRandomTip(categories: List<String>): VimTip {
        getRandomTipByCategoryCalls += 1
        lastRequestedCategories = categories.toList()

        val allowedCategories = categories.toSet()
        val tipPool = tips.filter { tip ->
            tip.category.any(allowedCategories::contains)
        }.ifEmpty { listOf(FILTERED_DEFAULT_TIP) }

        val tip = tipPool[currentIndex % tipPool.size]
        currentIndex += 1
        return tip
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
