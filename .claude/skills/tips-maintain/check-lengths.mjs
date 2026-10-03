#!/usr/bin/env node
// Lists tip lines longer than the measured balloon limits (see balloon.md).
// Advisory only: always exits 0 — length never gates the build.
//
//   node .claude/skills/tips-maintain/check-lengths.mjs

import { readdirSync, readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const LIMITS = { summary: 42, detail: 42, mnemonic: 32 };
const repoRoot = join(dirname(fileURLToPath(import.meta.url)), '..', '..', '..');
const dir = join(repoRoot, 'tips', 'categories');

const hits = [];
for (const file of readdirSync(dir).filter((f) => f.endsWith('.json')).sort()) {
  for (const tip of JSON.parse(readFileSync(join(dir, file), 'utf8')).tips) {
    const lines = [
      ['summary', tip.summary],
      ...(tip.details ?? []).map((d) => ['detail', d]),
      ['mnemonic', tip.mnemonic],
    ];
    for (const [kind, text] of lines) {
      if (typeof text === 'string' && text.length > LIMITS[kind]) {
        hits.push(`${file}  ${kind} ${text.length}/${LIMITS[kind]}  "${text}"  (tip: ${tip.summary})`);
      }
    }
  }
}

if (hits.length === 0) {
  console.log('check-lengths: every line is within its limit.');
} else {
  console.log(`check-lengths: ${hits.length} line(s) over the limit (advisory):\n`);
  for (const hit of hits) console.log(`  ${hit}`);
}
