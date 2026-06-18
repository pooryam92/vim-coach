# Beta release to JetBrains Marketplace

How to ship a **beta** build of the plugin to the JetBrains Marketplace beta
channel — including from a feature branch that is **not merged into `main`** yet.

## How channel routing works

The release channel is derived automatically from `pluginVersion` in
`gradle.properties`. See `build.gradle.kts`:

```kotlin
publishing {
    token = providers.environmentVariable("PUBLISH_TOKEN")
    channels = providers.gradleProperty("pluginVersion")
        .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
}
```

The pre-release label after the `-` becomes the channel name:

| `pluginVersion` | Published channel | Visible in normal Marketplace search? |
| --------------- | ----------------- | ------------------------------------- |
| `1.3.0`         | `default` (stable)| Yes                                   |
| `1.3.0-beta.1`  | `beta`            | No — opt-in only                      |
| `1.3.0-alpha.2` | `alpha`           | No — opt-in only                      |
| `1.3.0-eap.1`   | `eap`             | No — opt-in only                      |

So a beta release is just a version with a `-beta.N` suffix. You do **not** edit
the CI or the publish config to change channels.

## How CI triggers

`.github/workflows/release.yml` runs on:

```yaml
on:
  release:
    types: [ prereleased, released ]
```

It checks out the commit the release tag points at:

```yaml
- uses: actions/checkout@v6
  with:
    ref: ${{ github.event.release.tag_name }}
```

Two consequences:

- A GitHub **pre-release** (the "Set as a pre-release" checkbox / `--prerelease`)
  fires the same workflow as a full release.
- CI builds **whatever commit the tag points at** — any branch. The tag does not
  need to be on `main`, so you can release a beta without merging.

## Releasing a beta from a feature branch (no merge)

Example: releasing `1.4.0-beta.1` from `feat/add-ideavimrc-button`.

### 1. Set the beta version on the branch

In `gradle.properties`:

```properties
pluginVersion = 1.4.0-beta.1
```

Commit it to the branch and push. CI builds the tagged commit, so the version
bump must exist on that commit.

### 2. Make sure the changelog has notes for this version

The publish task depends on `patchChangelog` and pulls change notes from
`CHANGELOG.md` for the current `pluginVersion` (falling back to `Unreleased`).
Add an entry so testers see what changed.

### 3. Tag the branch HEAD and push

```bash
git tag 1.4.0-beta.1
git push origin 1.4.0-beta.1
```

### 4. Create the GitHub release as a pre-release

UI: Releases → Draft a new release → select the `1.4.0-beta.1` tag → check
**"Set as a pre-release"** → Publish.

Or with the CLI:

```bash
gh release create 1.4.0-beta.1 \
  --target feat/add-ideavimrc-button \
  --prerelease \
  --title "1.4.0-beta.1" \
  --notes "Beta: ideavimrc button"
```

`--target` anchors the tag to the branch if it does not exist yet (skip the
manual `git tag` step in that case).

### 5. CI publishes to the beta channel

The Release workflow runs `./gradlew publishPlugin`, which routes to the `beta`
channel because of the `-beta` suffix, and uploads the signed artifact as a
release asset. The build does **not** appear in the default Marketplace listing.

> Note: the workflow's "Create Pull Request" step opens a changelog PR when the
> release body is non-empty. That PR targets the default branch — review/close it
> as you see fit; it does not affect what was published.

## How testers install a beta

Beta-channel builds are not in normal Marketplace search. Testers add the beta
repository in **Settings → Plugins → ⚙ → Manage Plugin Repositories**:

```
https://plugins.jetbrains.com/plugins/beta/list
```

Then install/update **Vim Coach** as usual. (The per-plugin custom-repository URL
shown on the plugin's Marketplace page works too.)

## Beta testing an alternative remote source

The remote source defaults to the `pooryam92/vim-coach` GitHub Contents API. To point an
installed plugin at a different source (e.g. a beta branch) without rebuilding, set the
`vimcoach.tip.remote.url` JVM option via `Help | Edit Custom VM Options`, then restart the IDE:

```
-Dvimcoach.tip.remote.url=https://api.github.com/repos/pooryam92/vim-coach/contents/tips/vim_tips_min.json?ref=beta
```

Remove the line and restart to return to the default source. The override applies only in
remote mode; the response must match the same GitHub Contents API shape (base64 `content` + `sha`).

## Required secrets

`publishPlugin` needs these repo secrets (already used by the Release workflow):

- `PUBLISH_TOKEN` — Marketplace token (profile → My Tokens)
- `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD` — plugin signing

See <https://plugins.jetbrains.com/docs/intellij/plugin-signing.html>.

## Going stable later

When the feature is merged and ready for a stable release, drop the pre-release
label (e.g. `1.3.0`) on `main`, tag it, and publish a normal (non-pre-release)
GitHub release. The empty pre-release label routes it back to the `default`
channel.

## Checklist

- [ ] `pluginVersion = X.Y.Z-beta.N` committed on the branch
- [ ] `CHANGELOG.md` has notes for that version
- [ ] Tag pushed pointing at the branch commit
- [ ] GitHub release created with **pre-release** checked
- [ ] Release workflow green; build uploaded as a release asset
- [ ] Beta appears under the beta channel (not in default search)
