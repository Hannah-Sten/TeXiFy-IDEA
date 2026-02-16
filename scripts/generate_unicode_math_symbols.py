#!/usr/bin/env python
import re
import subprocess
import sys
import unicodedata
from collections import Counter, OrderedDict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PREDEFINED_DIR = ROOT / "src" / "nl" / "hannahsten" / "texifyidea" / "lang" / "predefined"
OUT_FILE = PREDEFINED_DIR / "PredefinedCmdUnicodeMathSymbols.kt"

LINE_RE = re.compile(
    r'\\UnicodeMathSymbol\{"([0-9A-F]+)\}\{\\([^}\s]+)\s*\}\{\\([^}]+)\}\{([^}]*)\}'
)

SYMBOL_OR_CMD_RE = re.compile(r'symbol\("([^"]+)"|"([^"]+)"\.cmd')


def run_kpsewhich() -> Path:
    result = subprocess.run(
        ["kpsewhich", "unicode-math-table.tex"],
        capture_output=True,
        text=True,
        check=False,
    )
    path = result.stdout.strip()
    if result.returncode != 0 or not path:
        print("ERROR: unable to locate unicode-math-table.tex via kpsewhich", file=sys.stderr)
        sys.exit(1)
    p = Path(path)
    if not p.exists():
        print(f"ERROR: kpsewhich returned non-existing path: {p}", file=sys.stderr)
        sys.exit(1)
    return p


def kotlin_escape(s: str) -> str:
    out = []
    for ch in s:
        code = ord(ch)
        if ch == "\\":
            out.append("\\\\")
        elif ch == '"':
            out.append('\\"')
        elif ch == "$":
            out.append("\\$")
        elif ch == "\n":
            out.append("\\n")
        elif ch == "\r":
            out.append("\\r")
        elif ch == "\t":
            out.append("\\t")
        elif 0x00 <= code < 0x20:
            out.append(f"\\u{code:04x}")
        else:
            out.append(ch)
    return "".join(out)


def to_display(cp_hex: str):
    code = int(cp_hex, 16)
    if 0xD800 <= code <= 0xDFFF:
        return None
    ch = chr(code)
    cat = unicodedata.category(ch)
    if cat.startswith("C"):
        return None
    return ch


def to_val_name(math_class: str) -> str:
    # mathalpha -> unicodeMathMathalphaSymbols
    parts = re.findall(r"[A-Za-z0-9]+", math_class)
    camel = "".join(p[:1].upper() + p[1:] for p in parts)
    return f"unicodeMath{camel}Symbols"


def load_existing_names() -> set[str]:
    names: set[str] = set()
    for file in PREDEFINED_DIR.glob("*.kt"):
        if file.name == OUT_FILE.name:
            continue
        text = file.read_text(encoding="utf-8")
        for m in SYMBOL_OR_CMD_RE.finditer(text):
            names.add(m.group(1) or m.group(2))
    return names


def main() -> int:
    src = run_kpsewhich()
    existing_names = load_existing_names()

    class_to_entries: OrderedDict[str, list[tuple[str, str | None, str]]] = OrderedDict()
    class_counter: Counter[str] = Counter()

    total_rows = 0
    duplicate_dropped = 0
    seen_names: set[str] = set()
    conflict_with_existing = 0

    with src.open("r", encoding="utf-8") as f:
        for line in f:
            m = LINE_RE.search(line)
            if not m:
                continue

            total_rows += 1
            cp_hex, name, math_class, desc = m.groups()
            class_counter[math_class] += 1

            if name in seen_names:
                duplicate_dropped += 1
                continue
            seen_names.add(name)

            if name in existing_names:
                conflict_with_existing += 1

            display = to_display(cp_hex)
            description = f"unicode-math: {desc.strip()}"
            class_to_entries.setdefault(math_class, []).append((name, display, description))

    lines: list[str] = []
    lines.append("package nl.hannahsten.texifyidea.lang.predefined")
    lines.append("")
    lines.append("import nl.hannahsten.texifyidea.lang.PredefinedCommandSet")
    lines.append("")
    lines.append("/**")
    lines.append(" * Generated from unicode-math-table.tex via scripts/generate_unicode_math_symbols.py.")
    lines.append(" * Do not edit manually; regenerate instead.")
    lines.append(" */")
    lines.append("object PredefinedCmdUnicodeMathSymbols : PredefinedCommandSet() {")
    lines.append("")

    for math_class, entries in class_to_entries.items():
        val_name = to_val_name(math_class)
        lines.append(f"    val {val_name} = mathCommands {{")
        lines.append('        underPackage("unicode-math") {')
        for name, display, description in entries:
            desc_esc = kotlin_escape(description)
            if display is None:
                lines.append(f'            symbol("{kotlin_escape(name)}", null, "{desc_esc}")')
            else:
                lines.append(
                    f'            symbol("{kotlin_escape(name)}", "{kotlin_escape(display)}", "{desc_esc}")'
                )
        lines.append("        }")
        lines.append("    }")
        lines.append("")

    lines.append("}")
    lines.append("")

    OUT_FILE.write_text("\n".join(lines), encoding="utf-8")

    generated_entries = sum(len(v) for v in class_to_entries.values())
    print(f"source: {src}")
    print(f"output: {OUT_FILE}")
    print(f"total rows parsed: {total_rows}")
    print(f"classes parsed: {len(class_to_entries)}")
    print(f"generated entries: {generated_entries}")
    print(f"duplicate names dropped in generated file: {duplicate_dropped}")
    print(f"name conflicts with existing predefined (kept): {conflict_with_existing}")
    print("class distribution:")
    for k, v in sorted(class_counter.items()):
        print(f"  {k}: {v}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
