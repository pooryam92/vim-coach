package com.github.pooryam92.vimcoach.features.tips.application

interface VimCoachSettingsScreenService {
    fun loadState(): VimCoachSettingsScreenState
    fun saveState(state: VimCoachSettingsScreenState)
}
