package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.PersistentStateComponent

interface VimTipService : PersistentStateComponent<VimTipService.State> {
    fun countTips(): Int
    fun saveTips(tips: List<VimTip>)
    fun getRandomTip(): VimTip

    data class State(
        var tips: MutableList<VimTip> = mutableListOf()
    )
}
