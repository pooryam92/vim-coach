package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationActions
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.github.pooryam92.vimcoach.features.tips.ui.settings.VimCoachSettingsConfigurable
import com.intellij.notification.Notification
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project

class TipNotificationController(
    private val project: Project
) : ShowTips {

    private var injectedNotificationFactory: TipNotificationFactory = TipNotificationFactory()
    private var injectedSettingsService: VimCoachSettingsService? = null
    private var injectedTipService: VimTipService? = null
    private val activeTipNotifications = ActiveTipNotificationTracker(project)

    constructor(
        project: Project,
        tipService: VimTipService,
        notificationFactory: TipNotificationFactory,
        settingsService: VimCoachSettingsService? = null
    ) : this(project) {
        injectedNotificationFactory = notificationFactory
        injectedSettingsService = settingsService
        injectedTipService = tipService
    }

    internal val activeNotification: Notification?
        get() = activeTipNotifications.current

    override fun showRandomTip() {
        showTip(selectRandomTip())
    }

    override fun showRandomTipIfNoneActive(): Boolean {
        if (activeTipNotifications.hasVisibleNotification()) {
            return false
        }
        showTip(selectRandomTip())
        return true
    }

    private fun showTip(tip: VimTip) {
        val notification = createNotificationWithActions(tip)
        activeTipNotifications.replaceWith(notification)
        notification.notify(project)
    }

    private fun createNotificationWithActions(tip: VimTip): Notification {
        return injectedNotificationFactory.createNotificationWithActions(tip, notificationActions(tip))
    }

    private fun notificationActions(tip: VimTip): TipNotificationActions {
        return TipNotificationActions(
            onShowNextTip = ::showRandomTip,
            onExcludeTip = { notification -> excludeTipFromFutureNotifications(tip, notification) }
        )
    }

    private fun excludeTipFromFutureNotifications(tip: VimTip, notification: Notification) {
        val result = ExcludeTipFromNotifications(settingsService()).exclude(tip)
        activeTipNotifications.expire(notification)
        if (result.shouldShowManagementHint) {
            injectedNotificationFactory
                .createTipExcludedNotification(::openSettings)
                .notify(project)
        }
    }

    private fun openSettings() {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, VimCoachSettingsConfigurable::class.java)
    }

    private fun selectRandomTip(): VimTip {
        val availableCategories = tipService().getCategories().values
        if (availableCategories.isEmpty()) {
            return tipService().getRandomTip()
        }

        val enabledCategories = settingsService().getEnabledTipCategories(availableCategories)
        return tipService().getRandomTip(enabledCategories)
    }

    private fun settingsService(): VimCoachSettingsService = injectedSettingsService ?: service()

    private fun tipService(): VimTipService = injectedTipService ?: service()

}
