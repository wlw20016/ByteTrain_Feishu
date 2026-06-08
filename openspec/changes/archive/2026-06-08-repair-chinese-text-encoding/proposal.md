# Change: Repair Chinese Text Encoding

## Why

Several Chinese project documents and Android UI strings are visibly mojibake. This reduces mobile polish and makes OpenSpec/AI context harder to consume. The project should preserve Chinese content as UTF-8 and keep user-visible app text readable.

## What Changes

- Repair user-visible Chinese text in Android UI files.
- Repair key OpenSpec and docs files that are intended to be read by reviewers or AI assistants.
- Add checks that detect common mojibake markers in source, OpenSpec, and docs.
- Document UTF-8 handling expectations for future edits.

## Impact

- Affects docs, OpenSpec text, and Android UI string literals.
- Does not change product logic, SDK contracts, or Bazel target structure.
- Requires careful edits to avoid changing meaning while fixing encoding.
