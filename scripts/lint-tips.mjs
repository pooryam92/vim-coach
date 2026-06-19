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

// Stopwords that carry no topic signal, so word-overlap ignores them.
const STOP = new Set(
  "a an the to of in on at for with and or is it as be by from your you".split(" "),
);

// The "meaning" words in a summary — lowercased, stopwords and short/key-ish
// tokens dropped. Used to spot duplicates the keySignature can't (two tips that
// teach the same thing under different keys/wording).
function topicWords(summary) {
  return new Set(
    summary
      .toLowerCase()
      .split(/[^a-z]+/)
      .filter((w) => w.length > 2 && !STOP.has(w)),
  );
}

function configLines(tip) {
  const c = tip.config;
  const lines = Array.isArray(c) ? c : c?.lines ?? [];
  return lines.map((l) => l.trim());
}

const files = readdirSync(sourceDir).filter((n) => n.endsWith(".json")).sort();

const longSummaries = [];
const longDetails = [];
const separators = [];
const legacyConfig = [];
const allTips = [];

for (const file of files) {
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

    allTips.push({
      file,
      isPlugin: file === "plugins.json",
      summary: s,
      keySig: keySignature(s),
      words: topicWords(s),
      config: new Set(configLines(tip)),
    });
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

// Possible duplicate TIPS — the same behavior taught twice, which the generator
// misses when summaries differ. Two precise signals (word-overlap alone is too
// noisy — intentional siblings like diw/ciw reuse words and even details):
//   1. a shared config line OUTSIDE plugins — near-certain duplication on its
//      own. Plugin tips share an enable line (`Plug '...'` or `set classtextobj`)
//      by design, so config overlap among them is ignored.
//   2. the same keySignature PLUS ≥2 shared topic words — same keys, same topic.
function intersects(a, b) {
  for (const x of a) if (b.has(x)) return true;
  return false;
}
const dupPairs = [];
for (let i = 0; i < allTips.length; i++) {
  for (let j = i + 1; j < allTips.length; j++) {
    const a = allTips[i];
    const b = allTips[j];
    const sharedWords = [...a.words].filter((w) => b.words.has(w));

    let reason = null;
    if (!a.isPlugin && !b.isPlugin && intersects(a.config, b.config)) {
      reason = "same config line";
    } else if (a.keySig && a.keySig === b.keySig && sharedWords.length >= 2) {
      reason = `shares "${a.keySig}" + words`;
    }
    if (reason) dupPairs.push([reason, sharedWords, a, b]);
  }
}
section("Possible duplicate tips (eyeball — some repeat legitimately)", dupPairs, ([reason, words, a, b]) =>
  `${reason}${words.length ? ` [${words.join(", ")}]` : ""}\n      ` +
  `${a.file.replace(".json", "")}: ${JSON.stringify(a.summary)}\n      ` +
  `${b.file.replace(".json", "")}: ${JSON.stringify(b.summary)}`,
);

console.log("\nlint-tips: advisory only — generate-tips.mjs owns the hard rules.");
