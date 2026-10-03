// A tip's id: its identity for rotation progress and exclusions in the plugin.
// Changing this formula resets every user's exclusions and rotation; the golden
// value in tip-id.test.mjs guards it.

import { createHash } from "node:crypto";

const ID_LENGTH = 8;

// Length-prefixing each field keeps boundaries, so "ab"+"c" and "a"+"bc" differ.
export function tipId(summary, details) {
  const fields = [`s${summary.length}:${summary}`, ...details.map((d) => `d${d.length}:${d}`)];
  return createHash("sha256").update(fields.join(""), "utf8").digest("hex").slice(0, ID_LENGTH);
}
