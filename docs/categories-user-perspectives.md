# Discovery: Tip Categories From The User's Perspective

How real users think about tip categories, and what that implies for the
taxonomy. This is the *why* behind the category list; for the concrete taxonomy,
migration mechanics, and config-tip plan, see
[config-tips-plan.md](config-tips-plan.md). For authoring rules, see
[tips-authoring/](tips-authoring/README.md).

---

## Why this document exists

The current 17 categories mirror **Vim's help-file structure** (`motion.txt`,
`change.txt`, `undo.txt`, …). That is a *reference* taxonomy — how the Vim manual
is organized — not how a person decides "what do I want to get better at." The
project already preaches "prefer user outcome over Vim taxonomy" for tip *text*;
this document applies the same rule to the *categories themselves*.

The guiding question is not **"what Vim topic is this tip?"** It is
**"what is the user trying to do when they reach for it?"**

---

## Where the user actually meets a category

Categories are not an internal filing system — they are a **user-facing control**:

- The **Settings panel renders one checkbox per category** (labeled with the
  category name). The user opts in or out of whole categories there.
- A tip is shown if **any** of its categories is enabled (OR semantics). So a
  category is effectively a faucet: checking it opens a stream of tips on that
  job; unchecking it shuts that stream off.

Implications that shape good categories:

- **Each box should map to a goal the user recognizes.** `cmdline` or `tabpage`
  is jargon; "the thing I'm trying to do" is not.
- **Fewer, clearer boxes beat many granular ones.** A wall of 17 niche toggles is
  cognitive load; a learner does not separate "motion" from "scroll" from
  "change."
- **A box only earns its place if a user would deliberately turn it on or off.**
  If nobody thinks "I want to silence *undo* tips specifically," `undo` should
  not be its own box.

---

## Who the user is

| Persona | Where they are | What they want from categories |
|---|---|---|
| **Fresh installer** | Just enabled IdeaVim, knows almost nothing | A short, friendly list; wants the essentials, not a taxonomy |
| **Editor switcher** | Came from VS Code / Sublime, knows *what* they want, not the Vim *how* | Goal-named boxes they can map to old habits ("how do I jump to a definition?") |
| **Skill-leveler** | Comfortable with basics, wants to get faster at one thing | Wants to focus a single job ("just feed me editing tips this month") |
| **Opt-outer** | Already fluent in motions, tired of seeing them | Wants to *turn off* what they've mastered without losing the rest |
| **Tinkerer** | In "set up my editor" mode for a weekend | Wants a stream of config recipes they can one-click install |

The common thread: a category is something a user **focuses on** or **mutes**,
and they reason about it in terms of a *job*, not a Vim manual section.

---

## The jobs (use cases), in the user's own words

Each scenario is a first-person intent at the moment the user opens settings.
The category it implies is in **bold**.

> **"I want to move through code faster."**
> Jump to a definition, bounce between methods, `%` across brackets, search to
> move, recenter the screen, fold away the noise.
> → **Navigation**

> **"I want to change code faster."**
> Operators plus text objects, `ciw` / `ci"`, yank and paste, registers, `.` to
> repeat, undo a mistake, multiple cursors, surround / comment text.
> → **Editing**

> **"I want to select exactly the right text."**
> Visual mode, growing a selection, visual-block edits.
> → **Selection** *(candidate to fold into Editing — see Open Calls)*

> **"I want to find and replace across the file."**
> `/` search patterns, `:s` substitutions, `:g` global commands, `*` / `#` on the
> word under the cursor.
> → **Search & replace**

> **"I'm juggling files, tabs, and splits."**
> Switch buffers, `:e` / `:w` / `:q`, split the window, move between panes, tab
> pages, NERDTree.
> → **Workspace**

> **"I want to type faster while inserting."**
> Insert-mode shortcuts, completion, `Ctrl-w` / `Ctrl-r`, abbreviations.
> → **Insert mode**

> **"I want to drive the IDE from the keyboard."**
> Run IDE actions with `:action`, Ex power-commands, `:normal`, `:source`,
> command-line history.
> → **Command line**

> **"I want to set up and tune my IdeaVim."**
> Toggle options, enable plugins, learn IdeaVim-specific behavior — the one-click
> `.ideavimrc` recipes.
> → **Setup & config**

There are also **mood-based** uses of the same boxes, which good categories must
support:

- **"Just the essentials, I'm overwhelmed."** → user checks 2–3 core boxes
  (Navigation, Editing) and mutes the rest.
- **"Stop showing me what I already know."** → user unchecks Navigation, keeps
  everything else. Only works if the box maps to a coherent skill.
- **"I'm in setup mode this weekend."** → user checks **Setup & config** and gets
  a focused stream of installable recipes.
- **"Show me everything, I'm exploring."** → all boxes on (the default).

---

## More use cases the tips actually reveal (finer-grained jobs)

