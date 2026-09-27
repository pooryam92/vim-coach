// Keys in tip sources are wrapped in «…» so the balloon can style them. Every printable
// ASCII character is a Vim key, so the markers have to be non-ASCII.
export const KEY_OPEN = "«";
export const KEY_CLOSE = "»";

/**
 * Splits marked source text into the plain text readers see and the [start, end) spans of its
 * keys, as UTF-16 offsets into the plain text (the same units Kotlin strings index by).
 * Throws with a readable reason when the markers are malformed.
 */
export function parseKeyMarkers(marked) {
  let text = "";
  let keyStart = -1;
  const spans = [];
  for (const ch of marked) {
    if (ch === KEY_OPEN) {
      if (keyStart !== -1) throw new Error(`nested ${KEY_OPEN} in '${marked}'`);
      keyStart = text.length;
    } else if (ch === KEY_CLOSE) {
      if (keyStart === -1) throw new Error(`${KEY_CLOSE} without a matching ${KEY_OPEN} in '${marked}'`);
      const key = text.slice(keyStart);
      if (key.trim() === "" || key.trim() !== key) {
        throw new Error(`blank key or key with surrounding spaces ${KEY_OPEN}${key}${KEY_CLOSE} in '${marked}'`);
      }
      spans.push([keyStart, text.length]);
      keyStart = -1;
    } else {
      text += ch;
    }
  }
  if (keyStart !== -1) throw new Error(`unclosed ${KEY_OPEN} in '${marked}'`);
  return { text, spans };
}

export function hasKeyMarker(text) {
  return text.includes(KEY_OPEN) || text.includes(KEY_CLOSE);
}

export function stripKeyMarkers(marked) {
  return marked.replaceAll(KEY_OPEN, "").replaceAll(KEY_CLOSE, "");
}
