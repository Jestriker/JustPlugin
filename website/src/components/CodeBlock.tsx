"use client";

import { useState, useMemo } from "react";

// ── Lightweight syntax highlighter ──────────────────────────────────
// No external deps — pattern-based tokenisation for Java, YAML, Groovy, properties

type Token = { text: string; cls: string };

function highlightJava(code: string): Token[] {
  const keywords = new Set([
    "abstract","assert","boolean","break","byte","case","catch","char","class",
    "const","continue","default","do","double","else","enum","extends","final",
    "finally","float","for","goto","if","implements","import","instanceof","int",
    "interface","long","native","new","package","private","protected","public",
    "return","short","static","strictfp","super","switch","synchronized","this",
    "throw","throws","transient","try","void","volatile","while","var","record",
    "sealed","permits","yield",
  ]);
  return tokenize(code, keywords, { lineComments: "//", blockComments: ["/*", "*/"] });
}

function highlightYaml(code: string): Token[] {
  const tokens: Token[] = [];
  const lines = code.split("\n");
  for (let i = 0; i < lines.length; i++) {
    if (i > 0) tokens.push({ text: "\n", cls: "" });
    const line = lines[i];
    // Comment
    const trimmed = line.trimStart();
    if (trimmed.startsWith("#")) {
      const indent = line.length - trimmed.length;
      if (indent > 0) tokens.push({ text: line.slice(0, indent), cls: "" });
      tokens.push({ text: trimmed, cls: "syn-cmt" });
      continue;
    }
    // Key: value
    const colonMatch = line.match(/^(\s*)([\w\-.<>]+)(\s*:\s*)(.*)/);
    if (colonMatch) {
      const [, indent, key, colon, value] = colonMatch;
      if (indent) tokens.push({ text: indent, cls: "" });
      tokens.push({ text: key, cls: "syn-prop" });
      tokens.push({ text: colon, cls: "syn-punc" });
      if (value) {
        // Check for inline comment
        const commentIdx = findYamlComment(value);
        const actualValue = commentIdx >= 0 ? value.slice(0, commentIdx) : value;
        const comment = commentIdx >= 0 ? value.slice(commentIdx) : "";
        highlightYamlValue(actualValue, tokens);
        if (comment) tokens.push({ text: comment, cls: "syn-cmt" });
      }
      continue;
    }
    // List item
    const listMatch = line.match(/^(\s*)(- )(.*)/);
    if (listMatch) {
      const [, indent, dash, value] = listMatch;
      if (indent) tokens.push({ text: indent, cls: "" });
      tokens.push({ text: dash, cls: "syn-op" });
      highlightYamlValue(value, tokens);
      continue;
    }
    tokens.push({ text: line, cls: "" });
  }
  return tokens;
}

function findYamlComment(value: string): number {
  let inStr = false;
  let strChar = "";
  for (let i = 0; i < value.length; i++) {
    const ch = value[i];
    if (inStr) {
      if (ch === strChar) inStr = false;
    } else {
      if (ch === '"' || ch === "'") { inStr = true; strChar = ch; }
      if (ch === "#" && (i === 0 || value[i - 1] === " ")) return i;
    }
  }
  return -1;
}

