package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

interface ShowTips {
    fun showRandomTip()
    fun showRandomTipIfNoneActive(): Boolean

    /** Shows [tip] as-is, bypassing tip selection and its no-repeat rotation. */
    fun showTip(tip: VimTip)
}
