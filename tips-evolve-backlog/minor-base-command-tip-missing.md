# A command taught only through its flags/sub-features needs a foundational tip

**Target file:** `.claude/skills/tips-maintain/SKILL.md` ("Finding the best tips to add — or cut", the candidate-wells paragraph)
**Priority:** MINOR (one session instance; partly covered by the standalone rule)

## Learning / problem

A cluster of advanced tips on one command can all silently lean on a base concept
that has no tip of its own. When improving such a tip, check the base command is
taught somewhere; if not, that gap is a high-reach tip to add.

## Evidence

User: "where is the normal :s tip?" → grep showed all 29 `pattern` tips taught
`:s` flags/sub-features (`gc`, `&`, `\1`, `:v`, ranges) but **no foundational
`:%s/foo/bar/g` "replace all" tip existed**. Added `Replace every match with
:%s` (`tips/categories/pattern.json`). This also recurred implicitly: multiple
tips this session needed reworking to teach `:s` inline ("what is :s?",
"not clear on its own") because the base wasn't assumable.

## Why it qualifies

Generalizable coverage check (applies to `:g`, macros `q`, registers, etc.), but
narrower than A/B — it's partly an instance of the existing standalone-in-
knowledge rule plus coverage mapping. Borderline; parked rather than applied.

## How it should land

*Fold one clause into the "candidate wells" list* in *Finding the best tips to
add* — do not add a section.

Sketch (add to the wells list):
> … a **base command taught only through its flags/sub-features** with no
> foundational tip (e.g. `:s` variations but no plain `:%s/old/new/g`) — add the
> base; it usually out-reaches every variation built on it.

*Why:* turns a discovered gap into a standing prompt during coverage reviews,
without restating the standalone rule. If a Phase 2 review finds the standalone
bullet already implies this, skip — don't duplicate.
