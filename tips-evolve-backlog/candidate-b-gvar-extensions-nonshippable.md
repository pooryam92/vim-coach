# Candidate B — extensions that bind no keys without a `g:` var are non-shippable

**Priority:** GOOD. Durable IdeaVim quirk.

**Target file:** `.claude/skills/tips-maintain/config-kinds.md`.

---

## Learning

Some bundled IdeaVim extensions create *no* key mappings unless a `g:` variable is
set, so a `Plug`-only config button would silently do nothing — the same failure
mode as the already-deferred plugins (multiple-cursors, EasyMotion, which-key).

## Evidence

- CamelCaseMotion:
  `external/ideavim/src/main/java/com/maddyhome/idea/vim/extension/camelcasemotion/CamelCaseMotion.kt`
  guards every mapping with `if (key != null)` where
  `key = getVariable("g:camelcasemotion_key")`. Only the `<Plug>` targets exist by
  default; no `w`/`b`/`e`-style keys until the user sets the var.
- We dropped CamelCaseMotion as a candidate this session for exactly this reason.

## Why it qualifies

Durable IdeaVim quirk that generalizes beyond this one plugin. The skill's
deferred-plugin note covers "needs a separate Marketplace IDE plugin" but not the
"needs a `g:` var to bind keys" variant.

## How it should land

Fold into the existing paragraph in `config-kinds.md` that begins *"Skip plugins
that need a separate Marketplace IDE plugin…"*. Broaden it to also cover:

> …or that bind no keys until a `g:` var is set (e.g. CamelCaseMotion needs
> `g:camelcasemotion_key`) — a `Plug`-only button would silently no-op.

One clause, no new section.
