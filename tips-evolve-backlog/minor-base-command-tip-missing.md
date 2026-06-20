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
