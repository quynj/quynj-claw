# Implementation Status

Last scanned: 2026-04-29.

## Current Stack

Backend:

- Spring Boot `3.3.8`
- Java `17`
- Maven
- AgentScope Java dependency `io.agentscope:agentscope:1.0.11`
- Spring Web
- Spring Validation
- Jackson through Spring Boot
- No database dependencies found in `pom.xml`

Frontend:

- Vue `3.5.x`
- TypeScript
- Vite `6.x`
- Pinia
- Element Plus
- Marked
- Lucide Vue
- Bun lockfile and Bun-oriented scripts are the intended frontend package workflow.

## Verified Commands

Passed:

```bash
mvn -q -DskipTests compile
cd frontend && bun run build
```

Frontend build warning:

- Vite reports one chunk above 500 kB after minification. This is not a build failure.

## Implemented Backend Behavior

- Session CRUD through local JSON.
- Session summary endpoint.
- Message list and send endpoints.
- Message lookup by scanning message JSON files.
- Trace list and lookup endpoints with currently empty trace documents.
- SSE subscription per session.
- Unified `Result` response wrapper.
- Global exception handler.
- Local JSON directory creation on store/session init.
- Atomic JSON writes through temp file and move/replace.
- Synchronized store operations.
- AgentScope `JsonSession` wrapper with session ID validation.
- AgentScope `ReActAgent` creation in `AgentFactory`.
- AgentScope `Toolkit` registration for project built-in Java tool beans.
- AgentScope `SkillBox` registration for skills loaded from `.agents/skills`.
- `AutoContextMemory` creation target in `AgentMemoryFactory`.
- `InMemoryMemory` fallback if `AutoContextMemory` creation fails and fallback is enabled.
- `DashScopeChatModel`, `OpenAIChatModel`, and `OllamaChatModel` provider branches.
- Basic AgentScope `Msg` mapping for text messages.
- Error block projection on chat failure.

## Implemented Frontend Behavior

- Three-column `/agent-console` view.
- Left session sidebar:
  - list
  - search
  - create
  - rename
  - delete
  - status and preview rendering through `SessionItem`
- Center chat:
  - message list
  - role/name display
  - markdown text rendering
  - tool/error/raw block components
  - send input with loading state
- Right data panel:
  - Summary tab
  - Message detail tab
  - Trace tab
- Session switching loads messages, summary, traces, and opens a new SSE connection.

## Current Configuration

`src/main/resources/application.yml` currently defaults to:

```yaml
server:
  port: 8080

agent-console:
  model:
    provider: ollama
    name: ${MODEL:qwen3.5:9B-UD-Q4_K_XL}
    api-key: ${OPENAI_API_KEY:not-needed}
    base-url: http://localhost:11434
```

Note: the original requested sample used DashScope and `DASHSCOPE_API_KEY`. The current code supports DashScope, OpenAI, and Ollama, but defaults to Ollama.

## Known Issues And Gaps

- Deleting a session does clear UI JSON and AgentScope `JsonSession`, but `ConversationService.delete(...)` itself only clears UI JSON. AgentScope deletion is currently done from `SessionController`.
- `messageCount` now counts UI message projections. A successful turn adds user + assistant, and a failed turn adds user + error.
- Backend publishes `message.created` for saved user projections, and the frontend guards stale session loads/responses/events during quick session switching.
- `SummaryService` exists, and runtime summary updates currently mirror `LocalSessionStore` through `ConversationService.markDone(...)` / `markError(...)`.
- No token accounting is implemented yet; token fields remain zero.
- Trace integration is a placeholder. Trace endpoints return data from local trace JSON, but AgentScope telemetry spans are not mapped/appended.
- `message.delta` streaming is not implemented.
- Tool rendering components exist, but AgentScope tool use/tool result parsing is not implemented in `AgentScopeMessageMapper`.
- `temperature` is stored on `ChatSessionDTO` but is not currently applied to model creation.
- `agent-console.memory.max-context-tokens` is configured but not used by `AgentMemoryFactory`; current `AutoContextConfig` uses hardcoded `lastKeep(10)` and `tokenRatio(0.7)`.
- API-key error message depends on provider:
  - DashScope branch throws `DASHSCOPE_API_KEY is not configured.`
  - OpenAI branch throws `OPENAI_API_KEY is not configured.`
  - Ollama branch does not require an API key.
- A focused backend smoke test covers local session/message/summary/delete lifecycle. No frontend automated tests were found.
- Project-local agent additions are intentional:
  - `.agents/skills/conventional-commit/SKILL.md`
  - `.agents/skills/nano-memory/SKILL.md`
  - `src/main/java/com/github/quynj/agentconsole/tool/*`
- Frontend uses Bun as the dependency manager:
  - `frontend/bun.lock` is intentional.
  - `frontend/package-lock.json` deletion is consistent with moving away from npm lockfiles.

## Current Runtime Paths

The project now keeps runtime data under the project root `.agents` tree:

```text
.agents/
  agentscope-sessions/
  ui-store/
  skills/
    conventional-commit/SKILL.md
    nano-memory/SKILL.md
```

`.agents/ui-store/` and `.agents/agentscope-sessions/` are ignored by git because they are runtime state. `.agents/skills/` is intentional project configuration.

## Not Present

- No database code or dependencies.
- No custom Agent orchestrator.
- No custom ReAct loop.
- No custom memory manager or context compressor.
- No global/static Agent or Memory instance.
