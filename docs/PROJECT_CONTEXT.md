# Project Context

## Goal

Build a custom AgentScope Studio-style Agent session console:

```text
Frontend Agent Console
  -> Spring Boot REST/SSE
    -> AgentScope Java ReActAgent
      -> AutoContextMemory
      -> AgentScope JsonSession local JSON state
    -> Custom UI JSON Store
      -> sessions.json
      -> messages/{sessionId}.json
      -> summaries/{sessionId}.json
      -> traces/{sessionId}.json
```

The app intentionally avoids Project management, multiple Agent app runtime management, and databases. The first version is a focused local session console.

## Hard Constraints

- Java 17+, Spring Boot 3.x, Maven.
- Backend must integrate AgentScope Java.
- No database dependencies or runtime database usage.
- No Spring Data JPA, MyBatis, H2, SQLite, MySQL/PostgreSQL drivers, Flyway, or Liquibase.
- Frontend is Vue 3 + TypeScript + Vite + Pinia + Element Plus.
- Frontend dependency management and scripts use Bun. Prefer `bun install`, `bun run dev`, and `bun run build`.
- Local JSON Store is for UI projection only.
- AgentScope `JsonSession` is responsible for AgentScope state persistence.
- AgentScope `AutoContextMemory` is the memory target.

## Do Not Reimplement

Do not add any of these custom systems:

- `AgentOrchestrator`
- ReAct loop
- LLM tool-calling loop
- `MemoryManager`
- `ContextCompressor`
- `AgentContextManager`
- custom Agent state serialization
- custom memory persistence
- tool protocol internals
- prompt history stitching and token truncation

These responsibilities should remain inside AgentScope Java.

## Current User-Facing Shape

Route:

- `/agent-console`

Layout:

- Left: session CRUD sidebar.
- Center: chatbot runtime.
- Right: Summary / Message / Trace data panel.

## Project-Local Agent Extensions

This repository intentionally contains project-local agent capabilities:

- `.agents/skills/conventional-commit/SKILL.md`
- `.agents/skills/nano-memory/SKILL.md`
- `.agents/skills/agent-console-agent/SKILL.md`
- `.agents/skills/skills-creator/SKILL.md`
- `src/main/java/com/github/quynj/agentconsole/tool/CalculatorTools.java`
- `src/main/java/com/github/quynj/agentconsole/tool/DateTimeTools.java`
- `src/main/java/com/github/quynj/agentconsole/tool/ListFileTool.java`
- `src/main/java/com/github/quynj/agentconsole/tool/SystemInfoTools.java`
- AgentScope file tools: `ReadFileTool`, `WriteFileTool`
- AgentScope AutoContext tool: `ContextOffloadTool` when `AutoContextMemory` is active

Treat these as part of the current project direction, not accidental worktree noise.

## Official References To Recheck Before API Changes

The project was requested against these AgentScope Java / Studio references:

- AgentScope Java intro: `https://java.agentscope.io/zh/intro.html`
- AgentScope Java repo: `https://github.com/agentscope-ai/agentscope-java`
- Installation: `https://java.agentscope.io/zh/quickstart/installation.html`
- Memory and `AutoContextMemory`: `https://java.agentscope.io/zh/task/memory.html`
- Session, `JsonSession`, `saveTo`, `loadIfExists`: `https://java.agentscope.io/zh/task/session.html`
- Observability / Studio / Trace: `https://java.agentscope.io/zh/task/observability.html`
- LLM guide: `https://raw.githubusercontent.com/agentscope-ai/agentscope-java/main/docs/llm/agentscope-llm-guide.md`
- Studio repo for UI/data-model reference only: `https://github.com/agentscope-ai/agentscope-studio/tree/main`
- Studio Project / Message / Run model: `https://agentscope-ai.github.io/agentscope-studio/zh_CN/develop/project.html`
- Studio overview: `https://agentscope-ai.github.io/agentscope-studio/zh_CN/tutorial/overview.html`
- Studio tracing: `https://agentscope-ai.github.io/agentscope-studio/zh_CN/develop/tracing.html`

Recheck official docs/source before changing AgentScope Java APIs. Do not guess class names or builder parameters.
