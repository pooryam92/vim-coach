package com.github.pooryam92.vimcoach.features.tips.state.store

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "VimTipCache", storages = [Storage("vim-tip-cache.xml")])
class VimTipStoreImpl : VimTipStore {
    private var state = VimTipStore.State()

    override fun getState(): VimTipStore.State = state

    override fun loadState(state: VimTipStore.State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }
}
