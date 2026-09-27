// node --test scripts/tip-keys.test.mjs
import { test } from "node:test";
import assert from "node:assert/strict";
import { hasKeyMarker, parseKeyMarkers, stripKeyMarkers } from "./tip-keys.mjs";

test("unmarked text passes through with no keys", () => {
  assert.deepEqual(parseKeyMarkers("Grow or trim the start"), { text: "Grow or trim the start", spans: [] });
});

test("keys become spans over the unmarked text", () => {
  const { text, spans } = parseKeyMarkers("«Ctrl-n» down, «Ctrl-p» up");
  assert.equal(text, "Ctrl-n down, Ctrl-p up");
  assert.deepEqual(spans.map(([s, e]) => text.slice(s, e)), ["Ctrl-n", "Ctrl-p"]);
});

test("a key may hold spaces, quotes and symbols", () => {
  const { text, spans } = parseKeyMarkers('«"ayy» then «g Ctrl-a» and «:%s/a/b/g»');
  assert.deepEqual(spans.map(([s, e]) => text.slice(s, e)), ['"ayy', "g Ctrl-a", ":%s/a/b/g"]);
});

test("offsets count UTF-16 units like Kotlin strings", () => {
  const { text, spans } = parseKeyMarkers("ä → «x»");
  assert.deepEqual(spans, [[4, 5]]);
  assert.equal(text.slice(4, 5), "x");
});

test("malformed markers throw", () => {
  for (const bad of ["«dd", "dd»", "««dd»»", "«»", "« dd»", "«dd »"]) {
    assert.throws(() => parseKeyMarkers(bad), undefined, bad);
  }
});

test("stripKeyMarkers drops only the markers", () => {
  assert.equal(stripKeyMarkers("«p» / «P» keeps «»"), "p / P keeps ");
});

test("hasKeyMarker spots either marker alone", () => {
  assert.equal(hasKeyMarker("set surround"), false);
  assert.equal(hasKeyMarker("«set surround"), true);
  assert.equal(hasKeyMarker("set surround»"), true);
});
