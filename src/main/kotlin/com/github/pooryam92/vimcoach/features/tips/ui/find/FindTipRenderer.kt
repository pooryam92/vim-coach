package com.github.pooryam92.vimcoach.features.tips.ui.find

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.application.search.FindTipEntry
import com.github.pooryam92.vimcoach.features.tips.domain.TipMode
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.popup.list.SelectablePanel
import com.intellij.ui.render.RenderingUtil
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

/**
 * Two-line Find Tip row: the summary with a dimmed label tail on the right, then the first detail
 * line, dimmed. Plain text only — Vim keys are deliberately not highlighted.
 */
internal class FindTipRenderer : ListCellRenderer<FindTipEntry> {

    internal val summary = rowText()
    internal val labels = rowText()
    internal val detail = rowText()

    private val topLine = JPanel(BorderLayout(JBUI.scale(LABEL_GAP), 0)).apply {
        isOpaque = false
        add(summary, BorderLayout.CENTER)
        add(labels, BorderLayout.EAST)
    }

    private val content = JPanel(BorderLayout()).apply {
        isOpaque = false
        add(topLine, BorderLayout.NORTH)
        add(detail, BorderLayout.CENTER)
    }

    // SelectablePanel paints the rounded, inset selection the platform's own popups use.
    private val row = SelectablePanel.wrap(content).apply {
        PopupUtil.configListRendererFlexibleHeight(this)
    }

    override fun getListCellRendererComponent(
        list: JList<out FindTipEntry>,
        value: FindTipEntry,
        index: Int,
        selected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        row.background = RenderingUtil.getBackground(list, false)
        row.selectionColor = if (selected) RenderingUtil.getBackground(list, true) else null
        val foreground = RenderingUtil.getForeground(list, selected)
        val labelTail = labelTail(value)
        val firstDetail = value.tip.details.firstOrNull().orEmpty()

        summary.replaceText(value.tip.summary, SimpleTextAttributes.REGULAR_ATTRIBUTES, foreground)
        labels.replaceText(labelTail, SimpleTextAttributes.GRAYED_ATTRIBUTES, foreground)
        detail.replaceText(firstDetail, SimpleTextAttributes.GRAYED_ATTRIBUTES, foreground)
        listOf(summary, labels, detail).forEach {
            SpeedSearchUtil.applySpeedSearchHighlighting(list, it, true, selected)
        }

        row.accessibleContext.accessibleName = listOf(value.tip.summary, labelTail, firstDetail)
            .filter(String::isNotBlank)
            .joinToString(" ")
        return row
    }

    private fun SimpleColoredComponent.replaceText(text: String, attributes: SimpleTextAttributes, foreground: Color) {
        clear()
        this.foreground = foreground
        if (text.isNotEmpty()) append(text, attributes)
    }

    private fun rowText() = SimpleColoredComponent().apply { isOpaque = false }

    private fun labelTail(entry: FindTipEntry): String {
        val tip = entry.tip
        return buildList {
            if (tip.advanced) add(TipNotificationFactory.ADVANCED_LABEL)
            TipMode.fromWire(tip.mode)?.let { add(it.label) }
            if (entry.muted) add(MUTED_LABEL)
            tip.category.filter(String::isNotBlank).takeIf { it.isNotEmpty() }?.let { add(it.joinToString(", ")) }
        }.joinToString(LABEL_SEPARATOR)
    }

    private companion object {
        const val LABEL_SEPARATOR = " · "
        const val LABEL_GAP = 12
        val MUTED_LABEL: String = MyBundle.message("findTipMutedLabel")
    }
}
