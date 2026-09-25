#!/usr/bin/env node
// Advisory lint for the tip sources — the soft, eyeball-it checks that
// generate-tips.mjs deliberately does NOT enforce.
//
// generate-tips.mjs owns the hard rules (source shape, categories, duplicate
// summaries and details, Plug lines tagged plugins) and FAILS the build on them.
// Length and wording stay here on purpose: they are judgment calls. This script
// never gates anything: it prints a review report and exits 0. Run it
// before/after editing tips/categories/*.json to catch the things a human
// would otherwise have to scan for by hand.
//
//   node scripts/lint-tips.mjs

import { spawnSync } from "node:child_process";
import { readdirSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), "..");
const sourceDir = join(repoRoot, "tips", "categories");

// The balloon grows to ~300px for its longest line, but one line past ~43 chars
// clamps the whole body to 240px (~35 chars), wrapping every long line in it.
// Both thresholds are estimates for a 13px font. Tune here if the skill's
// guidance changes.
const SUMMARY_MAX = 35;
const DETAIL_MAX = 35;
const DETAIL_CLAMP = 43;
// Past this many details the balloon stops being glanceable.
const DETAILS_MAX_COUNT = 3;
const FILLER_OPENER = /^(Useful|Handy|Use it|Good for|Great)\b/;
// "{ / }" reads as a pileup; symbol pairs join with "and".
const SYMBOL_SLASH = /(?:^|\s)([^\sA-Za-z0-9]+) \/ ([^\sA-Za-z0-9]+)(?=\s|$)/;
const isNumberedSteps = (details) => /^1\.\s/.test(details[0] ?? "") && /^2\.\s/.test(details[1] ?? "");
const escapeRegex = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

// True when `text` contains `keys` as a standalone run, not inside a word.
function containsKeys(text, keys) {
  return new RegExp(`(?<![A-Za-z0-9])${escapeRegex(keys)}(?![A-Za-z0-9])`).test(text);
}

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
const clampingDetails = [];
const longDetails = [];
const tooManyDetails = [];
const separators = [];
const symbolSlashes = [];
const fillerOpeners = [];
const restatedKeys = [];
const allTips = [];

