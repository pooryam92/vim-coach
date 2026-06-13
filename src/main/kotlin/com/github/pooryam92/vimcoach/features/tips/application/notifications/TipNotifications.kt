package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.TipIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationActions
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.github.pooryam92.vimcoach.features.tips.ui.settings.VimCoachSettingsConfigurable
import com.intellij.notification.Notification
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project

class TipNotifications(
    private val project: Project
) : ShowTips {

    private var notificationFactory: TipNotificationFactory = TipNotificationFactory()
    private var injectedSettingsService: SettingsRepository? = null
    private var injectedTipService: VimTipRepository? = null
    private var ideaVimRcHandler: TipIdeaVimRc = TipIdeaVimRc(project, notificationFactory)
    private val activeTipNotifications = ActiveTipNotificationTracker(project)

    constructor(
        project: Project,
        tipService: VimTipRepository,
        notificationFactory: TipNotificationFactory,
        settingsService: SettingsRepository? = null,
        addTipToIdeaVimRc: AddTipToIdeaVimRc = AddTipToIdeaVimRc(project),
        reloadIdeaVimRc: (() -> Unit)? = null
    ) : this(project) {
        this.notificationFactory = notificationFactory
        this.injectedSettingsService = settingsService
        this.injectedTipService = tipService
        this.ideaVimRcHandler = TipIdeaVimRc(project, notificationFactory, addTipToIdeaVimRc, reloadIdeaVimRc)
    }

    internal val activeNotification: Notification?
        get() = activeTipNotifications.current

    override fun showRandomTip() {
        showTip(selectRandomTip())
    }

    override fun showRandomTipIfNoneActive(): Boolean {
        if (activeTipNotifications.hasVisibleNotification()) return false
        showTip(selectRandomTip())
        return true
    }

    private fun showTip(tip: VimTip) {
        val notification = notificationFactory.createNotificationWithActions(tip, notificationActions(tip))
        activeTipNotifications.replaceWith(notification)
        notification.notify(project)
    }

    private fun notificationActions(tip: VimTip): TipNotificationActions {
        return TipNotificationActions(
            onShowNextTip = ::showRandomTip,
            onExcludeTip = { notification -> excludeTipFromFutureNotifications(tip, notification) },
            onAddToIdeaVimRc = ideaVimRcHandler.getAction(tip)
        )
    }

    private fun excludeTipFromFutureNotifications(tip: VimTip, notification: Notification) {
        val result = ExcludeTipFromNotifications(settingsService()).exclude(tip)
        activeTipNotifications.expire(notification)
        if (result.shouldShowManagementHint) {
            notificationFactory
                .createTipExcludedNotification(::openSettings)
                .notify(project)
        }
    }

    private fun openSettings() {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, VimCoachSettingsConfigurable::class.java)
    }

    private fun selectRandomTip(): VimTip {
        val availableCategories = tipService().getCategories().values
        if (availableCategories.isEmpty()) return tipService().getRandomTip()
        val enabledCategories = settingsService().getEnabledTipCategories(availableCategories)
        return tipService().getRandomTip(enabledCategories)
    }

    private fun settingsService(): SettingsRepository = injectedSettingsService ?: service()
    private fun tipService(): VimTipRepository = injectedTipService ?: service()
}
