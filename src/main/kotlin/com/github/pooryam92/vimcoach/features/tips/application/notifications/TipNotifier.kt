package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

/**
 * Port for presenting VimCoach notifications
 **/

interface TipNotifier {
    /** Whether a tip notification is currently visible to the user. */
    fun hasVisibleTip(): Boolean

    /** Present a tip with its action callbacks, replacing any currently-visible tip. */
    fun showTip(tip: VimTip, actions: TipActions)

    /** Confirm a tip was excluded, offering a way to manage exclusions. */
    fun showTipExcluded(onManage: () -> Unit)

    /**
     * Report that config lines were appended to .ideavimrc. [onReload], when non-null, offers a
     * "reload now" affordance. Returns a handle so the caller can dismiss this message later
     * (e.g. once reload has run).
     */
    fun showAddedToIdeaVimRc(onReload: (() -> Unit)?): TipMessageHandle

    /** Report that the config was already present in .ideavimrc. */
    fun showAlreadyInIdeaVimRc()

    /**
     * Guide the user to create a .ideavimrc when IdeaVim is installed but no file exists yet.
     * Vim Coach does not create the file itself; the user creates it through IdeaVim, then the
     * apply action works on the next click.
     */
    fun showCreateIdeaVimRcGuidance()

    /** Report that appending to .ideavimrc failed, explaining [reason] to the user. */
    fun showAddToIdeaVimRcFailed(reason: AddTipToIdeaVimRc.FailureReason)

    /** Report that .ideavimrc was reloaded. */
    fun showReloadedIdeaVimRc()

    /** Report that reloading .ideavimrc failed. */
    fun showReloadIdeaVimRcFailed()
}

/** A handle to a shown message, allowing the caller to dismiss it. */
interface TipMessageHandle {
    fun dismiss()
}

/**
 * Callbacks wired into a tip notification's affordances. [onAddToIdeaVimRc] is null when the
 * tip has no config lines or IdeaVim is not installed.
 */
data class TipActions(
    val onShowNextTip: () -> Unit,
    val onExcludeTip: () -> Unit,
    val onAddToIdeaVimRc: (() -> Unit)?,
)
