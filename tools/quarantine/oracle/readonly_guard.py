#!/usr/bin/env python3
"""Minimal fail-closed Oracle read-only transaction guard."""

from __future__ import annotations

import argparse
import sys


def enforce_read_only(connection) -> None:
    cursor = None
    try:
        cursor = connection.cursor()
        cursor.execute("SET TRANSACTION READ ONLY")
    except Exception as exc:
        try:
            connection.close()
        finally:
            raise RuntimeError("READ_ONLY_TRANSACTION_NOT_ENFORCED") from exc
    finally:
        if cursor is not None:
            cursor.close()


class _Cursor:
    def __init__(self, should_fail: bool):
        self.should_fail = should_fail
        self.closed = False

    def execute(self, statement: str):
        if statement != "SET TRANSACTION READ ONLY" or self.should_fail:
            raise RuntimeError("database rejected read-only transaction")

    def close(self):
        self.closed = True


class _Connection:
    def __init__(self, should_fail: bool):
        self.cursor_value = _Cursor(should_fail)
        self.closed = False

    def cursor(self):
        return self.cursor_value

    def close(self):
        self.closed = True


def self_test() -> None:
    accepted = _Connection(False)
    enforce_read_only(accepted)
    assert accepted.cursor_value.closed and not accepted.closed
    rejected = _Connection(True)
    try:
        enforce_read_only(rejected)
    except RuntimeError as exc:
        assert str(exc) == "READ_ONLY_TRANSACTION_NOT_ENFORCED"
    else:
        raise AssertionError("read-only rejection must fail closed")
    assert rejected.closed and rejected.cursor_value.closed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--self-test", action="store_true", required=True)
    parser.parse_args()
    try:
        self_test()
        print("oracle-readonly-guard: PASS")
        return 0
    except AssertionError as exc:
        print(f"oracle-readonly-guard: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
