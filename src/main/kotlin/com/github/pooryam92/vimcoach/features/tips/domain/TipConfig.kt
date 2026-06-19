package com.github.pooryam92.vimcoach.features.tips.domain

/**
 * A tip's optional configuration snippet.
 *
 * [lines] are written verbatim into .ideavimrc (order and any repeats preserved). [name] is an
 * optional human label for the apply affordance — e.g. a plugin name shown as "Add vim-surround";
 * when null the generic apply label is used. The name/lines split lets a tip present a friendly
 * action without parsing it back out of the config text, and is not limited to plugins.
 */
data class TipConfig(
    var name: String? = null,
    var lines: List<String> = emptyList()
)
