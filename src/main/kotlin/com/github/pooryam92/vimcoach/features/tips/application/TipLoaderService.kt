package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult

interface TipLoaderService {
    fun refetchTips(): TipLoadResult
    fun checkForUpdates(): TipLoadResult
}
