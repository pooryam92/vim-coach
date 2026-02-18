package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "VimTipCache", storages = [Storage("vim-tip-cache.xml")])
class VimTipServiceImpl : VimTipService {
    private var state: VimTipService.State = VimTipService.State()

    override fun getState(): VimTipService.State = state

    override fun loadState(state: VimTipService.State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    override fun countTips(): Int {
        return state.tips.count()
    }

    override fun saveTips(tips: List<VimTip>) {
        state.tips = tips.toMutableList()
    }

    override fun getRandomTip(): VimTip {
        if (state.tips.isEmpty()) {
            return VimTip(
                "No tips found.",
                listOf("Tips have not been loaded yet.")
            )
        }
        return state.tips.random()
    }

    override fun getMetadata(): TipMetadata {
        return state.metadata
    }

    override fun saveMetadata(metadata: TipMetadata) {
        state.metadata = metadata
    }
}
