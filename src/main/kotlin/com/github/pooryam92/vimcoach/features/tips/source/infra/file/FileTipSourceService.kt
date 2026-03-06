package com.github.pooryam92.vimcoach.features.tips.source.infra.file

import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult

interface FileTipSourceService {
    fun loadTips(): TipSourceLoadResult
}
