# Vim Tips Build Pipeline

How the tip sources become the published file, and how CI keeps the two in sync.
For writing and reviewing tip **content**, see the `tips-maintain` skill
([../../.claude/skills/tips-maintain/SKILL.md](../../.claude/skills/tips-maintain/SKILL.md)).

## Sources and the generated file

- **Authoring sources:** `tips/categories/*.json` — one file per primary
  category, each a JSON object with a `tips` array. These are the only files you
  edit by hand.
- **Generated file:** `tips/vim_tips_min.json` — the single minified file the
  plugin actually publishes and consumes.

> **Never edit `tips/vim_tips_min.json` by hand.** It is a generated artifact,
> always produced from `tips/categories/` by `scripts/generate-tips.mjs`. Any
> hand edits are overwritten the next time the generator runs. `.gitattributes`
> marks the file as generated so GitHub collapses its diffs.

## The generator

`scripts/generate-tips.mjs` is a plain Node script with no dependencies. It reads
every `tips/categories/*.json` file, validates the tips, and writes
`tips/vim_tips_min.json`.

Day to day you only need to **validate** the sources — CI regenerates and commits
the published file (see below), so you should not regenerate it yourself unless
asked:

```bash
node scripts/generate-tips.mjs --check   # validate sources, write nothing
node scripts/generate-tips.mjs           # rebuild the artifact (CI / on request)
```

For the soft, advisory quality checks the generator does not enforce
(over-length lines, stray separators, legacy-array config, possible duplicate
keys), run `node scripts/lint-tips.mjs`. It never gates — it just prints a report
to eyeball.

### Ordering

Tips are emitted grouped by category, with categories sorted **alphabetically**
by name. There is no curated order to maintain: tip selection is random at
runtime, so this ordering only affects the order categories list in the settings
UI. Adding a new category file therefore needs no ordering change — it slots into
the alphabetical sequence automatically.

### Validation

The generator **fails with a non-zero exit code** (and writes nothing) if any
tip:

- has a blank `summary`
- has no `details` lines
- does not use its file's category name as its first `category` entry
- repeats a `summary` already used by another tip, in any file

The error message names the offending file and tip so you can fix it quickly.

### Normalization

For each tip the generator trims surrounding whitespace, drops blank `details`
lines, and removes duplicate `details` lines (preserving order). An optional
`mnemonic` string is trimmed and emitted only when non-blank (dropped otherwise).
The optional `advanced` flag is emitted only when `true` (kept off the artifact
otherwise, so it stays minimal); a non-boolean `advanced` value fails generation.
The optional `mode` field is emitted only when set and must be one of `insert`,
`visual`, or `command` — any other value fails generation (absent means Normal,
which is never stored or labelled). The output is compact JSON with non-ASCII
characters escaped as `\uXXXX`, so the published file is deterministic and stays
plain ASCII.

### Schema evolution and the `advanced` field

The published schema grows **additively only** — new fields are optional and
defaulted; existing fields are never renamed or removed. Tip parsing is
**lenient**: unknown fields are ignored, so a newer published file never breaks
an older plugin. Keep it that way — tightening the parser would break the
forward compatibility every installed version relies on.

The optional `advanced` flag rides this schema. The plugin models and reads it
(advanced tips are hidden unless the user opts in; see
[Advanced Tips Opt-In](../features/settings.md#advanced-tips-opt-in)) while
keeping the leniency guarantee: `TipJsonParser` ignores a non-boolean
`advanced` value (the tip parses as not advanced) instead of failing the whole
file, which matters for hand-authored file-mode and custom-URL sources. The
generator carries the flag through: it emits `advanced` only when `true` and
`--check` rejects any non-boolean value, so a flag authored in
`tips/categories/` reaches `vim_tips_min.json` instead of being silently dropped
by the field whitelist. Author it via the `tips-maintain` skill, which documents
the field and the tagging guidance.

The optional `mode` field rides the same schema the same way. It names the mode
the reader must be in to press the tip's keys — `insert`, `visual`, or `command`
(absent = Normal, never labelled) — and renders as a dimmed label after the app
name in the tip balloon title (see
[Show a tip](../features/show-tip.md#advanced-tips-marker-and-nudge)). The generator validates
the value strictly (`--check` rejects anything outside the enum), while
`TipJsonParser` is lenient: an unknown or malformed `mode` (a value a future
schema adds, or a non-string) is dropped and the tip renders with no mode label
instead of failing the file — the same forward-compatibility the `advanced` field
relies on. `mode` is informational only: unlike `advanced` it is not an opt-in
setting and does not affect which tips are shown, hidden, or de-duplicated.

## CI: the Generate Tips workflow

`.github/workflows/generate-tips.yml` keeps the published file in sync
automatically. It runs on pushes to `main` and on pull requests, but only when
one of these changes:

- `tips/categories/**`
- `scripts/generate-tips.mjs`
- the workflow file itself

The job checks out the branch, runs `node scripts/generate-tips.mjs`, and — if
the regenerated `tips/vim_tips_min.json` differs from what is committed — commits
the result back to the branch with `[skip ci]`.

Because of this, you never have to keep the generated file in sync by hand: edit
the category sources, and CI regenerates and commits the published file. Hand
edits to `tips/vim_tips_min.json` are simply overwritten on the next run.

## How the published file is consumed

- **At runtime**, the plugin fetches `tips/vim_tips_min.json` from GitHub
  (`VimTipConfig.GITHUB_API_URL` points at
  `repos/pooryam92/vim-coach/contents/tips/vim_tips_min.json`). The committed
  generated file is therefore exactly what users receive, which is why CI must
  keep it current.
- **For local IDE runs**, the `runIdeWithFileTips` Gradle task launches the IDE
  with `-Dvimcoach.tip.source=file` pointed at the local
  `tips/vim_tips_min.json`. Run `node scripts/generate-tips.mjs` first if you
  have edited the category sources, since this task does not regenerate it.

## Flagging a tip that needs fixing (dev only)

While a tip balloon is open during a dev IDE run, a **"Note…"** action appears
alongside the other actions. Clicking it opens a text box; whatever you type is
appended to `docs/tips/tip-feedback.md` — an append-only markdown log stamped
with a timestamp, the tip's summary, and its `TipHash`. That file is
`.gitignore`d and is where the maintainer (or an agent) later picks up which
tips to revise.

This is wired only for the `runIdeWithFileTips` and `runIdeWithMinuteTipSchedule`
Gradle tasks, which set
`-Dvimcoach.tip.notes.file=<repo>/docs/tips/tip-feedback.md`. The action is
gated entirely on that system property: a released build never sets it, so the
"Note…" action never appears and nothing is written in production.
