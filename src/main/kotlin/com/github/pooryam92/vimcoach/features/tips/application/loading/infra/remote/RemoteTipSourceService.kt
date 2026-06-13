package com.github.pooryam92.vimcoach.features.tips.application.loading.infra.remote

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.TipSourceLoadResult

interface RemoteTipSourceService {
    fun loadTips(): TipSourceLoadResult
    fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult
}
