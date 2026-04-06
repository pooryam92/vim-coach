package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.intellij.notification.Notification
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

class TipNotificationServiceImpl(
    private val project: Project
) : TipNotificationService {

    private var injectedNotificationFactory: TipNotificationFactory = TipNotificationFactory()
    private var injectedSettingsService: VimCoachSettingsService? = null
    private var injectedTipService: VimTipService? = null

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

    private val lock = Any()

    internal var activeNotification: Notification? = null
        private set

    override fun showRandomTip() {
        showTip(selectRandomTip())
    }

    override fun showRandomTipIfNoneActive(): Boolean {
        if (hasActiveTipNotification()) {
            return false
        }
        showTip(selectRandomTip())
        return true
    }

    private fun showTip(tip: VimTip) {
        val notification = createNotificationWithActions(tip)
        expireActiveTipNotification()
        registerActiveTipNotification(notification)
        notification.notify(project)
    }

    private fun createNotificationWithActions(tip: VimTip): Notification {
        return injectedNotificationFactory.createNotificationWithActions(tip) {
            showRandomTip()
        }
    }

    private fun hasActiveTipNotification(): Boolean {
        synchronized(lock) {
            val existingNotification = activeNotification ?: return false
            val visibility = trackedNotificationVisibility(existingNotification)
            if (visibility != NotificationVisibility.VISIBLE) {
                clearActiveNotification(visibility.staleReason)
                return false
            }
            return true
        }
    }

    private fun expireActiveTipNotification() {
        synchronized(lock) {
            activeNotification?.let { notification ->
                activeNotification = null
                notification.expire()
            }
        }
    }

    private fun registerActiveTipNotification(notification: Notification) {
        synchronized(lock) {
            activeNotification = notification
        }
        notification.whenExpired {
            synchronized(lock) {
                if (activeNotification === notification) {
                    activeNotification = null
                }
            }
        }
    }

    private fun clearActiveNotification(reason: String? = null) {
        activeNotification = null
        if (reason != null) {
            logger.info("Cleared stale Vim tip notification for project '${project.name}' because $reason")
        }
    }

    private fun trackedNotificationVisibility(notification: Notification): NotificationVisibility {
        if (notification.isExpired) {
            return NotificationVisibility.EXPIRED
        }
        val balloon = notification.balloon ?: return NotificationVisibility.NO_BALLOON
        if (balloon.isDisposed) {
            return NotificationVisibility.DISPOSED_BALLOON
        }
        return NotificationVisibility.VISIBLE
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

    private enum class NotificationVisibility(val staleReason: String?) {
        VISIBLE(null),
        EXPIRED(null),
        NO_BALLOON("it has no visible balloon"),
        DISPOSED_BALLOON("its balloon is disposed")
    }

    private companion object {
        val logger = Logger.getInstance(TipNotificationServiceImpl::class.java)
    }
}
