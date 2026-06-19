# Config-tip Roadmap (discover)

Forward-looking work on `.ideavimrc` config tips. The **shippable, additive**
config kinds are already documented in the `tips-maintain` skill
([../../.claude/skills/tips-maintain/SKILL.md](../../.claude/skills/tips-maintain/SKILL.md)
→ "The three additive config kinds") — install a
plugin, tune built-in behavior, IDE-bridge options. This file tracks the parts
that are **not** shippable yet: positional/opinionated config and the code work
that gates it.

Status: the 14-category migration is **done** (the locked taxonomy lives in
the `tips-maintain` skill
([../../.claude/skills/tips-maintain/SKILL.md](../../.claude/skills/tips-maintain/SKILL.md)
→ "Categories")). The additive config set is
**documented and shippable today**. Everything below is still open.

---

## Positional / opinionated config — needs code first

These config kinds claim keys or set state others depend on; appending them
blindly causes collisions, so they need key-aware dedup and a leader decision
before they ship.

- **`<Action>` maps** — `nmap gr <Action>(FindUsages)`. 0 today.
- **Reshape the keyboard** — `inoremap jj <Esc>`, `nmap Y y$`. 0 today;
  collisions.
- **Foundations** — `let mapleader=" "`, `sethandler`. 0 today; these *gate* the
  two above by changing the meaning of other config.

---

## Roadmap — ship additive first

The additive buckets (A/B below) deliver ~25 config-backed tips with zero code or
taxonomy changes and are covered by the README. The remaining steps are the open
work:

1. **Bucket A** *(additive, shippable)* — keep expanding `plugins` with verified
   `Plug '<alias>'` entries from the official IdeaVim plugin list.
2. **Bucket B** *(additive, shippable)* — add a `config` line to the ~12 existing
   option tips. Highest value per effort; exact-match-safe, no leader, no
   collisions.
3. Provenance markers in `IdeaVimRcAppendPlan` + tests.
4. Lock config conventions in the tip-authoring docs.
5. Decide the `<leader>` convention — gates everything positional.
6. Phase 2 key-aware dedup in `IdeaVimRcAppendPlan` — gates positional config.
7. **Bucket C** — `<Action>` maps, non-leader first.
8. Keyboard + foundations families last.

---

## Deferred enhancement: friendly category labels

*(needs code)* A label/description layer — a `tips/categories.json` manifest read
by `VimCoachSettingsConfigurable.kt` so the settings checkbox shows "Command
line" instead of `cmdline`. Separate from tip selection; not blocking anything
above.

---

## Coupling points (keep in sync)

`tips/categories/<name>.json` (filename == a tip's **first** category) ·
the `tips-maintain` skill
([../../.claude/skills/tips-maintain/SKILL.md](../../.claude/skills/tips-maintain/SKILL.md))
Categories table + notes ·
[../tips/tips-pipeline.md](../tips/tips-pipeline.md). After any change:
`node scripts/generate-tips.mjs` must pass.
