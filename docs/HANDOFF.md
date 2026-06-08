# Handoff

## Quick Start

Backend:

```bash
mvn spring-boot:run
```

Default backend URL:

```text
http://localhost:8080
```

Frontend:

```bash
cd frontend
bun run dev
```

Open the Vite URL and use:

```text
/quynj-claw
```

## Build Verification

Backend compile:

```bash
mvn -q -DskipTests compile
```

Frontend build:

```bash
cd frontend
bun run build
```

Both passed during this handoff.

Latest frontend-only verification:

```bash
cd frontend && bun run build
```

This passed on 2026-06-02 after the chat UI and avatar menu updates. Vite still reports the known large chunk warning; it is not a build failure.

## Required Configuration

Main config file:

```text
src/main/resources/application.yml
```

Important keys:

```yaml
quynj-claw:
  base-dir: ${user.dir}/.agents
  ui-store-path: ${user.dir}/.agents/ui-store
  agentscope-session-store-path: ${user.dir}/.agents/agentscope-sessions
  skills-path: ${user.dir}/.agents/skills
  default-agent-name: Sunday
  default-system-prompt: |-
    # Identity
    ...
  model:
    provider: openai
    name: ${MODEL:qwen3.5:9B-UD-Q4_K_XL}
    api-key: ${ZHIPU_API_KEY:not-needed}
    base-url: ${ZHIPU_BASE_URL:http://localhost:11434/engines/v1}
  memory:
    type: auto-context
    max-context-tokens: 8192
    fallback-to-in-memory: true
```

Provider notes:

- `ollama`: requires local Ollama-compatible server at `base-url`; no API key check.
- `dashscope`: `AgentFactory` requires non-blank `quynj-claw.model.api-key`; error is `DASHSCOPE_API_KEY is not configured.`
- `openai`: `AgentFactory` requires non-blank `quynj-claw.model.api-key`; error is `OPENAI_API_KEY is not configured.`

## Where AgentScope Is Integrated

JsonSession:

- `src/main/java/com/github/quynj/quynjclaw/agentscope/AgentSessionStore.java`
- Initializes `new JsonSession(path)` using `quynj-claw.agentscope-session-store-path`.
- Exposes `loadIfExists`, `save`, `exists`, and `delete`.
- Validates session IDs before touching AgentScope session storage.

AutoContextMemory:

- `src/main/java/com/github/quynj/quynjclaw/agentscope/AgentMemoryFactory.java`
- Attempts to create `AutoContextMemory` with `AutoContextConfig` and the selected model.
- Uses `quynj-claw.memory.max-context-tokens` as `AutoContextConfig.maxToken`.
- Falls back to `InMemoryMemory` when initialization fails and fallback is enabled.
- `AgentFactory` registers `ContextOffloadTool` when the created memory is `AutoContextMemory`.

Agent creation:

- `src/main/java/com/github/quynj/quynjclaw/agentscope/AgentFactory.java`
- Creates the model and memory.
- Registers project Java tools into AgentScope `Toolkit`.
- Loads `.agents/skills` through AgentScope `FileSystemSkillRepository`.
- Registers loaded skills into AgentScope `SkillBox`.
- Builds `ReActAgent` with `toolkit(...)` and `skillBox(...)`.

Runtime call:

- `src/main/java/com/github/quynj/quynjclaw/application/AgentScopeRuntimeService.java`
- Calls `loadIfExists` before `agent.call(...)`.
- Calls `save` after successful completion.
- Records console-level trace spans through `TraceService`; these are UI projections only and do not replace AgentScope telemetry.

## How To Verify Session Isolation

Manual check:

1. Start backend and frontend.
2. Create two sessions.
3. Send different messages in each session.
4. Inspect UI JSON:

```bash
ls .agents/ui-store/messages
cat .agents/ui-store/sessions.json
```

5. Confirm each session has its own `messages/{sessionId}.json`.
6. Inspect AgentScope state root:

```bash
ls .agents/agentscope-sessions
```

7. Delete one session from the UI.
8. Confirm the matching UI JSON files are gone and the other session files remain.

Code-level check:

- `AgentFactory.createAgent(...)` must continue creating fresh Agent/Memory per call.
- `AgentScopeRuntimeService` must continue loading/saving only `session.agentscopeSessionId`.
- Do not add code that replays other sessions' UI messages into the prompt.

## Local JSON Store

UI projection files:

```text
.agents/ui-store/sessions.json
.agents/ui-store/messages/{sessionId}.json
.agents/ui-store/summaries/{sessionId}.json
.agents/ui-store/traces/{sessionId}.json
```

AgentScope state:

```text
.agents/agentscope-sessions/
```

Project-local skills:

```text
.agents/skills/
```

Agent built-in Java tools:

```text
src/main/java/com/github/quynj/quynjclaw/tool/
```

## Known TODOs

Read `docs/TODO_NEXT.md` first. The current smallest useful next task is a simple frontend smoke test or documented manual QA path for create/send/switch/delete and the chat input controls.

## Chat UI Notes

- The center chat feed now uses committed SVG avatars from `frontend/src/assets/avatars`.
- `frontend/src/composables/useChatAvatars.ts` owns the local avatar state and persists it in browser `localStorage`.
- User messages should display as `You`; assistant messages should display the configured agent name.
- Do not show raw role/type labels such as `user` or `assistant` in the chat feed.
- The input toolbar More menu currently has one action, `随机头像`, and is intended as the extension point for future local chat UI actions.
- These avatar and menu choices are frontend presentation state only. Do not feed them into AgentScope runtime state or backend UI JSON unless the product deliberately changes that boundary.

## Project-Local Additions

These are intentional parts of the current project direction:

- Frontend uses Bun for dependency management and scripts.
- `frontend/bun.lock` is the expected frontend lockfile.
- `frontend/package-lock.json` has been removed as part of the Bun workflow.
- `frontend/src/assets/avatars/*` contains committed static chat avatar assets.
- `.agents/skills/conventional-commit/SKILL.md`, `.agents/skills/nano-memory/SKILL.md`, `.agents/skills/quynj-claw-agent/SKILL.md`, and `.agents/skills/skills-creator/SKILL.md` are project-local agent skills.
- `src/main/java/com/github/quynj/quynjclaw/tool/*` contains Agent built-in tool classes.
- `.agents/ui-store/` and `.agents/agentscope-sessions/` are runtime state and are ignored by git.

Do not revert these unless the user explicitly asks.
