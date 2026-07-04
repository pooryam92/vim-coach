#!/usr/bin/env node
// Coverage report: IdeaVim's real capability surface vs. what the tip set teaches.
// Advisory only (always exits 0, like lint-tips.mjs). It does NOT judge tip
// quality or prove a gap is worth filling — it surfaces raw candidates for the
// tips-maintain "Finding the best tips to add" value rubric to score. A "miss"
// means no tip text mentions that command's keys; a textual heuristic, so eyeball
// every hit (many are false positives: keys taught under a different notation, or
// features not worth a tip).
//
//   node .claude/skills/tips-maintain/coverage.mjs            # summary + top misses
//   node .claude/skills/tips-maintain/coverage.mjs --all      # every miss, ungrouped
//   node .claude/skills/tips-maintain/coverage.mjs --plugins  # plugin coverage only
//
// Paths are relative to the repo root; run it from there.

import { readFileSync, readdirSync, existsSync, statSync } from 'node:fs'

const IDEAVIM = 'external/ideavim'
const ENGINE_KSP = `${IDEAVIM}/vim-engine/src/main/resources/ksp-generated`
const FRONTEND_KSP = `${IDEAVIM}/src/main/resources/ksp-generated`
const PLUGIN_DIR = `${IDEAVIM}/src/main/java/com/maddyhome/idea/vim/extension`

if (!existsSync(ENGINE_KSP)) {
  console.error(
    `IdeaVim submodule not checked out at ${IDEAVIM}.\n` +
    `Run:  git submodule update --init external/ideavim\n` +
    `(see the tips-maintain skill's reference.md for sparse-checkout setup).`,
  )
  process.exit(0)
}

const readJson = (p) => JSON.parse(readFileSync(p, 'utf8'))

// --- IdeaVim's real surface -------------------------------------------------
const commandKeys = new Set()
for (const f of ['engine_commands.json', 'frontend_commands.json']) {
  const path = f.startsWith('engine') ? `${ENGINE_KSP}/${f}` : `${FRONTEND_KSP}/${f}`
  if (!existsSync(path)) continue
  for (const c of readJson(path)) if (c.keys) commandKeys.add(c.keys)
}

// ex_commands.json is an object: { "ab[breviate]": "...Class" }. The bracketed
// span is the optional tail of the command name (Vim's abbreviation notation).
const exCommands = new Set()
for (const f of ['engine_ex_commands.json', 'frontend_ex_commands.json']) {
  const path = f.startsWith('engine') ? `${ENGINE_KSP}/${f}` : `${FRONTEND_KSP}/${f}`
  if (!existsSync(path)) continue
  for (const name of Object.keys(readJson(path))) exCommands.add(name)
}

// A plugin's source directory name often differs from the token a user actually
// writes to enable it (dir `multiplecursors` -> `set multiple-cursors`, dir
// `hints` -> `VimEverywhere`). Matching on the dir name alone misreports those,
// so read the real name from each extension's getName() and match on both.
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
const plugins = existsSync(PLUGIN_DIR)
  ? readdirSync(PLUGIN_DIR)
      .filter((n) => statSync(`${PLUGIN_DIR}/${n}`).isDirectory())
      .map((dir) => ({ dir, enable: pluginEnableName(dir) }))
  : []

// --- What the tips already say ----------------------------------------------
const TIPS_DIR = 'tips/categories'
let tipText = ''
const tipSummaries = []
for (const f of readdirSync(TIPS_DIR)) {
  if (!f.endsWith('.json')) continue
  const { tips = [] } = readJson(`${TIPS_DIR}/${f}`)
  for (const t of tips) {
    tipSummaries.push(t.summary || '')
    tipText += ' ' + (t.summary || '') + ' ' + (t.details || []).join(' ')
    if (t.config?.lines) tipText += ' ' + t.config.lines.join(' ')
  }
}
const haystack = tipText.toLowerCase()

// A command's keys are "taught" if its literal key string appears in any tip
// text. Single-char keys (x, p, u…) and pure modifiers are too noisy to match
// honestly, so we skip 1-char alpha keys and report them only under --all.
const exName = (n) => n.replace(/[\[\]]/g, '') // ab[breviate] -> abbreviate
const exShort = (n) => n.replace(/\[.*$/, '') // ab[breviate] -> ab
const mentions = (s) => s && haystack.includes(s.toLowerCase())
// Hyphen/underscore-insensitive match, so the dir name `multiplecursors` is
// found in a tip's `set multiple-cursors` enable line and vice versa.
const squash = (s) => s.toLowerCase().replace(/[-_]/g, '')
const haystackSquashed = squash(haystack)
const mentionsLoose = (s) => s && haystackSquashed.includes(squash(s))

const args = process.argv.slice(2)
const flag = (f) => args.includes(f)

const cmdMisses = [...commandKeys]
  .filter((k) => (flag('--all') ? true : k.length > 1))
  .filter((k) => !mentions(k))
  .sort()
const exMisses = [...exCommands]
  .filter((n) => !mentions(exName(n)) && !mentions(exShort(n)))
  .map(exName)
  .sort()
const pluginMisses = plugins
  .filter((p) => !mentionsLoose(p.dir) && !mentionsLoose(p.enable))
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
console.log(`  tips: ${tipSummaries.length}   commands: ${commandKeys.size}   ` +
  `ex-commands: ${exCommands.size}   plugins: ${plugins.length}`)

if (flag('--plugins')) {
  section('Plugins with no tip mentioning them by name', pluginMisses)
  console.log()
  process.exit(0)
}

section('Command keys no tip text mentions', cmdMisses)
section('Ex-commands no tip text mentions', exMisses)
section('Plugins with no tip mentioning them by name', pluginMisses)
console.log(
  '\nNote: textual heuristic. A miss is a candidate, not a verdict — many keys ' +
  'are taught under different notation, and not every supported command\n' +
  'deserves a tip. Score candidates with the tips-maintain value rubric before authoring.\n',
)
