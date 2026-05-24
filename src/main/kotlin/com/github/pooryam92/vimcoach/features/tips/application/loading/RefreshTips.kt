package com.github.pooryam92.vimcoach.features.tips.application.loading

import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult

interface RefreshTips {
    fun refetchTips(): TipLoadResult
    fun checkForUpdates(): TipLoadResult
}
