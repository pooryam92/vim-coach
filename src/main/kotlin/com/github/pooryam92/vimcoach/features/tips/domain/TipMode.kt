package com.github.pooryam92.vimcoach.features.tips.domain

/**
 * The mode a reader must be in to press a tip's keys, i.e. the tip's primary teaching mode.
 * Normal is the untagged default and has no entry here — a tip with no mode carries no label.
 *
 * [wireValue] is the canonical value stored in tip JSON (`VimTip.mode`); [label] is how it renders
 * in the notification title.
 */
enum class TipMode(val wireValue: String, val label: String) {
    INSERT("insert", "Insert mode"),
    VISUAL("visual", "Visual mode"),
    COMMAND("command", "Command mode");

    companion object {
        /** Resolves a stored wire value to a mode, or null if it is blank/unknown (lenient by design). */
        fun fromWire(value: String?): TipMode? {
            val trimmed = value?.trim() ?: return null
            return entries.firstOrNull { it.wireValue == trimmed }
        }
    }
}
