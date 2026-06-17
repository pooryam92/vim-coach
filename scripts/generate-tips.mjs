#!/usr/bin/env node
// Generates the published Vim tips file from the category sources.
//
// tips/vim_tips_min.json is a generated artifact and is never authored by hand.
// It is always produced from the files in tips/categories/ by this script.
// Run it directly (`node scripts/generate-tips.mjs`); CI also runs it and
// commits the result so the published file can never drift from the sources.

import { readdirSync, readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), "..");
const sourceDir = join(repoRoot, "tips", "categories");
const outputFile = join(repoRoot, "tips", "vim_tips_min.json");

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
    const config = (tip.config ?? []).map((l) => (typeof l === "string" ? l.trim() : "")).filter(Boolean);
    if (config.length > 0) entry.config = config;
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

mkdirSync(dirname(outputFile), { recursive: true });
writeFileSync(outputFile, json, "utf8");
console.log(`generate-tips: wrote ${mergedTips.length} tips to ${outputFile}`);
