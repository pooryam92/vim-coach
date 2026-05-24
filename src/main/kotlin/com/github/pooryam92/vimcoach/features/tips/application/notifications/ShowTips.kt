package com.github.pooryam92.vimcoach.features.tips.application.notifications

interface ShowTips {
    fun showRandomTip()
    fun showRandomTipIfNoneActive(): Boolean
}
