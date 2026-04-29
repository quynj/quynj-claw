# Agent Console Handoff Guide

This repository implements a custom AgentScope Java session console with a Spring Boot backend and a Vue 3 frontend.

Before changing code, read these documents in order:

1. `docs/PROJECT_CONTEXT.md` - product goal, non-goals, constraints, and source-of-truth rules.
2. `docs/ARCHITECTURE.md` - backend/frontend/store architecture and session isolation model.
3. `docs/IMPLEMENTATION_STATUS.md` - what is currently implemented, verified, and known to work.
4. `docs/TODO_NEXT.md` - next smallest tasks and risk-ranked backlog.
5. `docs/DECISIONS.md` - accepted technical decisions and rationale.
6. `docs/HANDOFF.md` - quick start, validation commands, and operational notes.

Core rules for future agents:

- Do not add a database. The first version uses local JSON only.
- Do not implement a custom ReAct loop, MemoryManager, ContextCompressor, AgentContextManager, tool-calling protocol, prompt history stitching, or token truncation.
- Agent runtime, memory, state persistence, and ReAct behavior belong to AgentScope Java.
- UI JSON files are only projections for display and management. They are not the inference source of truth.
- Each frontend session must map to exactly one generated `agentscopeSessionId`.
- Create a fresh `ReActAgent` and fresh `Memory` per chat call, then load and save only the current AgentScope `JsonSession`.
- Never use static/global Agent or Memory instances.
- Preserve the current no-database dependency policy in `pom.xml`.

Important local paths:

- Backend source: `src/main/java/com/github/quynj/agentconsole`
- Frontend source: `frontend/src`
- Config: `src/main/resources/application.yml`
- AgentScope state: `${user.dir}/.agents/agentscope-sessions`
- UI JSON store: `${user.dir}/.agents/ui-store`
- Project-local AgentScope skills: `${user.dir}/.agents/skills`
- Agent built-in tools: `src/main/java/com/github/quynj/agentconsole/tool`

Validation commands used for this handoff:

```bash
mvn -q -DskipTests compile
cd frontend && bun run build
```

Both commands passed on 2026-04-29. The frontend build emitted only Vite's large chunk warning.
