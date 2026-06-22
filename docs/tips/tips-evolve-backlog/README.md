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

- [minor-split-by-intent.md](minor-split-by-intent.md) — **MINOR.** Split a multi-key tip by intent, not key count; keep same-intent direction pairs together (`SKILL.md` Wording rules).
- [minor-base-command-tip-missing.md](minor-base-command-tip-missing.md) — **MINOR.** A command taught only through its flags needs a foundational tip (`SKILL.md` candidate wells).
- [minor-theory-in-one-tryable-tip.md](minor-theory-in-one-tryable-tip.md) — **MINOR.** Theory earns at most one tip and must stay tryable; else delete-and-fold into a concrete host (`SKILL.md` opening philosophy).
- [candidate-c-verify-not-default.md](candidate-c-verify-not-default.md) — **MINOR.** Verify a `set` option isn't already the default.
- [minor-symbol-pair-separator.md](minor-symbol-pair-separator.md) — **MINOR.** Symbol/bracket key-pairs read better joined with `and` than `/` (`SKILL.md` Wording rules, "Consistent pair phrasing").
- [minor-coverage-plugin-false-positives.md](minor-coverage-plugin-false-positives.md) — **MINOR.** `coverage.mjs` flags plugins by internal id, not Plug repo name → recurring false-positive misses (`coverage.mjs` and/or `SKILL.md`).
- [minor-action-mapping-prefer-convention.md](minor-action-mapping-prefer-convention.md) — **MINOR.** IDE-bridge action mapping should prefer the conventional Vim/Neovim binding over a free slot, disclosing any shadow (`config-kinds.md`).
- [did-not-qualify.md](did-not-qualify.md) — recorded for context; no action.

Applied 2026-06-20 (folded into the **reader's-seat** principle, `SKILL.md`
opening + Wording rules, and `examples.md`): mode-ambiguous-key-in-summary,
summary-key-density, plugin-vs-builtin-summary, keys-vs-typed-text. Git history
preserves them.

Applied 2026-06-20 (new "A category is its rendered set, not its file" bullet in
`SKILL.md` *How you work*, before "Search before adding"):
important-category-is-not-one-file. Git history preserves it.
