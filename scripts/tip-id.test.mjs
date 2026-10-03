import { test } from "node:test";
import assert from "node:assert/strict";
import { tipId } from "./tip-id.mjs";

const summary = "Surround a word";
const details = ['ysiw" wraps the word in quotes', 'ds" removes them', "Works on any text object — café"];

// A changed golden value resets every user's exclusions and rotation; note it in the CHANGELOG.
test("id definition is pinned", () => {
  assert.equal(tipId(summary, details), "14ec7dc7");
});

test("id is eight lowercase hex characters", () => {
  assert.match(tipId("x", ["y"]), /^[0-9a-f]{8}$/);
});

test("editing the summary or a detail changes the id", () => {
  assert.notEqual(tipId(summary, details), tipId("Surround a word with quotes", details));
  assert.notEqual(tipId(summary, details), tipId(summary, details.slice(0, -1)));
});

test("id keeps field boundaries", () => {
  assert.notEqual(tipId("ab", ["c"]), tipId("a", ["bc"]));
  assert.notEqual(tipId("s", ["ab", "c"]), tipId("s", ["a", "bc"]));
  assert.notEqual(tipId("adx", ["y"]), tipId("a", ["x", "y"]));
});
