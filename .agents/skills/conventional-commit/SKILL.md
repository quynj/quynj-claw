---
name: conventional-commit
version: 1.0.0
description: "Create Git commits that follow the Conventional Commits specification. Use whenever the user asks Codex to commit changes, create a git commit, write a commit message, amend a commit message, or prepare a PR/commit summary that should become a commit. Enforce type(scope): subject formatting, choose an accurate type from the diff, protect unrelated work, and include body/footer only when useful."
---

# Conventional Commit

## Overview

Use this skill for every git commit in this project. The goal is a clean, truthful commit message that follows Conventional Commits and reflects exactly the staged changes.

## Commit Workflow

1. Inspect repository state before committing:
   - Run `git status --short`.
   - Review staged changes with `git diff --cached`.
   - If nothing is staged and the user asked you to commit your work, stage only files you intentionally changed for this task.
   - Do not stage unrelated or user-owned changes unless the user explicitly asks.

2. Choose a Conventional Commits type:
   - `feat`: user-visible feature or capability
   - `fix`: bug fix or correctness change
   - `docs`: documentation only
   - `style`: formatting only, no behavior change
   - `refactor`: code change that is neither feature nor bug fix
   - `perf`: performance improvement
   - `test`: tests only or test infrastructure
   - `build`: build system, dependencies, packaging
   - `ci`: CI configuration or automation
   - `chore`: maintenance that does not fit above
   - `revert`: revert a previous commit

3. Choose a scope when it clarifies the affected area:
   - Prefer a module, package, app, or feature name, such as `studio`, `agent`, `pom`, `api`, `ui`, `tests`.
   - Omit scope if the change is broad or a scope would be forced.

4. Write the subject:
   - Format: `<type>(<scope>): <subject>` or `<type>: <subject>`.
   - Use imperative mood: `add`, `fix`, `update`, `remove`, `configure`.
   - Keep it concise, ideally 72 characters or fewer.
   - Use lowercase after the colon unless a proper noun or identifier requires capitalization.
   - Do not end with a period.

5. Add body/footer only when useful:
   - Body: explain why, migration notes, or non-obvious behavior.
   - Footer: include `BREAKING CHANGE: ...` for breaking changes, or issue references like `Refs #123`.
   - If the user gave an issue number, include it in the footer rather than forcing it into the subject.

6. Commit:
   - Prefer `git commit -m "<subject>"` for simple commits.
   - Use multiple `-m` arguments when a body or footer is needed.
   - After committing, report the commit hash and message.

## Guardrails

- Never use vague subjects like `update files`, `changes`, `fix stuff`, or `wip`.
- Never claim a feature, fix, or test was added unless the diff shows it.
- Do not include generated dependency/cache files unless they are intentionally part of the change.
- If the staged diff mixes unrelated purposes, ask whether to split into separate commits.
- If the working tree contains unrelated user changes, leave them unstaged and mention that they were left alone.
- If a command fails because the repository has no commits, no staged files, or missing git identity, explain the blocker and the exact next step.

## Examples

- `feat(studio): add Web UI conversation runner`
- `fix(studio): align OpenTelemetry dependency versions`
- `build(pom): add AgentScope Studio extension`
- `docs: document Studio startup workflow`
- `test(agent): cover prompt routing behavior`
