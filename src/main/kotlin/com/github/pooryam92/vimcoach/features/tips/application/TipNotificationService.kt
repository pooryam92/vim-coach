package com.github.pooryam92.vimcoach.features.tips.application

interface TipNotificationService {
    fun showRandomTip()
    fun showRandomTipIfNoneActive(): Boolean
}
