# tips-maintain evolution backlog

Working notes from a `/tips-evolve` Phase 1 review (session: adding IDE-bridge
`:action` mapping tips). One file per finding so each can be picked up cold in a
separate conversation. Apply via the `tips-evolve` skill's Phase 2 (show
before → after, edit, re-validate). Integrate — don't just append.

Target files live under `.claude/skills/tips-maintain/`:
- `SKILL.md` — the lean body, auto-loaded.
- `config-kinds.md` — rules for `config` blocks (the "Add to .ideavimrc" button).
- `checking-support.md` — how to prove IdeaVim supports a claim.

## Findings

- [important-mode-ambiguous-key-in-summary.md](important-mode-ambiguous-key-in-summary.md) — **IMPORTANT.** Mode-ambiguous keys (`O`, `Ctrl-w`) must name the mode in the summary (`SKILL.md` Wording rules).
- [important-category-is-not-one-file.md](important-category-is-not-one-file.md) — **IMPORTANT.** A category review must include cross-listed tips, not just `<category>.json` (`SKILL.md` "Search before adding").
- [important-summary-key-density.md](important-summary-key-density.md) — **IMPORTANT.** A summary carries at most one key/pair; 3+ keys or a symbol cluster move to details (`SKILL.md` summary field rule).
- [minor-split-by-intent.md](minor-split-by-intent.md) — **MINOR.** Split a multi-key tip by intent, not key count; keep same-intent direction pairs together (`SKILL.md` Wording rules).
- [minor-base-command-tip-missing.md](minor-base-command-tip-missing.md) — **MINOR.** A command taught only through its flags needs a foundational tip (`SKILL.md` candidate wells).
- [minor-theory-in-one-tryable-tip.md](minor-theory-in-one-tryable-tip.md) — **MINOR.** Theory earns at most one tip and must stay tryable; else delete-and-fold into a concrete host (`SKILL.md` opening philosophy).
- [candidate-c-verify-not-default.md](candidate-c-verify-not-default.md) — **MINOR.** Verify a `set` option isn't already the default.
- [minor-keys-vs-typed-text.md](minor-keys-vs-typed-text.md) — **MINOR.** Distinguish keys pressed from text typed in worked examples (`examples.md`).
- [minor-plugin-vs-builtin-summary.md](minor-plugin-vs-builtin-summary.md) — **MINOR (borderline).** Plugin-vs-built-in tips should state the difference in the summary (`SKILL.md` Wording rules).
- [did-not-qualify.md](did-not-qualify.md) — recorded for context; no action.