function highlightYamlValue(value: string, tokens: Token[]) {
  const trimmed = value.trim();
  if (!trimmed) { if (value) tokens.push({ text: value, cls: "" }); return; }
  if (/^(true|false|yes|no|on|off|null|~)$/i.test(trimmed)) {
    tokens.push({ text: value, cls: "syn-bool" });
  } else if (/^-?\d+(\.\d+)?$/.test(trimmed)) {
    tokens.push({ text: value, cls: "syn-num" });
  } else if (/^["']/.test(trimmed)) {
    tokens.push({ text: value, cls: "syn-str" });
  } else {
    tokens.push({ text: value, cls: "syn-str" });
  }
}

function highlightGroovy(code: string): Token[] {
  const keywords = new Set([
    "abstract","as","assert","boolean","break","byte","case","catch","char",
    "class","const","continue","def","default","do","double","else","enum",
    "extends","final","finally","float","for","goto","if","implements","import",
    "in","instanceof","int","interface","long","native","new","package","private",
    "protected","public","return","short","static","super","switch","synchronized",
    "this","throw","throws","transient","try","void","volatile","while",
    "true","false","null",
  ]);
  return tokenize(code, keywords, { lineComments: "//", blockComments: ["/*", "*/"] });
}

// Generic C-style tokenizer
function tokenize(
  code: string,
  keywords: Set<string>,
  opts: { lineComments: string; blockComments: [string, string] }
): Token[] {
  const tokens: Token[] = [];
  let i = 0;
  while (i < code.length) {
    // Block comment
    if (code.startsWith(opts.blockComments[0], i)) {
      const end = code.indexOf(opts.blockComments[1], i + 2);
      const endIdx = end >= 0 ? end + opts.blockComments[1].length : code.length;
      tokens.push({ text: code.slice(i, endIdx), cls: "syn-cmt" });
      i = endIdx;
      continue;
    }
    // Line comment
    if (code.startsWith(opts.lineComments, i)) {
      const end = code.indexOf("\n", i);
      const endIdx = end >= 0 ? end : code.length;
      tokens.push({ text: code.slice(i, endIdx), cls: "syn-cmt" });
      i = endIdx;
      continue;
    }
    // String (double)
    if (code[i] === '"') {
      let j = i + 1;
      while (j < code.length && code[j] !== '"') { if (code[j] === "\\") j++; j++; }
      tokens.push({ text: code.slice(i, j + 1), cls: "syn-str" });
      i = j + 1;
      continue;
    }
    // String (single)
    if (code[i] === "'") {
      let j = i + 1;
      while (j < code.length && code[j] !== "'") { if (code[j] === "\\") j++; j++; }
      tokens.push({ text: code.slice(i, j + 1), cls: "syn-str" });
      i = j + 1;
      continue;
    }
    // Number
    if (/\d/.test(code[i]) && (i === 0 || /[\s(,=+\-*/<>!&|^~%]/.test(code[i - 1]))) {
      let j = i;
      while (j < code.length && /[\d._xXaAbBcCdDeEfFLl]/.test(code[j])) j++;
      tokens.push({ text: code.slice(i, j), cls: "syn-num" });
      i = j;
      continue;
    }
    // Annotation
    if (code[i] === "@" && /[A-Za-z]/.test(code[i + 1] || "")) {
      let j = i + 1;
      while (j < code.length && /[\w]/.test(code[j])) j++;
      tokens.push({ text: code.slice(i, j), cls: "syn-type" });
      i = j;
      continue;
    }
    // Word
    if (/[A-Za-z_$]/.test(code[i])) {
      let j = i;
      while (j < code.length && /[\w$]/.test(code[j])) j++;
      const word = code.slice(i, j);
      if (keywords.has(word)) {
        tokens.push({ text: word, cls: "syn-kw" });
      } else if (word === "true" || word === "false") {
        tokens.push({ text: word, cls: "syn-bool" });
      } else if (word === "null") {
        tokens.push({ text: word, cls: "syn-bool" });
      } else if (/^[A-Z]/.test(word)) {
        tokens.push({ text: word, cls: "syn-type" });
      } else if (code[j] === "(") {
        tokens.push({ text: word, cls: "syn-fn" });
      } else {
        tokens.push({ text: word, cls: "" });
      }
      i = j;
      continue;
    }
    // Operators / punctuation
    if (/[{}()\[\];,.<>:=+\-*\/!&|^~%?]/.test(code[i])) {
      tokens.push({ text: code[i], cls: "syn-punc" });
      i++;
      continue;
    }
    // Whitespace / other
    let j = i;
    while (j < code.length && !/[A-Za-z_$@"'\/\d{}()\[\];,.<>:=+\-*!&|^~%?]/.test(code[j])) j++;
    tokens.push({ text: code.slice(i, j || i + 1), cls: "" });
    i = j || i + 1;
  }
  return tokens;
}

function highlight(code: string, language?: string): Token[] {
  switch (language) {
    case "java": return highlightJava(code);
    case "yaml":
    case "yml": return highlightYaml(code);
    case "groovy":
    case "gradle": return highlightGroovy(code);
    case "properties": return highlightYaml(code); // similar key:value
    default: return [{ text: code, cls: "" }];
  }
}

// ── Component ───────────────────────────────────────────────────────

function langLabel(lang?: string, filename?: string): string | undefined {
  if (filename) return filename;
  const map: Record<string, string> = {
    java: "Java", yaml: "YAML", yml: "YAML", groovy: "Groovy",
    gradle: "build.gradle", properties: "Properties", xml: "XML",
    json: "JSON", bash: "Shell", sh: "Shell",
  };
  return lang ? map[lang] || lang : undefined;
}

export default function CodeBlock({
  code,
  language,
  filename,
}: {
  code: string;
  language?: string;
  filename?: string;
}) {
  const [copied, setCopied] = useState(false);
  const tokens = useMemo(() => highlight(code, language), [code, language]);
  const label = langLabel(language, filename);

  const copy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="relative group rounded-lg overflow-hidden border border-[var(--border)] my-4">
      {/* Header bar */}
      <div className="flex items-center justify-between px-4 py-2 bg-[var(--bg-tertiary)] border-b border-[var(--border)]">
        <span className="text-xs text-[var(--text-muted)] font-mono">
          {label || "\u00A0"}
        </span>
        <button
          onClick={copy}
          className="p-1 rounded text-[var(--text-muted)] opacity-0 group-hover:opacity-100 focus:opacity-100 hover:text-[var(--accent)] transition-all"
          aria-label="Copy code"
          title={copied ? "Copied!" : "Copy"}
        >
          {copied ? (
            <svg className="w-4 h-4 text-[var(--green)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          ) : (
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
              <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1" />
            </svg>
          )}
        </button>
      </div>
      <pre className="!rounded-t-none !border-0 !m-0">
        <code>
          {tokens.map((t, i) =>
            t.cls ? (
              <span key={i} className={t.cls}>{t.text}</span>
            ) : (
              <span key={i}>{t.text}</span>
            )
          )}
        </code>
      </pre>
    </div>
  );
}
