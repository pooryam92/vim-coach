package com.github.pooryam92.vimcoach.features.tips.ui.settings

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.application.settings.ExcludedTipSettingsItem
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

internal class ExcludedTipsListPanel : JBScrollPane() {
    private val rowsPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
    }
    private var tips = emptyList<ExcludedTipSettingsItem>()
    private var restoredTipHashes = emptyList<String>()

    init {
        viewport.view = rowsPanel
        border = JBUI.Borders.customLine(JBColor.border())
        preferredSize = Dimension(SCROLL_WIDTH, SCROLL_HEIGHT)
    }

    fun reset(tips: List<ExcludedTipSettingsItem>) {
        this.tips = tips
        restoredTipHashes = emptyList()
        render()
    }

    fun currentTips(): List<ExcludedTipSettingsItem> {
        return tips
    }

    fun restoredTipHashes(): List<String> {
        return restoredTipHashes
    }

    private fun render() {
        rowsPanel.removeAll()

        if (tips.isEmpty()) {
            rowsPanel.add(createEmptyLabel())
        } else {
            tips.forEach { rowsPanel.add(createTipRow(it)) }
        }

        rowsPanel.revalidate()
        rowsPanel.repaint()
    }

    private fun createEmptyLabel(): JLabel {
        return JLabel(MyBundle.message("settingsNoExcludedTips")).apply {
            foreground = JBColor.GRAY
            border = JBUI.Borders.empty(ROW_VERTICAL_PADDING, 0)
        }
    }

    private fun createTipRow(tip: ExcludedTipSettingsItem): JPanel {
        return JPanel(BorderLayout(ROW_H_GAP, 0)).apply {
            isOpaque = false
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                JBUI.Borders.empty(
                    ROW_VERTICAL_PADDING,
                    ROW_LEFT_PADDING,
                    ROW_VERTICAL_PADDING,
                    ROW_RIGHT_PADDING
                )
            )
            add(createSummaryLabel(tip.summary), BorderLayout.CENTER)
            add(createUndoButton(tip), BorderLayout.EAST)
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }
    }

    private fun createSummaryLabel(summary: String): JLabel {
        return JLabel(summary).apply {
            horizontalAlignment = SwingConstants.LEFT
        }
    }

    private fun createUndoButton(tip: ExcludedTipSettingsItem): JButton {
        return JButton(MyBundle.message("settingsUndoExcludedTip")).apply {
            addActionListener {
                tips = tips.filterNot { it.hash == tip.hash }
                restoredTipHashes = (restoredTipHashes + tip.hash).distinct()
                render()
            }
        }
    }

    private companion object {
        const val SCROLL_WIDTH = 420
        const val SCROLL_HEIGHT = 120
        const val ROW_H_GAP = 12
        const val ROW_VERTICAL_PADDING = 6
        const val ROW_LEFT_PADDING = 8
        const val ROW_RIGHT_PADDING = 4
    }
}
