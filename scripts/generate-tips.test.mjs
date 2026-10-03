import { test } from "node:test";
import assert from "node:assert/strict";
import { spawnSync } from "node:child_process";
import { copyFileSync, mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const scriptsDir = dirname(fileURLToPath(import.meta.url));

// The generator resolves tips/categories relative to its own path, so it runs from a copy
// beside a throwaway source tree.
function checkSources(tips) {
  const root = mkdtempSync(join(tmpdir(), "generate-tips-"));
  try {
    mkdirSync(join(root, "scripts"));
    for (const file of ["generate-tips.mjs", "tip-id.mjs"]) {
      copyFileSync(join(scriptsDir, file), join(root, "scripts", file));
    }
    mkdirSync(join(root, "tips", "categories"), { recursive: true });
    writeFileSync(join(root, "tips", "categories", "basics.json"), JSON.stringify({ tips }));
    return spawnSync(process.execPath, [join(root, "scripts", "generate-tips.mjs"), "--check"], {
      encoding: "utf8",
    });
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
}

const basicsTip = (summary) => ({ category: ["basics"], summary, details: ["detail"] });

test("tips whose ids collide fail generation", () => {
  const result = checkSources([basicsTip("Tip number 21367"), basicsTip("Tip number 29288")]);

  assert.equal(result.status, 1);
  assert.match(result.stderr, /same id '87323a08'/);
});

test("tips with distinct ids pass", () => {
  const result = checkSources([basicsTip("Tip number 21367"), basicsTip("Tip number 1")]);

  assert.equal(result.status, 0, result.stderr);
});
