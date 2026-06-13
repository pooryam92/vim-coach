# Discovery: IdeaVim Templates

## Feature Promise

Vim Coach helps users quickly discover, preview, and install curated `.ideavimrc` setups from inside the IDE.

---

## Core Problem

**Starting from zero is hard.**
When a user has no `.ideavimrc`, they have no foundation. They either copy from memory, search the web, or go without.

This feature meets them at that moment and gives them a fast path to a working setup.

The feature can also serve existing users who want to browse other configs for inspiration, but v1 should not try to solve merging, cherry-picking, or config improvement workflows.

---

## Evidence of Demand

Users already share `.ideavimrc` configs manually on GitHub discussions. The need exists, but the current experience is fragmented: users copy from a browser, cannot easily preview inside the IDE, and have no guided install flow.

- [Share your `~/.ideavimrc` - JetBrains/ideavim - Discussion #303](https://github.com/JetBrains/ideavim/discussions/303)

This feature brings discovery, preview, and installation into the tool where users actually work.

---

## Primary Audience

| Segment | Description | V1 Priority |
|---|---|---|
| New IdeaVim users | Need a practical starting point and may not know what is possible | Primary |
| Users on a fresh install | Know they want an `.ideavimrc`, but want to get set up quickly | Primary |
| Users switching from another editor | Want a setup that feels familiar enough to start from | Primary |
| Learners | Want to study templates for ideas before installing | Secondary |
| Existing users with a config | May want inspiration, but merging/cherry-picking is a later feature | Secondary |
| Team/org users | May want private shared templates | Out of scope for v1 |

---

## V1 Scope

### Entry Point

Vim Coach should have an always-visible status bar icon.

Clicking the icon opens a small popup menu with plugin actions:

- Browse IdeaVim Templates
- Show Tip
- Refresh Tips
- Settings

The status bar icon acts as a lightweight interaction hub for Vim Coach. Actions should still be registered normally so users can find them through IDE action search.

The settings page remains for preferences, not for primary feature workflows.

### Template Browser

The template browser should be a modal dialog, not a tool window or settings page.

The dialog supports:

- Template list
- Sorting by Most Downloaded
- Sorting by Newest
- Detail panel
- Author shown in the detail panel only
- Total downloads
- Version selector
- Version changelog
- Read-only preview
- Install action

No search, tags, levels, ratings, likes, or advanced filters in v1.

### Install Flow

The install flow is:

1. User previews a template version.
2. User clicks Install.
3. Vim Coach shows a confirmation dialog.
4. Vim Coach writes the selected version as the user's `.ideavimrc`.

If the user already has an `.ideavimrc`, Vim Coach should clearly say so, copy the existing file to a timestamped backup name, and then install the selected template.

Example backup name:

```text
~/.ideavimrc.vim-coach-backup-YYYY-MM-DD-HHMMSS
```

The confirmation should communicate both the selected template version and the backup behavior.

---

## Template Source

Templates should live in a separate public GitHub repository.

The repository accepts community submissions through pull requests. Submissions are curated, not automatically accepted just because they are valid.

V1 should not self-host templates or metrics. GitHub is enough for the first version and keeps the system visible and low-maintenance.

---

## Template Index

The template repository publishes a static `index.json` file at its root. The plugin fetches this single file directly via its raw GitHub URL. No GitHub API calls are made from the plugin.

### Format

```json
{
  "generatedAt": "2026-06-03T12:00:00Z",
  "templates": [
    {
      "id": "beginner-starter",
      "name": "Beginner Starter",
      "description": "A small, practical config for getting started with IdeaVim.",
      "author": "pooryam92",
      "totalDownloads": 1240,
      "latestVersionDate": "2026-05-20",
      "versions": [
        {
          "version": "v3",
          "date": "2026-05-20",
          "changes": "Added split navigation mappings.",
          "downloads": 480,
          "contentUrl": "https://github.com/org/repo/releases/download/beginner-starter-v3/beginner-starter-v3.ideavimrc"
        }
      ]
    }
  ]
}
```

Design decisions:

- `contentUrl` is explicit so the plugin does not need to know URL construction rules. If hosting changes, only the index needs updating.
- `totalDownloads` is pre-aggregated by the CI job. The plugin does not sum across versions.
- `latestVersionDate` is denormalized so the plugin can sort by Newest without scanning version arrays.
- Versions are ordered newest-first. The plugin uses the first entry as the default selection.

### Generation

A GitHub Actions workflow regenerates and commits `index.json` on two triggers:

- Every merge to `main`, to pick up new templates and versions immediately.
- On a weekly schedule, to refresh download counts from the GitHub release API.

The workflow uses `GITHUB_TOKEN` for the release API calls, which avoids the unauthenticated rate limit.

---

## Community Submission Model

Submissions should be lightweight.

Required for a new template:

```text
templates/<template-id>/
  metadata.json
  versions/
    v1.ideavimrc
    v1.json
```

Optional:

```text
templates/<template-id>/README.md
```

Required metadata should stay minimal:

```json
{
  "id": "beginner-starter",
  "name": "Beginner Starter",
  "description": "A small, practical config for getting started with IdeaVim.",
  "author": "pooryam92"
}
```

Version metadata:

```json
{
  "createdAt": "2026-06-03",
  "changes": "Initial version."
}
```

No tags or levels in v1.

---

## Curation

Template submissions should be reviewed before merge.

Review should check that a template:

- Is understandable enough for users to inspect before installing
- Does not depend on obscure local paths
- Does not shell out or perform surprising external behavior
- Is not unnecessarily large
- Does not duplicate an existing template without a clear reason
- Has accurate metadata and version notes

The plugin should communicate these as curated templates, without claiming that they are universally "good" or best.

---

## Versioning And Downloads

Downloads are an important success metric and ranking signal.

Popularity should mean actual template downloads, not likes, stars, or preview views.

Templates should use versioned release assets so download history does not reset when a template changes and older versions remain available.

Example assets:

```text
beginner-starter-v1.ideavimrc
beginner-starter-v2.ideavimrc
beginner-starter-v3.ideavimrc
```

The browser should sort by:

- Most Downloaded: total downloads across all versions of a template
- Newest: latest version date

Users should be able to install older versions from the version selector.

Each version should include short changelog text so users can understand why versions differ.

---

## Caching And Failure States

V1 should cache only the template index on disk.

Template file contents should be cached in memory for the duration of the dialog session. Once a template version is fetched for preview, subsequent views of that same version reuse the in-memory result without a network round-trip. This cache is discarded when the dialog closes and is never written to disk.

If fetching templates fails and a cached index exists, the dialog can show the cached list with a clear stale-data message.

If fetching templates fails and no cached index exists, the dialog should show:

```text
Could not fetch IdeaVim templates.
Check your connection and try again.
```

Include a Retry action.

No bundled fallback templates in v1.

---

## Explicitly Out Of Scope For V1

- Template likes or voting
- GitHub stars as a per-template metric
- Self-hosted backend or metrics service
- Search
- Tag filtering
- Template levels
- Template ratings
- Bundled fallback templates
- Private/team template repositories
- Merging with an existing `.ideavimrc`
- Cherry-picking snippets
- Editing a template before install
- Tool window or dashboard UI
- Automatic notifications prompting setup

---

## Success Metrics

- Number of community template submissions
- Number of accepted template submissions
- Total downloads per template
- Downloads by template version
- Use of the template browser entry point

---

## Next Phase

The next phase should split this discovery into implementation tasks and dig deeper into each area:

- Status bar widget and popup actions
- Template browser dialog
- Template repository format
- GitHub release asset publishing workflow
- Download aggregation model
- Remote index loading and caching
- Preview and install flow
- Backup behavior
- Tests and verification
