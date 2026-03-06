package com.github.pooryam92.vimcoach.features.tips.testsupport

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService

class FakeVimTipService(
    initialTips: List<VimTip> = listOf(DEFAULT_TIP),
    initialMetadata: TipMetadata = TipMetadata()
) : VimTipService {
    private var tips = initialTips.toMutableList()
    private var metadata = initialMetadata
    private var currentIndex = 0

    var getRandomTipCalls = 0
        private set

    override fun countTips(): Int {
        return tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        this.tips = tips.toMutableList()
        currentIndex = 0
    }

    override fun getRandomTip(): VimTip {
        getRandomTipCalls += 1
        val tipPool = if (tips.isEmpty()) listOf(DEFAULT_TIP) else tips
        val tip = tipPool[currentIndex % tipPool.size]
        currentIndex += 1
        return tip
    }

    override fun getMetadata(): TipMetadata {
        return metadata
    }

    override fun saveMetadata(metadata: TipMetadata) {
        this.metadata = metadata
    }

    override fun getState(): VimTipService.State {
        return VimTipService.State(
            tips = tips.toMutableList(),
            metadata = metadata
        )
    }

    override fun loadState(state: VimTipService.State) {
        tips = state.tips.toMutableList()
        metadata = state.metadata
        currentIndex = 0
    }

    private companion object {
        val DEFAULT_TIP = VimTip(
            summary = "fallback",
            details = listOf("fallback-details")
        )
    }
}
