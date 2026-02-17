package com.github.pooryam92.vimcoach.services

import com.github.pooryam92.vimcoach.services.source.TipSourceLoadResult
import com.github.pooryam92.vimcoach.services.source.TipSourceService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class TipLoaderServiceImpl(project: Project) : TipLoaderService {
    private val tipService = project.service<VimTipService>()
    private val tipSource = project.service<TipSourceService>()

    override fun refetchTips(): TipLoadResult {
        return fetchAndSave(conditional = false)
    }

    override fun checkForUpdates(): TipLoadResult {
        if (tipService.countTips() == 0) {
            return fetchAndSave(conditional = false)
        }
        
        return fetchAndSave(conditional = true)
    }

    private fun fetchAndSave(conditional: Boolean): TipLoadResult {
        val sourceResult = if (conditional) {
            val metadata = tipService.getMetadata()
            tipSource.loadTipsConditional(metadata)
        } else {
            tipSource.loadTips()
        }

        return when (sourceResult) {
            is TipSourceLoadResult.Success -> {
                tipService.saveTips(sourceResult.tips)
                tipService.saveMetadata(sourceResult.metadata)
                TipLoadResult.Updated(sourceResult.tips.size)
            }
            
            TipSourceLoadResult.NotModified -> {
                val currentMetadata = tipService.getMetadata()
                val updatedMetadata = currentMetadata.copy(
                    lastFetchTimestamp = System.currentTimeMillis()
                )
                tipService.saveMetadata(updatedMetadata)
                TipLoadResult.NotModified
            }
            
            TipSourceLoadResult.Empty -> TipLoadResult.NoData
            
            is TipSourceLoadResult.Failure -> TipLoadResult.Failed(sourceResult.message, sourceResult.cause)
        }
    }
}
