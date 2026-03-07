package com.github.pooryam92.vimcoach.features.tips.state.store

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "VimCoachSettings", storages = [Storage("vim-coach-settings.xml")])
class VimCoachSettingsStoreImpl : VimCoachSettingsStore {
    private var state = VimCoachSettingsStore.State()

    override fun getState(): VimCoachSettingsStore.State = state

    override fun loadState(state: VimCoachSettingsStore.State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }
}
