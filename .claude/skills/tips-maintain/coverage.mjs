#!/usr/bin/env node
// Coverage report: IdeaVim's real capability surface vs. what the tip set teaches.
// Advisory only (always exits 0, like lint-tips.mjs). It does NOT judge tip
// quality or prove a gap is worth filling — it surfaces raw candidates for the
// tips-maintain "Finding the best tips to add" value rubric to score. A "miss"
// means no tip text mentions that command's keys; a textual heuristic, so eyeball
// every hit (some keys are taught in a form the matcher can't see, and not every
// supported feature is worth a tip).
//
// Matching rules:
//   - command keys match case-sensitively (zD is not zd), either verbatim or in
//     the notation tips use: <C-W>h -> "Ctrl-w h", <CR> -> Enter, <A-j> -> Alt-j
//   - ex-commands match only as `:name` (any abbreviation, optional range), or as
//     the first word of a config line, where rc commands carry no colon
//   - plugins match by id, `set` name or any Plug alias from IdeaVim Plugins.md
//
//   node .claude/skills/tips-maintain/coverage.mjs            # summary + top misses
//   node .claude/skills/tips-maintain/coverage.mjs --all      # every miss, incl. 1-char keys
//   node .claude/skills/tips-maintain/coverage.mjs --plugins  # plugin coverage only
//
// Paths are relative to the repo root; run it from there.

import { readFileSync, readdirSync, existsSync, statSync } from 'node:fs'

const IDEAVIM = 'external/ideavim'
const ENGINE_KSP = `${IDEAVIM}/vim-engine/src/main/resources/ksp-generated`
const FRONTEND_KSP = `${IDEAVIM}/src/main/resources/ksp-generated`
const PLUGIN_DIR = `${IDEAVIM}/src/main/java/com/maddyhome/idea/vim/extension`
const PLUGIN_DOC = `${IDEAVIM}/doc/IdeaVim Plugins.md`

if (!existsSync(ENGINE_KSP)) {
  console.error(
    `IdeaVim submodule not checked out at ${IDEAVIM}.\n` +
    `Run:  git submodule update --init external/ideavim\n` +
    `(see the tips-maintain skill's reference.md for sparse-checkout setup).`,
  )
  process.exit(0)
}

const readJson = (p) => JSON.parse(readFileSync(p, 'utf8'))
const escapeRegex = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

// --- IdeaVim's real surface -------------------------------------------------
const commandKeys = new Set()
for (const path of [`${ENGINE_KSP}/engine_commands.json`, `${FRONTEND_KSP}/frontend_commands.json`]) {
  if (!existsSync(path)) continue
  for (const c of readJson(path)) if (c.keys) commandKeys.add(c.keys)
}

// ex_commands.json is an object: { "ab[breviate]": "...Class" }. The bracketed
// span is the optional tail of the command name (Vim's abbreviation notation).
const exCommands = new Set()
for (const path of [`${ENGINE_KSP}/engine_ex_commands.json`, `${FRONTEND_KSP}/frontend_ex_commands.json`]) {
  if (!existsSync(path)) continue
  for (const name of Object.keys(readJson(path))) exCommands.add(name)
}

// A plugin's source directory name often differs from the token a user actually
// writes to enable it (dir `multiplecursors` -> `set multiple-cursors`, dir
// `hints` -> `VimEverywhere`), so read the real name from getName() too.
function pluginEnableName(dir) {
  const path = `${PLUGIN_DIR}/${dir}`
  for (const f of readdirSync(path)) {
    if (!f.endsWith('.kt') && !f.endsWith('.java')) continue
    const m = readFileSync(`${path}/${f}`, 'utf8')
      .match(/getName\(\)\s*(?::\s*String\s*)?=\s*"([^"]+)"/)
    if (m) return m[1]
  }
  return dir
}

const squash = (s) => s.toLowerCase().replace(/[-_]/g, '')

