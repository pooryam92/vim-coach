#!/usr/bin/env node
// Generates the published Vim tips file from the category sources.
//
// tips/vim_tips_min.json is a generated artifact and is never authored by hand.
// It is always produced from the files in tips/categories/ by this script.
// CI runs it and commits the result so the published file can never drift from
// the sources, so day to day you only need to validate, not regenerate:
//   node scripts/generate-tips.mjs --check   # validate sources, write nothing
//   node scripts/generate-tips.mjs           # regenerate the artifact (CI / on request)

import { readdirSync, readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), "..");
const sourceDir = join(repoRoot, "tips", "categories");
const outputFile = join(repoRoot, "tips", "vim_tips_min.json");

// --check validates the sources without touching the published artifact, so
// editing tips never regenerates vim_tips_min.json by accident — CI owns that.
const checkOnly = process.argv.includes("--check");

// The non-Normal modes a tip may be tagged with; Normal is the untagged default and never stored.
const VALID_MODES = new Set(["insert", "visual", "command"]);

function fail(message) {
  console.error(`generate-tips: ${message}`);
  process.exit(1);
}

// Trim, drop blanks, and de-duplicate (preserving order).
function normalizeStrings(values) {
  const seen = new Set();
  const result = [];
  for (const value of values ?? []) {
    if (typeof value !== "string") continue;
    const trimmed = value.trim();
    if (trimmed === "" || seen.has(trimmed)) continue;
    seen.add(trimmed);
    result.push(trimmed);
  }
  return result;
}

// Config lines are written verbatim into .ideavimrc, so keep order and duplicates;
// only trim and drop blanks.
function normalizeConfigLines(values) {
  return (values ?? [])
    .map((l) => (typeof l === "string" ? l.trim() : ""))
    .filter(Boolean);
}

// Accepts the object form { name, lines } or the legacy array form ["line", ...].
// Returns the emitted config (object when named, array otherwise) or undefined when empty.
function normalizeConfig(config) {
  if (config == null) return undefined;
  if (Array.isArray(config)) {
    const lines = normalizeConfigLines(config);
    return lines.length > 0 ? lines : undefined;
  }
  if (typeof config === "object") {
    const lines = normalizeConfigLines(config.lines);
    if (lines.length === 0) return undefined;
    const name = typeof config.name === "string" ? config.name.trim() : "";
    return name ? { name, lines } : lines;
  }
  return undefined;
}

const sourceFiles = readdirSync(sourceDir)
  .filter((name) => name.endsWith(".json"))
  .map((name) => name.slice(0, -".json".length));

if (sourceFiles.length === 0) {
  fail(`no tip category source files found in ${sourceDir}`);
}

// Categories are emitted alphabetically; tip selection is random at runtime,
// so this order only affects how categories list in the settings UI.
const ordered = [...sourceFiles].sort();

const mergedTips = [];
const summarySources = new Map();

for (const category of ordered) {
  const fileName = `${category}.json`;
  let root;
  try {
    root = JSON.parse(readFileSync(join(sourceDir, fileName), "utf8"));
  } catch (error) {
    fail(`${fileName} is not valid JSON: ${error.message}`);
  }
  if (!Array.isArray(root?.tips)) {
    fail(`${fileName} must contain a tips array`);
  }

  root.tips.forEach((tip, index) => {
    if (tip === null || typeof tip !== "object") {
      fail(`tip ${index + 1} in ${fileName} must be a JSON object`);
    }
    const categories = normalizeStrings(tip.category);
    const summary = (typeof tip.summary === "string" ? tip.summary : "").trim();
    const details = normalizeStrings(tip.details);

    if (summary === "") fail(`tip ${index + 1} in ${fileName} has a blank summary`);
    if (details.length === 0) fail(`tip '${summary}' in ${fileName} has no details`);
    if (categories[0] !== category) {
      fail(`tip '${summary}' in ${fileName} must use '${category}' as its first category`);
    }
    const previous = summarySources.get(summary);
    if (previous !== undefined) {
      fail(`duplicate tip summary '${summary}' found in ${fileName} and ${previous}`);
    }
    summarySources.set(summary, fileName);

    const entry = { category: categories, summary, details };
    const mnemonic = typeof tip.mnemonic === "string" ? tip.mnemonic.trim() : "";
    if (mnemonic) entry.mnemonic = mnemonic;
    const config = normalizeConfig(tip.config);
    if (config) entry.config = config;
    // advanced is optional and defaults to normal; emit it only when true so the
    // published artifact stays minimal, and reject non-boolean values so sources
    // never carry a flag the runtime would silently ignore.
    if (tip.advanced !== undefined) {
      if (typeof tip.advanced !== "boolean") {
        fail(`tip '${summary}' in ${fileName} has a non-boolean advanced value`);
      }
      if (tip.advanced) entry.advanced = true;
    }
    // mode is optional and names the mode the reader presses the keys in (absent = Normal, which
    // is never labelled). Emit it only when set, and reject any value outside the known enum so a
    // typo can't ship a mode the runtime would silently drop.
    if (tip.mode !== undefined && tip.mode !== null) {
      if (!VALID_MODES.has(tip.mode)) {
        fail(
          `tip '${summary}' in ${fileName} has an invalid mode '${tip.mode}' (expected one of ${[...VALID_MODES].join(", ")})`
        );
      }
      entry.mode = tip.mode;
    }
    mergedTips.push(entry);
  });
}

// Escape non-ASCII characters as \uXXXX so the output is plain ASCII, matching
// the previous generator's encoding.
const raw = JSON.stringify({ tips: mergedTips });
let json = "";
for (let i = 0; i < raw.length; i++) {
  const code = raw.charCodeAt(i);
  json += code > 0x7e ? "\\u" + code.toString(16).padStart(4, "0") : raw[i];
}

if (checkOnly) {
  console.log(`generate-tips: validated ${mergedTips.length} tips (--check, no file written)`);
  process.exit(0);
}

mkdirSync(dirname(outputFile), { recursive: true });
writeFileSync(outputFile, json, "utf8");
console.log(`generate-tips: wrote ${mergedTips.length} tips to ${outputFile}`);
