package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.source.infra.config.VimTipConfig
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.VimTipNotifier
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PeriodicTipSchedulerServiceImpl(
    private val project: Project
) : PeriodicTipSchedulerService, Disposable {

    private val lock = Any()
    private var scheduledTask: ScheduledFuture<*>? = null

    override fun start() {
        logger.info("Starting periodic tip scheduler for project '${project.name}'")
        reschedule()
    }

    override fun onSettingsChanged() {
        logger.info("Periodic tip scheduler settings changed for project '${project.name}'")
        reschedule()
    }

    private fun reschedule() {
        synchronized(lock) {
            scheduledTask?.cancel(false)
            scheduledTask = null

            val scheduleConfig = resolveScheduleConfigOrNull()
            if (scheduleConfig == null) {
                logger.info("Periodic tip scheduler disabled for project '${project.name}'")
                return
            }

            logger.info(
                "Periodic tip scheduler armed for project '${project.name}' with interval=${scheduleConfig.intervalValue} ${scheduleConfig.intervalLabel}"
            )
            scheduledTask = scheduleNextRun(scheduleConfig.intervalSeconds)
        }
    }

    private fun scheduleNextRun(intervalSeconds: Long): ScheduledFuture<*> {
        logger.info("Periodic tip scheduler waiting ${intervalSeconds}s for project '${project.name}'")
        return AppExecutorUtil.getAppScheduledExecutorService().schedule(
            {
                runCatchingSchedulerTick()
                if (isSchedulingEnabled()) {
                    reschedule()
                }
            },
            intervalSeconds,
            TimeUnit.SECONDS
        )
    }

    private fun runCatchingSchedulerTick() {
        try {
            showPeriodicTipIfEnabled()
        } catch (t: Throwable) {
            logger.warn("Periodic tip scheduler tick failed for project '${project.name}'", t)
        }
    }

    override fun dispose() {
        synchronized(lock) {
            scheduledTask?.cancel(false)
            scheduledTask = null
        }
    }

    private fun showPeriodicTipIfEnabled() {
        if (!isSchedulingEnabled()) {
            return
        }

        if (project.isDisposed) {
            logger.info("Skipping periodic tip because project '${project.name}' is disposed")
            return
        }

        val notifier = VimTipNotifier(tipService())
        ApplicationManager.getApplication().invokeLater {
            try {
                if (!project.isDisposed && isSchedulingEnabled()) {
                    logger.info("Showing periodic Vim tip for project '${project.name}'")
                    notifier.showRandomTipIfNoneActive(project)
                }
            } catch (t: Throwable) {
                logger.warn("Failed to show periodic Vim tip for project '${project.name}'", t)
            }
        }
    }

    private fun isSchedulingEnabled(): Boolean {
        return resolveScheduleConfigOrNull() != null
    }

    private fun resolveScheduleConfigOrNull(): ScheduleConfig? {
        if (project.isDisposed) {
            return null
        }
        val settingsService = settingsService()
        if (!settingsService.isPeriodicTipsEnabled()) {
            return null
        }
        val intervalValue = settingsService.getTipIntervalHours().toLong().coerceIn(0L, MAX_INTERVAL_HOURS)
        val intervalSeconds = intervalValue * intervalUnitSeconds()
        if (intervalSeconds <= 0L) {
            return null
        }
        return ScheduleConfig(
            intervalValue = intervalValue,
            intervalSeconds = intervalSeconds,
            intervalLabel = intervalUnitLabel()
        )
    }

    private fun intervalUnitSeconds(): Long {
        return when (System.getProperty(VimTipConfig.TIP_INTERVAL_UNIT_PROPERTY)?.lowercase()) {
            "minutes" -> SECONDS_PER_MINUTE
            else -> SECONDS_PER_HOUR
        }
    }

    private fun intervalUnitLabel(): String {
        return when (System.getProperty(VimTipConfig.TIP_INTERVAL_UNIT_PROPERTY)?.lowercase()) {
            "minutes" -> "minute(s)"
            else -> "hour(s)"
        }
    }

    private fun settingsService(): VimCoachSettingsService = service()

    private fun tipService(): VimTipService = service()

    private data class ScheduleConfig(
        val intervalValue: Long,
        val intervalSeconds: Long,
        val intervalLabel: String
    )

    private companion object {
        val logger = Logger.getInstance(PeriodicTipSchedulerServiceImpl::class.java)
        const val SECONDS_PER_MINUTE = 60L
        const val SECONDS_PER_HOUR = 60L * 60L
        const val MAX_INTERVAL_HOURS = 24L * 7L
    }
}
