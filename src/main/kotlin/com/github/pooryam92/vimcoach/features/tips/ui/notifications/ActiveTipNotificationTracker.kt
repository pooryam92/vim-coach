package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.intellij.notification.Notification
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

internal class ActiveTipNotificationTracker(
    private val project: Project
) {
    private val lock = Any()

    var current: Notification? = null
        private set

    fun hasVisibleNotification(): Boolean {
        synchronized(lock) {
            val existingNotification = current ?: return false
            val visibility = trackedNotificationVisibility(existingNotification)
            if (visibility != NotificationVisibility.VISIBLE) {
                clearCurrent(visibility.staleReason)
                return false
            }
            return true
        }
    }

    fun replaceWith(notification: Notification) {
        synchronized(lock) {
            expireCurrentLocked()
            current = notification
        }
        notification.whenExpired {
            synchronized(lock) {
                if (current === notification) {
                    current = null
                }
            }
        }
    }

    fun expire(notification: Notification) {
        synchronized(lock) {
            if (current === notification) {
                current = null
            }
            notification.expire()
        }
    }

    private fun expireCurrentLocked() {
        current?.let { notification ->
            current = null
            notification.expire()
        }
    }

    private fun clearCurrent(reason: String? = null) {
        current = null
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

    private enum class NotificationVisibility(val staleReason: String?) {
        VISIBLE(null),
        EXPIRED(null),
        NO_BALLOON("it has no visible balloon"),
        DISPOSED_BALLOON("its balloon is disposed")
    }

    private companion object {
        val logger = Logger.getInstance(ActiveTipNotificationTracker::class.java)
    }
}
