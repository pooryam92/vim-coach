package com.github.pooryam92.vimcoach.features.tips.application.loading.infra.file

import com.github.pooryam92.vimcoach.features.tips.domain.TipSourceLoadResult

interface FileTipSourceService {
    fun loadTips(): TipSourceLoadResult
}
