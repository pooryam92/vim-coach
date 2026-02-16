package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.services.VimTip
import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel

internal class TipDetailsDialog(project: Project, tip: VimTip) : DialogWrapper(project) {
    private val summaryText = tip.summary
    private val detailsText = tip.details

    init {
        title = MyBundle.message("tipDetailsDialogTitle")
        setResizable(true)
        setOKButtonText(CommonBundle.getCloseButtonText())
        init()
    }

    override fun createActions(): Array<Action> = arrayOf(okAction)

    override fun createCenterPanel(): JComponent {
        val contentLineCount = estimateVisualLineCount(summaryText, 54) + estimateVisualLineCount(detailsText, 58)
        val preferredHeight = when {
            contentLineCount <= 8 -> 170
            contentLineCount <= 16 -> 230
            else -> 290
        }
        val summaryArea = JBTextArea(summaryText).apply {
            isEditable = false
            isOpaque = false
            isFocusable = false
            lineWrap = true
            wrapStyleWord = true
            border = JBUI.Borders.emptyBottom(8)
            font = UIUtil.getLabelFont().deriveFont(Font.BOLD)
            foreground = UIUtil.getLabelForeground()
        }
        val detailsArea = JBTextArea(detailsText).apply {
            isEditable = false
            isOpaque = false
            isFocusable = false
            lineWrap = true
            wrapStyleWord = true
            border = JBUI.Borders.empty()
            foreground = UIUtil.getLabelForeground()
            font = UIUtil.getLabelFont()
            caretPosition = 0
        }
        val scrollPane = JBScrollPane(detailsArea).apply {
            preferredSize = Dimension(470, preferredHeight)
            border = JBUI.Borders.empty()
            viewport.background = UIUtil.getPanelBackground()
        }
        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
            background = UIUtil.getPanelBackground()
            add(summaryArea, BorderLayout.NORTH)
            add(scrollPane, BorderLayout.CENTER)
        }
    }

    private fun estimateVisualLineCount(text: String, wrapWidthChars: Int): Int {
        return text.lineSequence()
            .map { line ->
                val length = line.length.coerceAtLeast(1)
                ((length - 1) / wrapWidthChars) + 1
            }
            .sum()
            .coerceAtLeast(1)
    }
}
