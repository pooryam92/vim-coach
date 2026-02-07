package com.github.pooryam92.plugin.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.State
import com.intellij.util.xmlb.XmlSerializerUtil


@Service(Service.Level.PROJECT)
@State(name = "VimTipCache", storages = [Storage("vim-tip-cache.xml")])
class VimTips : PersistentStateComponent<VimTips.State> {

    private var state: State = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    fun countTips(): Int {
        return state.tips.count()
    }

    fun getTips(): List<VimTip> {
        if (state.tips.isEmpty())
        {
            return listOf(VimTip("No tips found.", "Tips have not been loaded yet.", null))
        }
        return state.tips
    }

    fun saveTips(tips: List<VimTip>) {
        state.tips = tips.toMutableList()
    }

    data class State(
        var tips: MutableList<VimTip> = mutableListOf()
    )
}
