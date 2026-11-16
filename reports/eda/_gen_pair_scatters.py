"""Generate scatter plots for each bivariate pair in reports/eda/*.md and embed them."""
from __future__ import annotations

import re
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[2]
EDA = Path(__file__).resolve().parent
CHARTS = EDA / "charts"
CHARTS.mkdir(exist_ok=True)

PAIR_HDR = re.compile(
    r"^### (?:Main bivariate pair|Pair): `([^`]+)` × `([^`]+)`\s*$",
    re.M,
)
SOURCE = re.compile(r"\*\*Source file\*\*\s*\|\s*\[`[^`]+`\]\(([^)]+)\)")
PROP_LINE = re.compile(
    r"(?:^|;)\s*((?:[a-zA-Z_][\w]*:)?[a-zA-Z_][\w]*)\s+([^\s;]+)\s*(?:;|\.)",
    re.M,
)
SUBJECT = re.compile(r"^<([^>]+)>\s+a\s+", re.M)


def expand_curie(token: str, prefixes: dict[str, str]) -> str:
    if token.startswith("<") and token.endswith(">"):
        return token[1:-1]
    if ":" in token:
        pre, local = token.split(":", 1)
        if pre in prefixes:
            return prefixes[pre] + local
    return token


def parse_object(raw: str, prefixes: dict[str, str]) -> object:
    if raw.startswith('"'):
        sm2 = re.match(r'"((?:\\.|[^"\\])*)"', raw)
        return sm2.group(1) if sm2 else raw.strip('"')
    if raw.startswith("<") and raw.endswith(">"):
        return raw[1:-1]
    try:
        return float(raw)
    except ValueError:
        return expand_curie(raw, prefixes) if ":" in raw else raw


def parse_ttl(path: Path) -> tuple[dict[str, dict[str, object]], dict[str, str]]:
    text = path.read_text(encoding="utf-8")
    prefixes: dict[str, str] = {}
    for m in re.finditer(r"@prefix\s+(\w+):\s*<([^>]+)>\s*\.", text):
        prefixes[m.group(1)] = m.group(2)

    subjects: dict[str, dict[str, object]] = {}
    # Absolute IRI subjects OR prefixed subjects (ex:alice)
    blocks = re.split(r"(?=^(?:<[^>]+>|\w+:[\w-]+)\s)", text, flags=re.M)
    subj_re = re.compile(r"^(?:<([^>]+)>|(\w+:[\w-]+))\s")
    prop_re = re.compile(
        r"(?:^|[;\n])\s*((?:\w+:)[\w]+)\s+([^\s;]+)(?:\^\^[\w:]+|@[a-zA-Z-]+)?\s*(?=[;.])",
        re.M,
    )
    for block in blocks:
        sm = subj_re.match(block)
        if not sm:
            continue
        sid = sm.group(1) or expand_curie(sm.group(2), prefixes)
        props: dict[str, object] = {}
        for pm in prop_re.finditer(block):
            short = pm.group(1)
            # skip rdf:type-like "a Class" — handled separately; "a" isn't \w+:
            pred = expand_curie(short, prefixes)
            val = parse_object(pm.group(2), prefixes)
            props[short] = val
            props[pred] = val
        subjects[sid] = props
    return subjects, prefixes


def to_numeric(values: list[object]) -> tuple[list[float], list[float], bool]:
    """Return xs, ys for scatter; categorical=True if non-numeric encoding used."""
    # paired already
    return [], [], False


