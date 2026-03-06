package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "VimTipCache", storages = [Storage("vim-tip-cache.xml")])
class VimTipServiceImpl : VimTipService {
    private var state = VimTipService.State()

    override fun getState(): VimTipService.State = state

    override fun loadState(state: VimTipService.State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    override fun countTips(): Int {
        return state.tips.size
    }

    override fun saveTips(tips: List<VimTip>) {
        state.tips = tips.toMutableList()
    }

    override fun getRandomTip(): VimTip {
        if (state.tips.isEmpty()) {
            return FALLBACK_TIP
        }
        return state.tips.random()
    }

    override fun getMetadata(): TipMetadata {
        return state.metadata
    }

    override fun saveMetadata(metadata: TipMetadata) {
        state.metadata = metadata
    }

    private companion object {
        val FALLBACK_TIP = VimTip(
            summary = "No tips found.",
            details = listOf("Tips have not been loaded yet.")
        )
    }
}
