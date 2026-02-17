package com.github.pooryam92.vimcoach.services

interface TipLoaderService {
    fun refetchTips(): TipLoadResult
    fun checkForUpdates(): TipLoadResult
}