def pair_points(
    subjects: dict[str, dict[str, object]], left: str, right: str
) -> tuple[list[float], list[float], str, str]:
    xs: list[float] = []
    ys: list[float] = []
    left_cats: dict[object, int] = {}
    right_cats: dict[object, int] = {}
    numeric_pairs: list[tuple[float, float]] = []
    cat_pairs: list[tuple[object, object]] = []

    for props in subjects.values():
        if left not in props or right not in props:
            continue
        a, b = props[left], props[right]
        a_num = isinstance(a, (int, float)) and not isinstance(a, bool)
        b_num = isinstance(b, (int, float)) and not isinstance(b, bool)
        if a_num and b_num:
            numeric_pairs.append((float(a), float(b)))
        else:
            cat_pairs.append((a, b))

    if numeric_pairs and not cat_pairs:
        xs = [p[0] for p in numeric_pairs]
        ys = [p[1] for p in numeric_pairs]
        return xs, ys, left, right

    # categorical / mixed → encode
    pairs = numeric_pairs + [
        (
            float(a) if isinstance(a, (int, float)) and not isinstance(a, bool) else a,
            float(b) if isinstance(b, (int, float)) and not isinstance(b, bool) else b,
        )
        for a, b in cat_pairs
    ]
    # rebuild from original cat+num
    pairs2: list[tuple[object, object]] = []
    for props in subjects.values():
        if left in props and right in props:
            pairs2.append((props[left], props[right]))

    def enc(v: object, table: dict[object, int]) -> float:
        if isinstance(v, (int, float)) and not isinstance(v, bool):
            return float(v)
        if v not in table:
            table[v] = len(table)
        return float(table[v])

    xs = [enc(a, left_cats) for a, _ in pairs2]
    ys = [enc(b, right_cats) for _, b in pairs2]
    xlab = left if not left_cats else f"{left} (encoded)"
    ylab = right if not right_cats else f"{right} (encoded)"
    return xs, ys, xlab, ylab


def slug(s: str) -> str:
    return re.sub(r"[^a-zA-Z0-9]+", "_", s).strip("_").lower()


def plot_scatter(
    xs: list[float],
    ys: list[float],
    xlab: str,
    ylab: str,
    title: str,
    out: Path,
) -> None:
    fig, ax = plt.subplots(figsize=(5.5, 4.0), dpi=120)
    ax.scatter(xs, ys, s=18, alpha=0.65, c="#1f77b4", edgecolors="none")
    ax.set_xlabel(xlab)
    ax.set_ylabel(ylab)
    ax.set_title(title)
    ax.grid(True, alpha=0.25)
    fig.tight_layout()
    fig.savefig(out, bbox_inches="tight")
    plt.close(fig)


def process_md(md_path: Path) -> None:
    text = md_path.read_text(encoding="utf-8")
    sm = SOURCE.search(text)
    if not sm:
        print(f"skip {md_path.name}: no source")
        return
    ttl_rel = sm.group(1)
    ttl_path = (md_path.parent / ttl_rel).resolve()
    if not ttl_path.exists():
        print(f"skip {md_path.name}: missing {ttl_path}")
        return

    subjects, _ = parse_ttl(ttl_path)
    pairs = list(PAIR_HDR.finditer(text))
    if not pairs:
        print(f"skip {md_path.name}: no pairs")
        return

    # Rebuild document inserting charts after each pair block
    # Pair block ends at next ### or end
    out_parts: list[str] = []
    last = 0
    stem = md_path.stem
    for i, m in enumerate(pairs):
        left, right = m.group(1), m.group(2)
        # end of this pair section = start of next ### heading after this match, or EOF
        after_hdr = m.end()
        next_hdr = re.search(r"^### ", text[after_hdr:], re.M)
        section_end = after_hdr + next_hdr.start() if next_hdr else len(text)
        section = text[m.start() : section_end]

        # strip any previous chart embed for this pair
        section = re.sub(
            r"\n*!\[.*?\]\(charts/[^)]+\)\s*",
            "\n",
            section,
        )
        section = section.rstrip() + "\n"

        xs, ys, xlab, ylab = pair_points(subjects, left, right)
        chart_name = f"{stem}__{slug(left)}__{slug(right)}.png"
        chart_path = CHARTS / chart_name
        if xs:
            plot_scatter(
                xs,
                ys,
                xlab,
                ylab,
                f"{left} × {right}  (n={len(xs)})",
                chart_path,
            )
            embed = f"\n![{left} × {right} scatter](charts/{chart_name})\n"
            section = section.rstrip() + "\n" + embed
            print(f"  {md_path.name}: {left}×{right} n={len(xs)} → {chart_name}")
        else:
            print(f"  {md_path.name}: {left}×{right} no points")

        out_parts.append(text[last : m.start()])
        out_parts.append(section)
        # if section didn't include trailing content before next hdr carefully:
        last = section_end

    out_parts.append(text[last:])
    new_text = "".join(out_parts)
    # collapse excessive blank lines
    new_text = re.sub(r"\n{3,}", "\n\n", new_text)
    md_path.write_text(new_text, encoding="utf-8")


def main() -> None:
    for md in sorted(EDA.glob("*.md")):
        if md.name.startswith("_") or md.name == "README.md":
            continue
        print(md.name)
        process_md(md)


if __name__ == "__main__":
    main()
