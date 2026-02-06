package com.github.pooryam92.plugin.tips.loading

import com.github.pooryam92.plugin.config.VimTipConfig
import com.github.pooryam92.plugin.services.VimTipService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class TipLoader(project: Project) {
    private val cacheSource = project.service<CacheTipSource>()
    private val remoteSource = project.service<RemoteTipSource>()
    private val localSource = project.service<LocalTipSource>()

    fun loadTips(loadRemote: Boolean = true): List<VimTipService.VimTip> {
        val cached = cacheSource.loadTips()
        if (!cached.isNullOrEmpty()) {
            return cached
        }

        if (loadRemote) {
            val remote = remoteSource.loadTips()
            if (!remote.isNullOrEmpty()) {
                cacheSource.saveTips(remote)
                return remote
            }
        }

        val local = localSource.loadTips().orEmpty()
        if (local.isNotEmpty()) {
            cacheSource.saveTips(local)
            return local
        }

        return listOf(
            VimTipService.VimTip(
                "No tips found.",
                "No tips found in ${VimTipConfig.LOCAL_RESOURCE_PATH}.",
                null
            )
        )
    }
}
