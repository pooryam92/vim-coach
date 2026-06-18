# Display & Wording

How a tip renders in the IntelliJ balloon, and how to word the `summary` and
`details`. See [tip-format.md](tip-format.md) for the field structure and
[README](README.md) for the loop.

## Display

Tips show in an IntelliJ balloon. Title is the static "Vim Coach"; `summary`
renders **bold** on top, `details` below, all as HTML. The body is 240px wide
w 
(`BalloonLayoutConfiguration.MaxWidthStyle`) — roughly **30–35 chars/line** before
wrapping, and only ~2 wrapped body lines show before the user must scroll. So keep
the summary on one line and details short, or details get pushed out of view.
Abbreviations (`char`, `msg`, `prev`) are fine when spelling out would wrap; drop
filler like `with` when the command already makes the relationship clear
(`Replace one character r{char}`).

**Keystroke separator (REQUIRED — no exceptions):** when a summary ends with the
keys it teaches, attach them with a **single space** and nothing else. Do **not**
use a dash, colon, arrow, or parentheses as a separator. The whole tip set relies
on this so the keys form a scannable command-first column; a stray ` - ` makes the
tip an outlier (it appears in zero other categories).

| Use this | Never this |
|---|---|
| `Add surroundings ys{motion}` | `Add surroundings - ys{motion}` |
| `Change surroundings cs` | `Change surroundings: cs` |
| `Surround a selection S` | `Surround a selection (S)` |
| `Wrap in an HTML tag ysiwt` | `Wrap in an HTML tag → ysiwt` |

`with` stays allowed only as genuine prose linking words (`Show line numbers with
number`), never as a substitute separator when a plain space reads cleaner
(`Delete surroundings ds`, not `Delete surroundings with ds`).

**Identity:** a user hiding a tip is keyed to a hash of its trimmed `summary`.
Renaming a summary resets any existing user preference for it — reword when it's
an improvement, but know the cost.

## Wording: Good vs. Worse

| Good | Worse | Why |
|---|---|---|
| `Repeat last substitution with &` | `Use the substitute repeat command` | command-first, concrete outcome |
| `Next/previous tab gt / gT` | `Next and previous tab gt / gT` | consistent pair phrasing |
| `Browse command-line history Ctrl-p / Ctrl-n` | `Browse cmdline history …` | spell out `command-line` in user text |
| `Show line numbers with number` | `Line numbers` | action/outcome, not a noun label |
| `Open help with :help` | `Built-in help :help` | verb-first, not a taxonomy label |
| `Add surroundings ys{motion}` | `Add surroundings - ys{motion}` | keys attach with a space, never a dash ([separator rule](#display)) |
| `Add surroundings ys{motion}` | `Surround text with vim-surround` | outcome + keys in summary; plugin name lives in `config`/details |

- Command-first, concrete, user outcome over Vim taxonomy. Verb-first over labels.
- Attach trailing keys with a plain space, never a `-`/`:`/`(…)` separator — see
  the [enforced separator rule](#display).
- Make mode/prompt context explicit only when it helps; name modes `Normal mode`
  / `Insert mode` / `Visual mode`.
- Reuse pair phrasing across similar tips: `next/previous`, `before/after`,
  `top/bottom`. Prefer `command line` in user text; reserve `cmdline` for the slug.
- One strong tip over two near-duplicates — merge overlap with multiple
  categories. Split a dense line; no semicolons joining prose (unless `;` is the
  key taught).
- For IdeaVim/plugin tips put the user outcome in the summary, the plugin/option
  name in details. Avoid config-authoring summaries unless config *is* the workflow.
- Don't make detail line 1 restate the summary — it wastes the most-read line.
  `Surround a line yss` → detail `yss wraps the line` teaches nothing; use that
  line for the mechanic or a mnemonic instead (`2nd s = whole line, like dd/yy`).
- Never dump alternative keys as an inline symbol list — `Swap ) for ] } " ' t`
  reads like keyboard mashing. Name the category in prose (`Any bracket or quote
  works too`) and keep at most one concrete symbol as the example.

### Every keystroke shown must do something when typed

A **text object** (`iw`, `ac`, `ii`, `ai`, `aI`, `am`/`im`) does **nothing** on
its own — it only acts after an operator (`d`, `c`, `y`, `>`, `=`) or in Visual
mode (`v`). So never put a bare text object in a summary as if it were a command:
the reader types it, sees nothing happen, and the tip looks broken. Show a
*usable* combination instead, and prove composition in the details.

| Good | Worse | Why |
|---|---|---|
| `Act on a class dac / cic` | `Select a class ac` | `ac` alone does nothing; `dac`/`cic` act |
| `Act on a block by indent dii / cii` | `Select an indent block ii / ai` | `ii`/`ai` are objects, not commands |

```json
{
  "category": ["plugins", "visual"],
  "summary": "Act on a block by indent dii / cii",
  "details": [
    "dii deletes lines at the current indent",
    "cii changes them, yii yanks them",
    "Works with any operator, even > and ="
  ]
}
```

The summary carries usable keys; the third detail line teaches that the object
composes with *any* operator. This also distinguishes the variations honestly —
`ic`/`ii` is the inner body, `ac`/`ai` adds the surrounding line(s) — instead of
listing two objects the reader can't tell apart.

### Each tip stands alone (display order is random)

Tips are picked at random, one at a time, so a tip may **never** assume the
reader just saw another. A summary like `Repeat a surround with .` whose details
say "replays the last `ys`, `cs` or `ds`" is meaningless unless those commands
were just introduced — which random order never guarantees. Two fixes:

- **Fold** the dependent point into the tip(s) it relies on. The dot-repeat note
  belongs *inside* the `ys` and `cs` surround tips (one extra detail line each:
  `Press . to repeat it on the next match`), not as its own tip.
- **Split** an overloaded tip into focused, self-contained ones rather than
  cramming variations into two lines. One object + operator per tip reads clean
  and each survives being shown in isolation. Only split when each piece earns a
  distinct summary (the generator rejects duplicate summaries); two objects can
  stay one tip, three+ usually want splitting.

Also **name the feature, not just the keys**: `{ } paragraph jumps stop at
blanks` tells the reader `{ }` *are* paragraph motions; `Stop { } at
whitespace-only lines` assumes they already know. State the surprising default,
then the change.
