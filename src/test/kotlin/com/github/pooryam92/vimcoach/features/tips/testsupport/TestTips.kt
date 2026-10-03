package com.github.pooryam92.vimcoach.features.tips.testsupport

import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

/**
 * A [VimTip] with an id, as the generator would emit. The default id follows the summary and
 * details, so editing either yields a new tip, like the real id.
 */
fun vimTip(
    summary: String = "",
    details: List<String> = emptyList(),
    category: List<String> = emptyList(),
    config: TipConfig? = null,
    mnemonic: String? = null,
    advanced: Boolean = false,
    mode: String? = null,
    id: String = testTipId(summary, details),
): VimTip = VimTip(summary, details, category, config, mnemonic, advanced, mode, id)

private fun testTipId(summary: String, details: List<String>): String =
    (listOf(summary) + details).joinToString("|")
