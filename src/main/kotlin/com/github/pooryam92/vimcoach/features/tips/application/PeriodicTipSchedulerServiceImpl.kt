package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.source.infra.config.VimTipConfig
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.VimTipNotifier
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PeriodicTipSchedulerServiceImpl(
    private val project: Project,
    private val cs: CoroutineScope
) : PeriodicTipSchedulerService {

    private val lock = Any()
    private var schedulerJob: Job? = null

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
            schedulerJob?.cancel()
            schedulerJob = null

            val intervalMs = intervalMsOrNull()
            if (intervalMs == null) {
                logger.info("Periodic tip scheduler disabled for project '${project.name}'")
                return
            }

            logger.info(
                "Periodic tip scheduler armed for project '${project.name}' with interval=${settingsService().getTipIntervalHours()} ${intervalUnitLabel()}"
            )
            schedulerJob = scheduleLoop(intervalMs)
        }
    }

    private fun scheduleLoop(intervalMs: Long): Job {
        return cs.launch {
            while (isActive) {
                logger.info("Periodic tip scheduler waiting ${intervalMs}ms for project '${project.name}'")
                delay(intervalMs)
                if (!isActive) {
                    return@launch
                }

                runCatchingSchedulerTick()
            }
        }
    }

    private fun runCatchingSchedulerTick() {
        try {
            showPeriodicTipIfEnabled()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            logger.warn("Periodic tip scheduler tick failed for project '${project.name}'", t)
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
                    notifier.showRandomTip(project)
                }
            } catch (t: Throwable) {
                logger.warn("Failed to show periodic Vim tip for project '${project.name}'", t)
            }
        }
    }

    private fun isSchedulingEnabled(): Boolean {
        return !project.isDisposed && settingsService().isPeriodicTipsEnabled() && intervalMs() > 0L
    }

    private fun intervalMsOrNull(): Long? {
        if (!isSchedulingEnabled()) {
            return null
        }
        return intervalMs()
    }

    private fun intervalMs(): Long {
        val hours = settingsService().getTipIntervalHours().toLong().coerceIn(0L, MAX_INTERVAL_HOURS)
        return hours * intervalUnitMs()
    }

    private fun intervalUnitMs(): Long {
        return when (System.getProperty(VimTipConfig.TIP_INTERVAL_UNIT_PROPERTY)?.lowercase()) {
            "minutes" -> MILLIS_PER_MINUTE
            else -> MILLIS_PER_HOUR
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

    private companion object {
        val logger = Logger.getInstance(PeriodicTipSchedulerServiceImpl::class.java)
        const val MILLIS_PER_MINUTE = 60L * 1000L
        const val MILLIS_PER_HOUR = 60L * 60L * 1000L
        const val MAX_INTERVAL_HOURS = 24L * 7L
    }
}
