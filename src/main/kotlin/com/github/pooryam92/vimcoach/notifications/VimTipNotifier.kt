package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class VimTipNotifier(
    private val tipService: VimTipService
) {

    fun showRandomTip(project: Project) {
        showTip(project, tipService.getRandomTip(), false)
    }

    private fun showTip(project: Project, tip: VimTip, expanded: Boolean) {
        val notification = createNotification(project, tip, expanded)
        notification.notify(project)
    }

    private fun createNotification(project: Project, tip: VimTip, expanded: Boolean): Notification {
        val modeBadge = formatModeBadge(tip.mode)
        val categoryLabel = tip.category?.takeIf { it.isNotBlank() }?.let { "Category: ${toTitleCase(it)}" }
        val metaLine = listOfNotNull(modeBadge, categoryLabel).takeIf { it.isNotEmpty() }?.joinToString(" | ")
        val detailsHtml = if (expanded) tip.details.replace("\n", "<br/>") else null
        val content = buildString {
            append("<html>")
            if (metaLine != null) {
                append("<span style='color:#6a6a6a;'>")
                append(metaLine)
                append("</span>")
            } else {
                append("&nbsp;")
            }
            if (detailsHtml != null) {
                append("<br/>")
                append("<span>")
                append(detailsHtml)
                append("</span>")
            }
            append("</html>")
        }
        val notification = Notification(
            NOTIFICATION_GROUP_ID,
            tip.summary,
            content,
            NotificationType.INFORMATION
        )
        notification.addAction(NotificationAction.createSimple(MyBundle.message(TIP_NEXT_ACTION_KEY)) {
            notification.expire()
            showTip(project, tipService.getRandomTip(), false)
        })
        if (!expanded) {
            notification.addAction(NotificationAction.createSimple(MyBundle.message(TIP_MORE_ACTION_KEY)) {
                notification.expire()
                showTip(project, tip, true)
            })
        }
        return notification
    }

    private fun formatModeBadge(mode: String?): String? {
        val normalized = mode?.trim()?.ifBlank { null }?.lowercase() ?: return null
        val label = toTitleCase(normalized)
        val short = when (normalized) {
            "normal" -> "N"
            "insert" -> "I"
            "visual" -> "V"
            "visual line", "visual-line" -> "VL"
            "visual block", "visual-block" -> "VB"
            "replace" -> "R"
            "command", "command line", "command-line", "cmd" -> "C"
            else -> normalized.take(1).uppercase()
        }
        return "<b>[$short]</b> $label"
    }

    private fun toTitleCase(value: String): String {
        return value
            .replace('_', ' ')
            .replace('-', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    companion object {
        const val NOTIFICATION_GROUP_ID = "Vim Tips"
        private const val TIP_NEXT_ACTION_KEY = "tipNextAction"
        private const val TIP_MORE_ACTION_KEY = "tipMoreAction"
    }
}
