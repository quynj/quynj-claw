---
name: skills-creator
description: "Create or update project-local Agent Console skills under .agents/skills. Use when the user asks to add a new skill, revise an existing SKILL.md, define trigger descriptions, add agents/openai.yaml metadata, or turn stable system-prompt guidance into progressive skill instructions."
---

# Skills Creator

## Purpose

Create concise, reliable project-local skills that AgentScope can load from `.agents/skills`. Prefer skills for reusable procedures, durable operating guides, and specialized workflows that do not need to live in the default system prompt.

## Skill Shape

Every skill must include:

- `SKILL.md` with YAML frontmatter containing only `name` and `description`.
- A folder name exactly matching the skill name.
- A lowercase hyphenated skill name.

Recommended when useful:

- `agents/openai.yaml` for UI-facing metadata.
- `scripts/`, `references/`, or `assets/` only when they directly support the skill.

Do not create extra README, changelog, installation notes, or unrelated docs inside a skill.

## Workflow

1. Clarify the skill's trigger and expected tasks from the user request.
2. Choose a short hyphenated name. If the user gives a name, preserve it unless invalid.
3. Create the skill under `.agents/skills/<skill-name>/`.
4. Write a compact `description` that includes both what the skill does and when to use it, because this is the trigger text.
5. Keep `SKILL.md` body procedural and short. Put only information another agent needs at execution time.
6. Add `agents/openai.yaml` with:
   - `display_name`
   - `short_description` between 25 and 64 characters
   - `default_prompt` that explicitly mentions `$<skill-name>`
7. Validate the skill structure. If the validation script is unavailable, at least check that frontmatter exists and has `name` and `description`.
8. Update project docs or the default system prompt if the new skill changes the advertised project-local skills.

## Agent Console Constraints

- Use only file tools registered in this runtime: `view_text_file`, `write_text_file`, `insert_text_file`, `list_files`, and `list_directory`.
- Do not reference unavailable tools such as `edit_file` unless they are explicitly added to the runtime.
- Keep skills project-local unless the user asks for a global Codex skill.
- Do not put runtime state in `.agents/skills`; use `.agents/memory` for durable memory and `.agents/ui-store` only for UI projections.

## Good Skill Descriptions

Use descriptions like:

```text
Create conventional commit messages and commits for this repository. Use when the user asks to commit, stage, amend, or prepare a PR commit summary.
```

Avoid descriptions like:

```text
Helpful notes for commits.
```

The first form gives the loader clear trigger conditions; the second does not.