The 8 jobs above are the coarse cut. Reading every tip summary surfaces several
**finer jobs** that the coarse buckets either bury inside a big box or split
across two. Each is written here in the user's voice, with the tips that back it,
a rough size, and a **verdict**: does it earn its own checkbox, fold into a
bigger box, or stay a cross-cutting sub-skill that lives in several?

> **"I keep losing my place — get me back to where I was."**
> Drop a mark and return (`` ma `` / `` `a ``), bounce the jump list
> (`Ctrl-o` / `Ctrl-i`), `` `. `` to the last edit, `g;` / `g,` through the
> change list, `` `[ `` / `` `] `` around the last edit, inspect with `:jumps` /
> `:marks`.
> → **Marks & jumps** *(~12 tips, today split across `motion` + `cmdline`)*
> **Verdict:** a job users name in pain ("I lost my spot"), but small. Lean:
> a **secondary tag inside Navigation**, promoted to its own box only if
> Navigation gets too broad.

> **"I want to copy and paste like a pro."**
> The system clipboard (`"+`), the yank register (`"0`), recover a delete
> (`"1`), named registers (`"a` / `"A`), the black hole (`"_`), `:reg` to see
> them all, paste variants (`p` / `P` / `gp` / `]p`), and the IdeaVim clipboard
> options (`unnamedplus`, `ideaput`).
> → **Registers & clipboard** *(~15 tips, today inside `editing` + `ideavim`)*
> **Verdict:** a distinct mental model — "managing what I've copied" is not the
> same as "changing text." Strongest candidate to **split out of Editing**, which
> is the 61-tip dumping ground.

> **"I want to make the same edit in fifty places."**
> `.` to repeat the last change, the `* cw n .` rename loop, `cgn` to step
> through matches, macros (`q` / `@` / `@@` / `{count}@`), append to a macro
> (`qA`), run one on a range (`:normal @r`), replay the last Ex command (`@:`),
> add a cursor at the next match, `:g` to act on every matching line.
> → **Repeat & automate** *(~12 tips, today across `repeat` + `change` +
> `pattern` + `editing`)*
> **Verdict:** a real standalone goal ("automate the boring edit"). Note this
> **breaks the planned `undo`+`repeat`→`history` merge**: *undo* is "take it
> back," *repeat/macros* is "do it again" — opposite intents that shouldn't share
> a box.

> **"I want to drive the IDE's intelligence from the keyboard."**
> Go to definition (`gd` / `gD`), find usages, rename element, go to
> implementation, jump between methods (`]m` / `[m`) and sections (`[[` / `]]`),
> quick docs (`K`), trigger completion (`Ctrl-Space`), step through misspellings
> (`]s` / `[s`).
> → **Code & IDE actions** *(~10 tips, today across `motion` + `ideavim`, plus
> the future `<Action>` mapping recipes — Bucket C)*
> **Verdict:** the highest-"wow" job and the **showcase surface for the one-click
> `<Action>` recipes**. Strong own-box candidate; overlaps Navigation and
> Setup & config (enable-the-mapping vs. use-the-mapping, same split as plugins).

> **"I want to select by structure, not by counting characters."**
> Text objects: `iw` / `aw`, `i"` / `a"`, `i(` / `ib`, `it` / `at`, `is` / `as`,
> arguments (`ia` / `aa`), functions (`if` / `af`), indent blocks (`ii` / `ai`),
> the whole file (`ae` / `ie`).
> → **Text objects** *(~15 tips, today across `editing` + `visual`)*
> **Verdict:** a *means*, not a goal — it powers both Editing and Selection.
> **Cross-cutting sub-skill**, not its own box; multi-tag it instead.

> **"I want to clean up formatting and case."**
> Change case (`gu` / `gU` / `g~`), reflow (`gq` / `gqap`), reindent (`==` / `=`),
> indent / outdent (`>>` / `<<`), join lines (`J`), `cr` coercions, `switch`.
> → **Format & transform** *(~12 tips)*
> **Verdict:** **folds into Editing**; little standalone opt-out appeal.

> **"I don't know what I don't know — show me what's available."**
> `:help`, list mappings (`:map`), `:reg`, `:marks`, `:history`, `:set option?`,
> inspect a character (`ga`), quick docs (`K`), find action ids (`:actionlist`,
> `trackactionids`), the IdeaVim Tutor.
> → **Discover & help** *(~12 tips, across `cmdline` + `ideavim` + `map`)*
> **Verdict:** a genuine **beginner** job, but introspection tips scatter by
> nature. Lean: **fold into Command line / Setup & config**, not its own box.

> **"I want to crunch numbers and sort."**
> Increment / decrement (`Ctrl-a` / `Ctrl-x`), sequential (`g Ctrl-a`), sort
> lines (`:sort`, `:sort n`).
> → *(~5 tips)* **Verdict:** too small — **fold into Editing**.

### What this changes about the 8-job cut

