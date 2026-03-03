#!/usr/bin/env python3
"""Plot Pareto fronts from mo-hbhc plain-text output.

Usage:
  python scripts/pareto_plot.py run.out --output pareto.png
  python scripts/pareto_plot.py run.out --csv archive.csv --negate-first
"""

from __future__ import annotations

import argparse
import csv
import re
from dataclasses import dataclass
from pathlib import Path
from typing import List, Sequence, Tuple


ARCHIVE_HEADER = re.compile(r"^Archive \((\d+) solutions\):$")
PAIR_LINE = re.compile(r"^\s*(-?\d+(?:\.\d+)?)\s*,\s*(-?\d+(?:\.\d+)?)\s*$")


@dataclass
class ParetoFront:
    """Container and utilities for a 2D Pareto front."""

    points: List[Tuple[float, float]]

    @classmethod
    def from_mo_hbhc_output(cls, text: str) -> "ParetoFront":
        """Parse the archive block from mo-hbhc output text."""
        in_archive = False
        points: List[Tuple[float, float]] = []

        for raw_line in text.splitlines():
            line = raw_line.strip()
            if not in_archive:
                if ARCHIVE_HEADER.match(line):
                    in_archive = True
                continue

            match = PAIR_LINE.match(line)
            if match is None:
                if points:
                    break
                continue

            points.append((float(match.group(1)), float(match.group(2))))

        if not points:
            raise ValueError(
                "No Pareto points found. Ensure input contains 'Archive (N solutions):' "
                "followed by lines in 'x, y' format."
            )

        return cls(points)

    def unique_sorted(self) -> "ParetoFront":
        """Remove duplicates and sort by first objective (then second)."""
        return ParetoFront(sorted(set(self.points), key=lambda p: (p[0], p[1])))

    def maybe_negate_first(self, negate: bool) -> "ParetoFront":
        """Optionally convert first objective sign (e.g., -cost -> cost)."""
        if not negate:
            return ParetoFront(list(self.points))
        return ParetoFront([(-x, y) for x, y in self.points])

    def to_csv(self, path: Path, header: Sequence[str] = ("f1", "f2")) -> None:
        """Save points to CSV file."""
        path.parent.mkdir(parents=True, exist_ok=True)
        with path.open("w", newline="", encoding="utf-8") as fh:
            writer = csv.writer(fh)
            if header:
                writer.writerow(list(header))
            writer.writerows(self.points)

    def plot(
        self,
        output_path: Path,
        title: str = "Pareto Front",
        x_label: str = "Objective 1",
        y_label: str = "Objective 2",
    ) -> None:
        """Render a scatter plot to an image file."""
        try:
            import matplotlib.pyplot as plt
        except ImportError as exc:
            raise RuntimeError(
                "matplotlib is required for plotting. Install it with 'pip install matplotlib'."
            ) from exc

        output_path.parent.mkdir(parents=True, exist_ok=True)

        xs = [p[0] for p in self.points]
        ys = [p[1] for p in self.points]

        plt.figure(figsize=(8, 5))
        plt.scatter(xs, ys, s=16)
        plt.title(title)
        plt.xlabel(x_label)
        plt.ylabel(y_label)
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(output_path, dpi=180)
        plt.close()


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Parse and plot Pareto front from mo-hbhc output")
    parser.add_argument("input", type=Path, help="Path to mo-hbhc output file")
    parser.add_argument("--output", type=Path, default=Path("pareto.png"), help="Image output path")
    parser.add_argument("--csv", type=Path, default=None, help="Optional CSV output path")
    parser.add_argument(
        "--negate-first",
        action="store_true",
        help="Negate first objective (useful to show real cost when archive prints -cost)",
    )
    parser.add_argument("--title", default="Pareto Front (mo-hbhc)", help="Plot title")
    parser.add_argument("--xlabel", default="Objective 1", help="X axis label")
    parser.add_argument("--ylabel", default="Objective 2", help="Y axis label")
    parser.add_argument("--no-plot", action="store_true", help="Only parse/export CSV; do not generate image")
    return parser


def main() -> None:
    args = build_parser().parse_args()
    text = args.input.read_text(encoding="utf-8")

    front = ParetoFront.from_mo_hbhc_output(text).maybe_negate_first(args.negate_first).unique_sorted()

    if args.csv is not None:
        header = ("cost", "coverage") if args.negate_first else ("f1", "f2")
        front.to_csv(args.csv, header=header)

    if not args.no_plot:
        front.plot(args.output, title=args.title, x_label=args.xlabel, y_label=args.ylabel)


if __name__ == "__main__":
    main()