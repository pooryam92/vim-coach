package com.github.pooryam92.vimcoach.features.tips.application.loading.infra.config

object VimTipConfig {
    const val TIPS_FILE_PATH_PROPERTY = "vimcoach.tip.file.path"
    const val TIP_INTERVAL_UNIT_PROPERTY = "vimcoach.tip.interval.unit"
    const val TIP_REMOTE_URL_PROPERTY = "vimcoach.tip.remote.url"
    const val GITHUB_API_URL = "https://api.github.com/repos/pooryam92/vim-coach/contents/tips/vim_tips_min.json"

    /**
     * The remote tip source URL. Defaults to [GITHUB_API_URL]; beta testers can point the
     * plugin at an alternative source by setting the [TIP_REMOTE_URL_PROPERTY] JVM option
     * (Help | Edit Custom VM Options) and restarting the IDE.
     */
    fun resolveRemoteUrl(): String =
        System.getProperty(TIP_REMOTE_URL_PROPERTY)?.trim()?.takeIf { it.isNotEmpty() }
            ?: GITHUB_API_URL
}
