# Discovery: Vim-mode Indicator (badge on tip body)

Proposal from a tip-legibility brainstorm (2026-07-05).
Status: **brainstormed shape, not started**. Corpus at time of analysis: 307
tips, 14 categories.

## Problem

Tips don't say which vim mode they apply in. `category` is the only axis
today, and it only *sometimes* doubles as mode — `insert`/`visual`/`cmdline`
are mode-flavored, but `navigation`/`registers`/`repeat`/`editing`/etc. carry
no mode signal at all. A user reading "Recall last search with `Ctrl-r /`"
has no way to tell, without trying it, whether that's an Insert-mode or
Command-line-mode trick.

## Possible solution

Give each tip a mode signal, separate from `category`, shown as a small
badge using vim's own mode letters (`n`/`i`/`v`/`c` — already the audience's
mental model, no invented icon needed) next to the tip content, not in the
notification title (title stays reserved for the `◆` advanced marker).

**Multiple modes**: if a tip applies to one or two modes, show them (e.g.
`[N/V]`). If it applies to three or more — i.e. essentially everywhere —
showing a badge stops narrowing anything down for the reader, so skip the
badge entirely in that case rather than trying to enumerate "all modes."

**Tagging effort**: 307 tips is too many to hand-tag at once. Tips already
filed under `insert`/`visual`/`cmdline` categories (54 total) map to a mode
for free; the rest would need a real pass, done a handful at a time rather
than all up front — same incremental approach used for `advanced` tagging.

Also considered and set aside: a separate icon for tips with a
`.ideavimrc` config action — the existing "Add to .ideavimrc" button
already signals that, so a second symbol doesn't earn its keep yet.

This is orthogonal to the parked
[category restructure](category-restructure.md) (that's topic labels on
settings checkboxes; this is a per-tip mode signal on the balloon) — the two
can land independently.
</content>
