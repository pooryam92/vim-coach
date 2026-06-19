package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.TipIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.ui.settings.VimCoachSettingsConfigurable
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project

class TipNotifications internal constructor(
    private val notifier: TipNotifier,
    private val tipRepository: () -> VimTipRepository,
    private val settingsRepository: () -> SettingsRepository,
    private val ideaVimRcAction: (VimTip) -> (() -> Unit)?,
    private val openSettings: () -> Unit,
) : ShowTips {

    constructor(project: Project) : this(
        notifier = project.service(),
        tipRepository = { service() },
        settingsRepository = { service() },
        ideaVimRcAction = TipIdeaVimRc(project, project.service())::getAction,
        openSettings = {
            ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, VimCoachSettingsConfigurable::class.java)
        },
    )

    override fun showRandomTip() = showTip(selectRandomTip())

    override fun showRandomTipIfNoneActive(): Boolean {
        if (notifier.hasVisibleTip()) return false
        showTip(selectRandomTip())
        return true
    }

    private fun showTip(tip: VimTip) {
        notifier.showTip(
            tip,
            TipActions(
                onShowNextTip = ::showRandomTip,
                onExcludeTip = { excludeTip(tip) },
                onAddToIdeaVimRc = ideaVimRcAction(tip),
            )
        )
    }

    private fun excludeTip(tip: VimTip) {
        val result = ExcludeTipFromNotifications(settingsRepository()).exclude(tip)
        if (result.shouldShowManagementHint) {
            notifier.showTipExcluded(openSettings)
        }
    }

    private fun selectRandomTip(): VimTip {
        val tips = tipRepository()
        val availableCategories = tips.getCategories().values
        if (availableCategories.isEmpty()) return tips.getRandomTip()
        val enabledCategories = settingsRepository().getEnabledTipCategories(availableCategories)
        return tips.getRandomTip(enabledCategories)
    }
}
