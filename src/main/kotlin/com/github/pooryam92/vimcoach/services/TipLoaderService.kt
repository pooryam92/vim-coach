package com.github.pooryam92.vimcoach.services

interface TipLoaderService {
    fun loadTips(): TipLoadResult
    fun refetchTips(): TipLoadResult
}
