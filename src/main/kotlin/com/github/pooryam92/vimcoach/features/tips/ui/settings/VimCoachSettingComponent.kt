package com.github.pooryam92.vimcoach.features.tips.ui.settings

import javax.swing.JComponent

interface VimCoachSettingComponent {
    fun component(): JComponent
    fun isModified(): Boolean
    fun apply()
    fun reset()
}
