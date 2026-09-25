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

// The source fields a tip may carry. Sources are validated strictly so a typo'd
// key fails here instead of being silently dropped; the runtime TipJsonParser
// stays lenient on purpose (forward compatibility with newer published files).
const ALLOWED_KEYS = new Set(["category", "summary", "details", "advanced", "mode", "config"]);
const MAX_CATEGORIES = 3;

// Config lines are written verbatim into .ideavimrc, so keep order and duplicates;
// only trim and drop blanks.
function normalizeConfigLines(lines) {
  return lines.map((l) => l.trim()).filter(Boolean);
}

// Accepts the object form { name?, lines } or the legacy array form ["line", ...].
// Returns the emitted config (object when named, array otherwise); fails on any
// other shape, a non-string line or name, or a config with no lines.
function normalizeConfig(config, where) {
  const named = config !== null && typeof config === "object" && !Array.isArray(config);
  const lines = named ? config.lines : config;
  if (!Array.isArray(lines)) {
    fail(`${where} has a config without a lines array (expected { name, lines } or ["line", ...])`);
  }
  if (lines.some((l) => typeof l !== "string")) fail(`${where} has a non-string config line`);
  const normalized = normalizeConfigLines(lines);
  if (normalized.length === 0) fail(`${where} has a config with no lines`);
  if (!named) return normalized;
  const unknown = Object.keys(config).filter((k) => k !== "name" && k !== "lines");
  if (unknown.length > 0) fail(`${where} has unknown config key(s): ${unknown.join(", ")}`);
  if (config.name === undefined) return normalized;
  if (typeof config.name !== "string") fail(`${where} has a non-string config name`);
  const name = config.name.trim();
  return name ? { name, lines: normalized } : normalized;
}

function requireStringArray(value, field, where) {
  if (!Array.isArray(value)) fail(`${where} must have a ${field} array`);
  if (value.some((v) => typeof v !== "string")) fail(`${where} has a non-string ${field} entry`);
  return value.map((v) => v.trim()).filter(Boolean);
}

const sourceFiles = readdirSync(sourceDir)
  .filter((name) => name.endsWith(".json"))
  .map((name) => name.slice(0, -".json".length));

if (sourceFiles.length === 0) {
  fail(`no tip category source files found in ${sourceDir}`);
}

const knownCategories = new Set(sourceFiles);

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
    if (tip === null || typeof tip !== "object" || Array.isArray(tip)) {
      fail(`tip ${index + 1} in ${fileName} must be a JSON object`);
    }
    const summary = (typeof tip.summary === "string" ? tip.summary : "").trim();
    if (summary === "") fail(`tip ${index + 1} in ${fileName} has a blank or missing summary`);
    const where = `tip '${summary}' in ${fileName}`;

    const unknownKeys = Object.keys(tip).filter((key) => !ALLOWED_KEYS.has(key));
    if (unknownKeys.length > 0) {
      fail(`${where} has unknown key(s): ${unknownKeys.join(", ")} (allowed: ${[...ALLOWED_KEYS].join(", ")})`);
    }

    const categories = [...new Set(requireStringArray(tip.category, "category", where))];
    if (categories[0] !== category) {
      fail(`${where} must use '${category}' as its first category`);
    }
    if (categories.length > MAX_CATEGORIES) {
      fail(`${where} has ${categories.length} categories (at most ${MAX_CATEGORIES})`);
    }
    const unknownCategories = categories.filter((c) => !knownCategories.has(c));
    if (unknownCategories.length > 0) {
      fail(`${where} uses unknown category '${unknownCategories.join("', '")}' (no tips/categories/<name>.json)`);
    }

    const details = requireStringArray(tip.details, "details", where);
    if (details.length === 0) fail(`${where} has no details`);
    const repeated = details.find((d, i) => details.indexOf(d) !== i);
    if (repeated !== undefined) fail(`${where} repeats the detail '${repeated}'`);

    const previous = summarySources.get(summary);
    if (previous !== undefined) {
      fail(`duplicate tip summary '${summary}' found in ${fileName} and ${previous}`);
    }
    summarySources.set(summary, fileName);

    const entry = { category: categories, summary, details };
    if (tip.config !== undefined && tip.config !== null) {
      const config = normalizeConfig(tip.config, where);
      const configLines = Array.isArray(config) ? config : config.lines;
      // Plug lines install a plugin, and plugin tips must be findable under plugins.
      if (configLines.some((l) => /^Plug\s/.test(l)) && !categories.includes("plugins")) {
        fail(`${where} has a Plug config line but no 'plugins' category`);
      }
      entry.config = config;
    }
    // advanced is optional and defaults to normal; emit it only when true so the
    // published artifact stays minimal, and reject non-boolean values so sources
    // never carry a flag the runtime would silently ignore.
    if (tip.advanced !== undefined) {
      if (typeof tip.advanced !== "boolean") {
        fail(`${where} has a non-boolean advanced value`);
      }
      if (tip.advanced) entry.advanced = true;
    }
    // mode is optional and names the mode the reader presses the keys in (absent = Normal, which
    // is never labelled). Emit it only when set, and reject any value outside the known enum so a
    // typo can't ship a mode the runtime would silently drop.
    if (tip.mode !== undefined && tip.mode !== null) {
      if (!VALID_MODES.has(tip.mode)) {
        fail(
          `${where} has an invalid mode '${tip.mode}' (expected one of ${[...VALID_MODES].join(", ")})`
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