| Finer job | Size | Verdict |
|---|---|---|
| Marks & jumps | ~12 | Secondary tag inside **Navigation** |
| Registers & clipboard | ~15 | **Own box** candidate (relieves the 61-tip Editing bucket) |
| Repeat & automate | ~12 | **Own box** candidate; do **not** merge with `undo` |
| Code & IDE actions | ~10+ | **Own box** candidate; Bucket-C showcase |
| Text objects | ~15 | Cross-cutting sub-skill, multi-tag |
| Format & transform | ~12 | Fold into Editing |
| Discover & help | ~12 | Fold into Command line / Setup |
| Numbers & sort | ~5 | Fold into Editing |

The headline: **Editing (61 tips) is doing too many jobs.** Three of the finer
use cases above (Registers & clipboard, Repeat & automate, and arguably text
objects) are the natural seams to split it along — which is the same finding the
8-job table flagged for the old `editing` bucket, now at one more level of
resolution. And **Code & IDE actions** is the one genuinely *new* box the coarse
cut underweights, despite being the feature's biggest selling point.

---

## What the jobs imply: the proposed categories (17 → 8)

| Category | The job it serves | Absorbs (old) |
|---|---|---|
| **Navigation** | move through code | motion, scroll, fold |
| **Editing** | change text | change, undo, repeat, text-edit half of editing |
| **Selection** | select precisely | visual |
| **Search & replace** | find / replace | pattern |
| **Workspace** | manage files, tabs, splits | windows, tabpage, file/buffer half of editing |
| **Insert mode** | type faster | insert |
| **Command line** | drive the IDE | cmdline |
| **Setup & config** | set up the editor | options, ideavim, plugin |

Every existing tip maps to exactly one primary job; nothing is orphaned.

Two structural findings drove this:

1. **`editing` was three jobs in one bucket** — file management (`:w`, `:bn`),
   text editing (`ciw`, `yy`), and plugin actions. A user never thinks `:w` and
   `ciw` are the same topic, so the bucket splits across **Workspace** and
   **Editing**.
2. **"Set up my editor" is a real job, not just a field.** This is why
   **Setup & config** earns its own box even though the `config` field already
   marks actionable tips. The field powers the *button*; the category serves the
   *intent*. This is also the showcase surface for the one-click `.ideavimrc`
   feature — a user checks one box and receives a stream of installable recipes.

---

## Cross-cutting notes

### One tip can serve two jobs

OR semantics make multi-tagging a feature, not a hazard. A code-navigation
mapping serves both *"move faster"* and *"set up my editor."* The rule:

- **Primary category = the main job** (decides the tip's authoring file).
- **Secondary categories = other jobs it also serves** (widen who sees it).

Example: `nmap gr <Action>(FindUsages)` → primary **Navigation**, secondary
**Setup & config**.

### The enable / use split becomes natural

A plugin yields two clean tips with obvious homes:

- *"Enable surround"* (the config line) → **Setup & config**
- *"Edit surroundings with `ysiw)`"* (how to use it) → **Editing**

### Trade-offs we are accepting

- **Setup & config is a large merge** (options + ideavim + plugin). A user can no
  longer mute *just plugins*. Given OR semantics already made a granular plugin
  opt-out leaky, this is an acceptable simplification.
- **Coarser opt-out.** Folding `undo`, `repeat`, `scroll`, `fold`, `tabpage` into
  bigger boxes means a user cannot silence those niches individually — but
  almost no user wants to, which is the point.

---

## Open calls

These are genuine product judgment calls, not derivable from the jobs alone:

1. **Selection** — keep it as its own job (8 categories), or fold it into
   **Editing** (7)? "Select text" is arguably a sub-skill of editing rather than
   a standalone goal; 23 tips argue for keeping it.
2. **Workspace** — one box for files + tabs + splits, or separate "Files &
   buffers" from "Windows & tabs"? One box matches how a user thinks about
   "juggling my workspace"; splitting gives finer opt-out.
3. **Code & IDE actions** — promote to its own box, or leave the `<Action>` /
   `gd` / `K` tips multi-tagged across Navigation + Setup & config? It is the
   feature's biggest selling point, which argues for visibility; against it, the
   tips are also legitimately Navigation. (See finer-jobs section above.)
4. **Split Editing** — do Registers & clipboard (~15) and/or Repeat & automate
   (~12) earn their own boxes, to relieve the 61-tip Editing bucket? Both are
   distinct user goals, not just sub-skills.
5. **Don't merge `undo` + `repeat`** — the config-plan taxonomy proposed a
   `history` box merging them. The finer-jobs read says **undo ("take it back")
   and repeat/macros ("do it again") are opposite intents** and should not share
   a box. Confirm: keep them separate (likely folding `undo` into Editing and
   `repeat` into a Repeat & automate box)?

---

## Next phase

Once the two open calls are settled, lock the taxonomy in
[config-tips-plan.md](config-tips-plan.md) and rewrite the **Category Reference**
in [tips-authoring/categories.md](tips-authoring/categories.md) so future tips are filed by job, not by
Vim manual section.
