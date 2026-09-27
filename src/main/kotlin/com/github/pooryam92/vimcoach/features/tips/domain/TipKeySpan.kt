package com.github.pooryam92.vimcoach.features.tips.domain

/**
 * A key the reader types, as the `[start, end)` range of one line of a tip's text: [line] 0 is the
 * summary and 1.. the details. Offsets are UTF-16 indices, which the tip generator (JavaScript) and
 * Kotlin strings share.
 */
data class TipKeySpan(
    var line: Int = 0,
    var start: Int = 0,
    var end: Int = 0
) {
    fun fitsIn(lines: List<String>): Boolean {
        return line in lines.indices && start in 0 until end && end <= lines[line].length
    }
}
