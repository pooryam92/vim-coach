#!/usr/bin/env node
// Advisory lint for the tip sources — the soft, eyeball-it checks that
// generate-tips.mjs deliberately does NOT enforce.
//
// generate-tips.mjs owns the hard rules (blank summary, no details, wrong
// primary category, duplicate summaries) and FAILS the build on them. This
// script never gates anything: it prints a review report and exits 0. Run it
// before/after editing tips/categories/*.json to catch the things a human
// would otherwise have to scan for by hand.
//
//   node scripts/lint-tips.mjs

import { readdirSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), "..");
const sourceDir = join(repoRoot, "tips", "categories");

// A summary must fit one line in the ~240px balloon. Details may wrap to ~2
// lines, so they get a looser cap. Tune here if the skill's guidance changes.
const SUMMARY_MAX = 35;
const DETAIL_MAX = 68;

// Tokens that look like English words rather than Vim keys, so the duplicate
// heuristic stops consuming the trailing key-run at them.
function looksLikeKey(token) {
  if (token === "/") return true;
  if (/[^a-z]/.test(token)) return true; // has uppercase, digit, or punctuation
  return token.length <= 3; // short all-lowercase tokens are usually keys (gt, zz, p)
}

// The trailing run of key-ish tokens in a summary — its rough "command
// signature". Two tips that share one are likely the same key taught twice.
function keySignature(summary) {
  const tokens = summary.trim().split(/\s+/);
  const run = [];
  for (let i = tokens.length - 1; i >= 0; i--) {
    if (!looksLikeKey(tokens[i])) break;
    run.unshift(tokens[i]);
  }
  return run.join(" ");
}

const files = readdirSync(sourceDir).filter((n) => n.endsWith(".json")).sort();

const longSummaries = [];
const longDetails = [];
const separators = [];
const legacyConfig = [];
const sigGroups = new Map();

for (const file of files) {
  const category = file.slice(0, -".json".length);
  const { tips } = JSON.parse(readFileSync(join(sourceDir, file), "utf8"));
  for (const tip of tips) {
    const s = tip.summary;

    if (s.length > SUMMARY_MAX) longSummaries.push([s.length, file, s]);

    for (const d of tip.details ?? []) {
      if (d.length > DETAIL_MAX) longDetails.push([d.length, file, d]);
    }

    // Keys must attach with a plain space, never a separator. The `-` case
    // false-positives when a dash is part of the keys (Ctrl-w), so flag for
    // eyeballing, not as error. (`:` is skipped — it's usually the prompt being
    // taught, not a separator.)
    if (/\s-\s/.test(s) || /\s→\s/.test(s) || /\([^)]*\)\s*$/.test(s)) {
      separators.push([file, s]);
    }

    // Object form { name, lines } renders a labelled button; the legacy array
    // form falls back to a generic "Apply".
    if (Array.isArray(tip.config)) legacyConfig.push([file, s]);

    const sig = keySignature(s);
    if (sig) {
      if (!sigGroups.has(sig)) sigGroups.set(sig, []);
      sigGroups.get(sig).push([file, s]);
    }
  }
}

function section(title, rows, render) {
  console.log(`\n${title} (${rows.length})`);
  if (rows.length === 0) {
    console.log("  none");
    return;
  }
  for (const row of rows) console.log("  " + render(row));
}

longSummaries.sort((a, b) => b[0] - a[0]);
longDetails.sort((a, b) => b[0] - a[0]);

section(`Summaries over ${SUMMARY_MAX} chars`, longSummaries, ([n, f, s]) =>
  `${String(n).padEnd(3)} ${f.replace(".json", "").padEnd(12)} ${JSON.stringify(s)}`,
);
section(`Details over ${DETAIL_MAX} chars (won't fit ~2 wrapped lines)`, longDetails, ([n, f, d]) =>
  `${String(n).padEnd(3)} ${f.replace(".json", "").padEnd(12)} ${JSON.stringify(d)}`,
);
section("Possible stray separators in summaries (eyeball — `-` may be part of keys)", separators, ([f, s]) =>
  `${f.replace(".json", "").padEnd(12)} ${JSON.stringify(s)}`,
);
section("Legacy array config form (no button label)", legacyConfig, ([f, s]) =>
  `${f.replace(".json", "").padEnd(12)} ${JSON.stringify(s)}`,
);

const dups = [...sigGroups.entries()].filter(([, g]) => g.length > 1);
section("Possible duplicate keys taught twice (eyeball — some repeat legitimately)", dups, ([sig, g]) =>
  `${JSON.stringify(sig)}\n      ` + g.map(([f, s]) => `${f.replace(".json", "")}: ${JSON.stringify(s)}`).join("\n      "),
);

console.log("\nlint-tips: advisory only — generate-tips.mjs owns the hard rules.");
