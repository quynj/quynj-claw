---
name: nano-memory
description: "Maintain durable local memory for the Agent Console user and project using only the registered file tools and shell search. Use when the agent needs to recall who the user is, user preferences, project history, prior decisions, daily logs, or when it learns durable facts that should survive new sessions."
---

# Nano Memory

## Architecture

Durable memory lives in project-local files. AgentScope session memory and UI JSON files are not a substitute for this skill.

Core files:

- `.agents/memory/MEMORY.md`: curated long-term memory for user identity, preferences, project conventions, and stable decisions.
- `.agents/memory/YYYY-MM-DD.md`: daily chronological logs for recent work and raw notes.

## Available Tools

Use only tools that are actually registered in this Agent Console runtime:

- `list_files` or `list_directory` to inspect directories.
- `view_text_file` to read file contents, optionally with line ranges.
- `write_text_file` to create, overwrite, or replace a range in a text file.
- `insert_text_file` to insert text at a specific line when available.
- Shell search commands such as `rg`, `grep`, `ls`, `find`, or platform equivalents when the runtime allows shell access.

Do not call non-existent tools such as `read_file`, `write_file`, or `edit_file` unless the current runtime explicitly exposes them. In this project, prefer `view_text_file`, `write_text_file`, and `insert_text_file`.

## Workflow A: Retrieve Memory

1. List `.agents/memory/` to see available memory files.
2. Search `.agents/memory/` for relevant names, topics, dates, decisions, or preferences.
3. Read the specific matching files with `view_text_file`.
4. Answer based on retrieved memory. If memory is absent or ambiguous, say so.

Use this workflow before claiming to know who the user is, what they prefer, or what happened in previous sessions.

## Workflow B: Record Daily Context

1. Use the current local date for `.agents/memory/YYYY-MM-DD.md`.
2. If the file exists, read it first with `view_text_file`; then append by rewriting the file with `write_text_file` or insert at the end with `insert_text_file`.
3. If the file does not exist, create it with `write_text_file`.
4. Keep daily entries short, dated, and factual.

## Workflow C: Update Long-Term Memory

1. Read `.agents/memory/MEMORY.md` with `view_text_file` before updating.
2. Preserve existing structure and useful content.
3. Use `write_text_file` with a precise line range when the target section is clear. Otherwise rewrite the full file after reading the latest contents.
4. Record durable facts only: stable preferences, identity details the user wants remembered, project conventions, architecture decisions, recurring workflows, and completed milestones.
5. Do not store passwords, API keys, private medical/financial details, or sensitive personal data unless the user explicitly requests it.

## Recording Rules

- Record first, then rely on the memory in future answers.
- Prefer concise distilled memory over full chat transcripts.
- Do not use `.agents/ui-store` as memory; those files are UI projections only.
- If a write tool fails, report the failure and do not pretend the memory was saved.
