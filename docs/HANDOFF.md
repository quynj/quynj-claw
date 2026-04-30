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
/agent-console
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

## Required Configuration

Main config file:

```text
src/main/resources/application.yml
```

Important keys:

```yaml
agent-console:
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
- `dashscope`: `AgentFactory` requires non-blank `agent-console.model.api-key`; error is `DASHSCOPE_API_KEY is not configured.`
- `openai`: `AgentFactory` requires non-blank `agent-console.model.api-key`; error is `OPENAI_API_KEY is not configured.`

## Where AgentScope Is Integrated

JsonSession:

- `src/main/java/com/github/quynj/agentconsole/agentscope/AgentSessionStore.java`
- Initializes `new JsonSession(path)` using `agent-console.agentscope-session-store-path`.
- Exposes `loadIfExists`, `save`, `exists`, and `delete`.
- Validates session IDs before touching AgentScope session storage.

AutoContextMemory:

- `src/main/java/com/github/quynj/agentconsole/agentscope/AgentMemoryFactory.java`
- Attempts to create `AutoContextMemory` with `AutoContextConfig` and the selected model.
- Uses `agent-console.memory.max-context-tokens` as `AutoContextConfig.maxToken`.
- Falls back to `InMemoryMemory` when initialization fails and fallback is enabled.
- `AgentFactory` registers `ContextOffloadTool` when the created memory is `AutoContextMemory`.

Agent creation:

- `src/main/java/com/github/quynj/agentconsole/agentscope/AgentFactory.java`
- Creates the model and memory.
- Registers project Java tools into AgentScope `Toolkit`.
- Loads `.agents/skills` through AgentScope `FileSystemSkillRepository`.
- Registers loaded skills into AgentScope `SkillBox`.
- Builds `ReActAgent` with `toolkit(...)` and `skillBox(...)`.

Runtime call:

- `src/main/java/com/github/quynj/agentconsole/application/AgentScopeRuntimeService.java`
- Calls `loadIfExists` before `agent.call(...)`.
- Calls `save` after successful completion.

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
src/main/java/com/github/quynj/agentconsole/tool/
```

## Known TODOs

Read `docs/TODO_NEXT.md` first. The current smallest useful next task is a simple frontend smoke test or documented manual QA path for create/send/switch/delete.

## Project-Local Additions

These are intentional parts of the current project direction:

- Frontend uses Bun for dependency management and scripts.
- `frontend/bun.lock` is the expected frontend lockfile.
- `frontend/package-lock.json` has been removed as part of the Bun workflow.
- `.agents/skills/conventional-commit/SKILL.md`, `.agents/skills/nano-memory/SKILL.md`, `.agents/skills/agent-console-agent/SKILL.md`, and `.agents/skills/skills-creator/SKILL.md` are project-local agent skills.
- `src/main/java/com/github/quynj/agentconsole/tool/*` contains Agent built-in tool classes.
- `.agents/ui-store/` and `.agents/agentscope-sessions/` are runtime state and are ignored by git.

Do not revert these unless the user explicitly asks.
