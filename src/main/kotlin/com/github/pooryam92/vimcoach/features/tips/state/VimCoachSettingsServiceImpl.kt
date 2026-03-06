package com.github.pooryam92.vimcoach.features.tips.state

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "VimCoachSettings", storages = [Storage("vim-coach-settings.xml")])
class VimCoachSettingsServiceImpl : VimCoachSettingsService {
    private var state = VimCoachSettingsService.State()

    override fun getState(): VimCoachSettingsService.State = state

    override fun loadState(state: VimCoachSettingsService.State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    override fun isShowTipsOnStartupEnabled(): Boolean {
        return state.showTipsOnStartup
    }

    override fun setShowTipsOnStartupEnabled(enabled: Boolean) {
        state.showTipsOnStartup = enabled
    }
}