for (const file of files) {
  const { tips } = JSON.parse(readFileSync(join(sourceDir, file), "utf8"));
  for (const tip of tips) {
    const s = tip.summary;

    if (s.length > SUMMARY_MAX) longSummaries.push([s.length, file, s]);

    const details = tip.details ?? [];
    for (const d of details) {
      if (d.length > DETAIL_CLAMP) clampingDetails.push([d.length, file, d]);
      else if (d.length > DETAIL_MAX) longDetails.push([d.length, file, d]);
      if (FILLER_OPENER.test(d)) fillerOpeners.push([file, d]);
    }
    for (const line of [s, ...details]) {
      if (SYMBOL_SLASH.test(line)) symbolSlashes.push([file, line]);
    }

    if (details.length > DETAILS_MAX_COUNT && !isNumberedSteps(details)) {
      tooManyDetails.push([details.length, file, s]);
    }

    // Keys must attach with a plain space, never a separator. The `-` case
    // false-positives when a dash is part of the keys (Ctrl-w), so flag for
    // eyeballing, not as error. (`:` is skipped — it's usually the prompt being
    // taught, not a separator.) A trailing `(dw)` is a separator; `( and )` is
    // the keys themselves.
    if (/\s-\s/.test(s) || /\s→\s/.test(s) || /\s\([^\s()]+\)\s*$/.test(s)) {
      separators.push([file, s]);
    }

    const keySig = keySignature(s);
    if (keySig && details[0] && containsKeys(details[0], keySig)) {
      restatedKeys.push([file, s, details[0]]);
    }

    allTips.push({
      file,
      isPlugin: file === "plugins.json",
      summary: s,
      keySig,
      words: topicWords(s),
      config: new Set(configLines(tip)),
      details: new Set(details.map((d) => d.trim())),
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
clampingDetails.sort((a, b) => b[0] - a[0]);
longDetails.sort((a, b) => b[0] - a[0]);
tooManyDetails.sort((a, b) => b[0] - a[0]);

section(`Summaries over ${SUMMARY_MAX} chars`, longSummaries, ([n, f, s]) =>
  `${String(n).padEnd(3)} ${f.replace(".json", "").padEnd(12)} ${JSON.stringify(s)}`,
);
const lengthRow = ([n, f, text]) => `${String(n).padEnd(3)} ${f.replace(".json", "").padEnd(12)} ${JSON.stringify(text)}`;
const fileRow = ([f, text]) => `${f.replace(".json", "").padEnd(12)} ${JSON.stringify(text)}`;

section(
  `Details over ~${DETAIL_CLAMP} chars (approx.; clamps the whole balloon to 240px, fix first)`,
  clampingDetails,
  lengthRow,
);
section(
  `Details of ~${DETAIL_MAX + 1}-${DETAIL_CLAMP} chars (approx.; wrap once the balloon is clamped)`,
  longDetails,
  lengthRow,
);
section(`Tips with more than ${DETAILS_MAX_COUNT} details (numbered steps exempt)`, tooManyDetails, lengthRow);
section("Possible stray separators in summaries (eyeball — `-` may be part of keys)", separators, fileRow);
section('Slash between symbol keys (join symbol pairs with "and": { and })', symbolSlashes, fileRow);
section("Details opening with filler (Useful/Handy/Use it/Good for/Great)", fillerOpeners, fileRow);
section("First detail repeats the summary's keys (spend the line on what they do)", restatedKeys, ([f, s, d]) =>
  `${f.replace(".json", "").padEnd(12)} ${JSON.stringify(s)}\n      ${JSON.stringify(d)}`,
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

// Tips sharing two or more detail lines usually teach the same thing twice, or
// copy-pasted lines that fit only one of them.
const sharedDetails = [];
for (let i = 0; i < allTips.length; i++) {
  for (let j = i + 1; j < allTips.length; j++) {
    const shared = [...allTips[i].details].filter((d) => allTips[j].details.has(d));
    if (shared.length >= 2) sharedDetails.push([shared, allTips[i], allTips[j]]);
  }
}
section("Tips sharing 2+ detail lines", sharedDetails, ([shared, a, b]) =>
  `${a.file.replace(".json", "")}: ${JSON.stringify(a.summary)}\n      ` +
  `${b.file.replace(".json", "")}: ${JSON.stringify(b.summary)}\n      ` +
  shared.map((d) => JSON.stringify(d)).join("\n      "),
);

// The hide key hashes the trimmed summary, so every summary that disappears
// relative to the committed artifact resets that tip's hide for its users.
function publishedSummaries() {
  const result = spawnSync("git", ["show", "HEAD:tips/vim_tips_min.json"], {
    cwd: repoRoot,
    encoding: "utf8",
    maxBuffer: 64 * 1024 * 1024,
  });
  if (result.error) return { skipped: `git unavailable (${result.error.message})` };
  if (result.status !== 0) return { skipped: (result.stderr || "git show failed").trim() };
  try {
    return { tips: JSON.parse(result.stdout).tips ?? [] };
  } catch (error) {
    return { skipped: `HEAD:tips/vim_tips_min.json is not valid JSON (${error.message})` };
  }
}

const published = publishedSummaries();
const resetTitle = "Summaries renamed or removed vs HEAD:tips/vim_tips_min.json (each resets a hide)";
if (published.skipped) {
  console.log(`\n${resetTitle}\n  skipped: ${published.skipped}`);
} else {
  const current = new Set(allTips.map((t) => t.summary.trim()));
  const oldSummaries = new Set(published.tips.map((t) => (t.summary ?? "").trim()));
  const added = allTips.filter((t) => !oldSummaries.has(t.summary.trim()));
  const gone = published.tips.filter((t) => !current.has((t.summary ?? "").trim()));
  // Best-guess rename target: a new summary with the same key signature, or one
  // sharing 2+ topic words (modifier names like "ctrl" carry no topic).
  const MODIFIER_WORDS = new Set(["ctrl", "shift", "alt"]);
  const likelyRename = (old) => {
    const sig = keySignature(old);
    const words = [...topicWords(old)].filter((w) => !MODIFIER_WORDS.has(w));
    let best = null;
    let bestScore = 1;
    for (const t of added) {
      const score = (sig && sig === t.keySig ? 10 : 0) + words.filter((w) => t.words.has(w)).length;
      if (score > bestScore) [best, bestScore] = [t, score];
    }
    return best;
  };
  section(resetTitle, gone, (old) => {
    const guess = likelyRename(old.summary ?? "");
    return `${(old.category?.[0] ?? "?").padEnd(12)} ${JSON.stringify(old.summary)}` +
      (guess ? `  -> maybe ${JSON.stringify(guess.summary)}` : "");
  });
}

console.log("\nlint-tips: advisory only — generate-tips.mjs owns the hard rules.");
