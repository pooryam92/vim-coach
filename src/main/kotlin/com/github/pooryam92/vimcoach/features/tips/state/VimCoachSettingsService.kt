package com.github.pooryam92.vimcoach.features.tips.state

interface VimCoachSettingsService {
    fun isShowTipsOnStartupEnabled(): Boolean
    fun setShowTipsOnStartupEnabled(enabled: Boolean)
}