// Each <h2> block of IdeaVim Plugins.md documents one plugin: its heading
// name, its `set <name>` forms and its Plug aliases. Tips install plugins with
// Plug lines (`Plug 'michaeljsmith/vim-indent-object'`), which share nothing
// with the source dir (`textobjindent`), so match through these aliases.
function pluginDocSections() {
  if (!existsSync(PLUGIN_DOC)) return []
  // Split on <h2>, not <details>: the "Alternative syntax" aliases sit in a nested <details>.
  return readFileSync(PLUGIN_DOC, 'utf8').split('<h2>').slice(1).map((block) => {
    const heading = block.match(/^([^:<]+)/)?.[1]?.trim()
    // Only code-formatted `set` lines; prose like "set to <leader>" would alias "to".
    const setNames = [...block.matchAll(/(?:`|<code>)set ([A-Za-z][\w.-]*)/g)].map((m) => m[1])
    const plugRefs = [...block.matchAll(/Plug '([^']+)'/g)]
      .map((m) => m[1].replace(/^https:\/\/github\.com\//, ''))
      .filter((ref) => !ref.includes('://') && !ref.startsWith('<'))
    const names = [heading, ...setNames].filter(Boolean)
    return { ids: new Set(names.map(squash)), aliases: [...names, ...plugRefs, ...plugRefs.map((r) => r.split('/').pop())] }
  })
}
const docSections = pluginDocSections()

const plugins = existsSync(PLUGIN_DIR)
  ? readdirSync(PLUGIN_DIR)
      .filter((n) => statSync(`${PLUGIN_DIR}/${n}`).isDirectory())
      .map((dir) => {
        const enable = pluginEnableName(dir)
        const aliases = new Set([dir, enable])
        for (const s of docSections) {
          if (s.ids.has(squash(dir)) || s.ids.has(squash(enable))) s.aliases.forEach((a) => aliases.add(a))
        }
        return { dir, enable, aliases: new Set([...aliases].map(squash)) }
      })
  : []

// --- What the tips already say ----------------------------------------------
const TIPS_DIR = 'tips/categories'
let haystack = ''
let tipCount = 0
const configCommands = new Set()
for (const f of readdirSync(TIPS_DIR)) {
  if (!f.endsWith('.json')) continue
  const { tips = [] } = readJson(`${TIPS_DIR}/${f}`)
  for (const t of tips) {
    tipCount++
    haystack += '\n' + (t.summary || '') + '\n' + (t.details || []).join('\n')
    const lines = Array.isArray(t.config) ? t.config : t.config?.lines ?? []
    for (const line of lines) {
      haystack += '\n' + line
      const first = line.trim().split(/\s+/)[0]
      if (first) configCommands.add(first)
    }
  }
}

// Words for plugin matching: whole tokens, plus the repo part of owner/repo, so
// the word "signatures" doesn't count as the signature plugin.
const haystackWords = new Set()
for (const token of haystack.split(/[\s'"`,;:()]+/)) {
  if (!token) continue
  haystackWords.add(squash(token))
  if (token.includes('/')) haystackWords.add(squash(token.split('/').pop()))
}

// --- Command keys -----------------------------------------------------------
const NAMED_KEYS = {
  CR: 'Enter', Enter: 'Enter', Return: 'Enter', Esc: 'Esc', Tab: 'Tab',
  BS: 'Backspace', Del: 'Delete', DEL: 'Delete', Space: 'Space', Insert: 'Insert',
  Undo: 'Undo', Up: 'Up', Down: 'Down', Left: 'Left', Right: 'Right',
  Home: 'Home', End: 'End', PageUp: 'PageUp', PageDown: 'PageDown',
}
const LITERAL_KEYS = { lt: '<', Bar: '|', Bslash: '\\' }
const MODIFIERS = { C: 'Ctrl', S: 'Shift', A: 'Alt', M: 'Alt', D: 'Cmd' }

// One <...> key in the notation tips use: <C-X> -> Ctrl-x, <kLeft> -> Left.
function keyName(inner) {
  const parts = inner.split('-')
  let key = parts.pop()
  if (key === '' && inner.endsWith('-')) key = '-'
  const mods = parts.map((m) => MODIFIERS[m] ?? m)
  const base = key.replace(/^k(?=[A-Z])/, '')
  let name = NAMED_KEYS[base] ?? base
  if (mods.includes('Ctrl') && /^[A-Za-z]$/.test(name)) name = name.toLowerCase()
  return [...mods, name].join('-')
}

// <C-W>h -> "Ctrl-w h", g<C-X> -> "g Ctrl-x"; plain runs stay glued (gg, i<).
// `spell` renders one <...> key; tips mostly write keyName, a few bare Vim (S-Left).
function tipNotation(keys, spell = keyName) {
  const segments = []
  let plain = ''
  for (const m of keys.matchAll(/<([^<>]+)>|[\s\S]/g)) {
    if (m[1] === undefined || LITERAL_KEYS[m[1]]) {
      plain += m[1] === undefined ? m[0] : LITERAL_KEYS[m[1]]
      continue
    }
    if (plain) segments.push(plain), (plain = '')
    segments.push(spell(m[1]))
  }
  if (plain) segments.push(plain)
  return segments.join(' ')
}

// Bare, these read as the mode or the verb far more often than as the key.
const ENGLISH_KEY_NAMES = new Set(['Insert', 'Undo'])

function mentionsKeys(keys) {
  if (haystack.includes(keys)) return true
  const notations = new Set([tipNotation(keys), tipNotation(keys, (inner) => inner)])
  notations.delete(keys)
  // Word boundaries so <Tab> isn't found inside Shift-Tab and <Up> not in "Update".
  return [...notations].some((n) => !ENGLISH_KEY_NAMES.has(n) &&
    new RegExp(`(?<![A-Za-z0-9-])${escapeRegex(n)}(?![A-Za-z0-9])`).test(haystack))
}

// --- Ex-commands ------------------------------------------------------------
const exName = (n) => n.replace(/[\[\]]/g, '') // ab[breviate] -> abbreviate
const exShort = (n) => n.replace(/\[.*$/, '') // ab[breviate] -> ab
const EX_RANGE = String.raw`(?:[%.$\d,;+\-]|'[<>a-zA-Z])*`

// `:ab`, `:abbrev` and `:%s` all count; `:set` does not count for `:s`.
function mentionsExCommand(n) {
  const short = exShort(n)
  const tail = exName(n).slice(short.length)
  const prefixes = [short]
  for (let i = 1; i <= tail.length; i++) prefixes.push(short + tail.slice(0, i))
  if (prefixes.some((p) => configCommands.has(p))) return true
  const alternatives = prefixes.sort((a, b) => b.length - a.length).map(escapeRegex).join('|')
  return new RegExp(`:${EX_RANGE}(?:${alternatives})(?![A-Za-z])`).test(haystack)
}

const args = process.argv.slice(2)
const flag = (f) => args.includes(f)

const cmdMisses = [...commandKeys]
  .filter((k) => (flag('--all') ? true : k.length > 1))
  .filter((k) => !mentionsKeys(k))
  .sort()
const exMisses = [...exCommands]
  .filter((n) => !mentionsExCommand(n))
  .map(exName)
  .sort()
const pluginMisses = plugins
  .filter((p) => ![...p.aliases].some((a) => haystackWords.has(a)))
  .map((p) => (p.enable === p.dir ? p.dir : `${p.dir} (${p.enable})`))

function section(title, items) {
  console.log(`\n${title} (${items.length})`)
  if (!items.length) { console.log('  — all referenced —'); return }
  const width = process.stdout.columns || 80
  let line = '  '
  for (const it of items) {
    if ((line + it + '  ').length > width) { console.log(line); line = '  ' }
    line += it + '  '
  }
  if (line.trim()) console.log(line)
}

console.log('IdeaVim coverage report (advisory — eyeball every hit)')
console.log(`  tips: ${tipCount}   commands: ${commandKeys.size}   ` +
  `ex-commands: ${exCommands.size}   plugins: ${plugins.length}`)

if (flag('--plugins')) {
  section('Plugins with no tip mentioning them by name', pluginMisses)
  console.log()
  process.exit(0)
}

section('Command keys no tip text mentions', cmdMisses)
section('Ex-commands no tip text mentions as :name', exMisses)
section('Plugins with no tip mentioning them by name', pluginMisses)
console.log(
  '\nNote: textual heuristic. A miss is a candidate, not a verdict — a key can be ' +
  'taught in a form the matcher misses, and not every supported command\n' +
  'deserves a tip. Score candidates with the tips-maintain value rubric before authoring.\n',
)
