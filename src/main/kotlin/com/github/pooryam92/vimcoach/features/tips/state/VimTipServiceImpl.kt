package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStore
import com.intellij.openapi.components.service

class VimTipServiceImpl(
    private val tipStore: VimTipStore = service()
) : VimTipService {

    override fun countTips(): Int {
        return currentState().tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        tipStore.setTips(tips)
    }

    override fun getRandomTip(): VimTip {
        val state = currentState()
        if (state.tips.isEmpty()) {
            return FALLBACK_TIP
        }
        return state.tips.random()
    }

    override fun getMetadata(): TipMetadata {
        return currentState().metadata
    }

    override fun saveMetadata(metadata: TipMetadata) {
        tipStore.setMetadata(metadata)
    }

    private fun currentState(): VimTipStore.State {
        return tipStore.state ?: VimTipStore.State()
    }

    private companion object {
        val FALLBACK_TIP = VimTip(
            summary = "No tips found.",
            details = listOf("Tips have not been loaded yet.")
        )
    }
}
