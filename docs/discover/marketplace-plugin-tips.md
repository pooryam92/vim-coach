# Discovery: Tips That Need a Marketplace Plugin

Status: **brainstorm, no decisions yet** (2026-10-03).

Some IdeaVim plugins only work once a separate JetBrains Marketplace plugin is
installed — EasyMotion (IdeaVim-EasyMotion + AceJump), which-key, quick-scope,
FunctionTextObj. The list and why they're blocked today:
[config-tips-roadmap.md](config-tips-roadmap.md#deferred-plugins-that-need-an-external-ide-plugin).
The **Add to .ideavimrc** button can only append the config line, so such a tip
would ship a button that silently does nothing.

## Constraint: older plugin versions ignore new fields

Tips are fetched remotely and `TipJsonParser` ignores unknown fields on purpose
([tips-pipeline.md](../tips/tips-pipeline.md#schema-evolution-and-the-advanced-field)).
Any new marker (`"requires": …`) is invisible to every version already
installed — they'd show the tip with a working-looking, broken button. Any
option below has to answer this too.

## Ideas

**Marking the dependency**

- An optional `requires` field: Marketplace plugin ID + display name,
  validated by the generator.

**What a user without the plugin sees**

- Hide the tip until the plugin is installed — same shape as suppressing config
  tips while IdeaVim is absent. Safe, but the users who'd benefit never learn
  the plugin exists.
- Show it with a dimmed "Needs AceJump" label (like the `mode` label), button
  disabled or relabelled. Discovery survives; install is still manual.
- Button becomes "Install AceJump", then appends the config line. Best UX;
  IntelliJ's install APIs are partly internal — verify in the SDK docs first. A
  cheaper variant opens Settings → Plugins pre-searched.
- One "advert" tip per plugin shown while it's missing, usage tips only once
  it's installed.

**Keeping older versions safe**

- Publish these tips in a separate file (e.g. `vim_tips_plugins.json`) that only
  new versions fetch.
- Put their config under a new key (e.g. `pluginConfig`) so old versions show
  no button — the text still advertises something they can't use.

## Open questions

- EasyMotion and which-key default to `<leader>` keys, which config tips can't
  ship yet either. Does a non-leader map (`nmap s <Plug>(easymotion-s)`) make
  EasyMotion shippable once the dependency is solved?
- Does installing a dynamic plugin need an IDE restart before the `.ideavimrc`
  line takes effect?
- Is a tip whose first step is "install a plugin" still tryable on the spot, or
  does it need a softer bar than the skill's four-axis score?
