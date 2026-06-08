# Implementation Status

Last scanned: 2026-06-02.

## Current Stack

Backend:

- Spring Boot `3.5.14`
- Java `25`
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
mvn -q test
cd frontend && bun run build
```

Frontend build warning:

- Vite reports one chunk above 500 kB after minification. This is not a build failure.
- The latest frontend-only verification was `cd frontend && bun run build` on 2026-06-02 after the chat UI refresh.

## Implemented Backend Behavior

- Session CRUD through local JSON.
- Session summary endpoint.
- Message list and send endpoints.
- Message lookup by scanning message JSON files.
- Trace list and lookup endpoints backed by local trace JSON.
- Console-level runtime trace recording for chat request roots, user-message persistence, AgentScope agent creation, AgentScope session load/save, blocking/stream calls, stream events, errors, and cancellation.
- `trace.created` and `trace.updated` SSE events.
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
- `quynj-claw.memory.max-context-tokens` is wired into `AutoContextConfig.maxToken`.
- `ContextOffloadTool` is registered when `AutoContextMemory` is active.
- `InMemoryMemory` fallback if `AutoContextMemory` creation fails and fallback is enabled.
- `DashScopeChatModel`, `OpenAIChatModel`, and `OllamaChatModel` provider branches.
- Basic AgentScope `Msg` mapping for text messages.
- AgentScope message projection now maps `text`, `thinking`, `tool_use`, `tool_result`, `image`, `audio`, and `video` content blocks when present in raw message content.
- Streaming chat path uses AgentScope `agent.stream(...)`, publishes `message.delta`, and persists completed stream messages after completion.
- Error block projection on chat failure.

## Implemented Frontend Behavior

- Three-column `/quynj-claw` view.
- Left session sidebar:
  - list
  - search
  - create
  - rename
  - delete
  - status and preview rendering through `SessionItem`
- Center chat:
  - message list
  - polished message cards with avatar, display name, and timestamp metadata
  - user messages display as `You`; assistant messages display the configured agent name
  - message role/type labels such as `user` and `assistant` are intentionally hidden from the chat feed
  - markdown text rendering
  - text/thinking/tool/error/raw block components
  - send input with loading state
  - image attachment upload, paste/drop handling, and preview modal
  - voice input button when browser speech recognition is available
  - More menu in the input toolbar with a `随机头像` action
  - project-local static avatar assets under `frontend/src/assets/avatars`
  - avatar selection state is client-only UI state persisted in `localStorage`
- Right data panel:
  - Summary tab
  - Message detail tab
  - Trace tab with timeline rows, status icons, duration, and selected-span JSON details
- Session switching loads messages, summary, traces, and opens a new SSE connection.

## Current Configuration

`src/main/resources/application.yml` currently defaults to:

```yaml
server:
  port: 8080

quynj-claw:
  model:
    provider: openai
    name: ${MODEL:qwen3.5:9B-UD-Q4_K_XL}
    api-key: ${ZHIPU_API_KEY:not-needed}
    base-url: ${ZHIPU_BASE_URL:http://localhost:11434/engines/v1}
```

Note: the original requested sample used DashScope and `DASHSCOPE_API_KEY`. The current code supports DashScope, OpenAI-compatible, and Ollama providers, and `application.yml` currently defaults to the `openai` branch with ZHIPU-style environment variable names.

## Known Issues And Gaps

- Deleting a session does clear UI JSON and AgentScope `JsonSession`, but `ConversationService.delete(...)` itself only clears UI JSON. AgentScope deletion is currently done from `SessionController`.
- `messageCount` now counts UI message projections. A successful turn adds user + assistant, and a failed turn adds user + error.
- Backend publishes `message.created` for saved user projections, and the frontend guards stale session loads/responses/events during quick session switching.
- `SummaryService` exists, and runtime summary updates currently mirror `LocalSessionStore` through `ConversationService.markDone(...)` / `markError(...)`.
- No token accounting is implemented yet; token fields remain zero.
- Chat avatars are frontend-only presentation state. They are not persisted in backend UI JSON and are not part of AgentScope inference state.
- Trace integration records console-level runtime spans. AgentScope official telemetry/OpenTelemetry spans are not mapped/appended yet.
- Streaming is implemented as AgentScope event projection with `message.delta`; token-level incremental text streaming is not implemented because runtime uses `incremental(false)`.
- Tool rendering components and basic `tool_use` / `tool_result` content block mapping are implemented, but provider-specific edge cases may still need refinement.
- `temperature` is stored on `ChatSessionDTO` but is not currently applied to model creation.
- `AgentMemoryFactory` currently uses fixed AutoContext tuning for `msgThreshold(30)`, `lastKeep(10)`, and `tokenRatio(0.3)` beyond the configurable `max-context-tokens`.
- API-key error message depends on provider:
  - DashScope branch throws `DASHSCOPE_API_KEY is not configured.`
  - OpenAI branch throws `OPENAI_API_KEY is not configured.`
  - Ollama branch does not require an API key.
- A focused backend smoke test covers local session/message/summary/delete lifecycle. No frontend automated tests were found.
- Project-local agent additions are intentional:
  - `.agents/skills/conventional-commit/SKILL.md`
  - `.agents/skills/nano-memory/SKILL.md`
  - `.agents/skills/quynj-claw-agent/SKILL.md`
  - `.agents/skills/skills-creator/SKILL.md`
  - `src/main/java/com/github/quynj/quynjclaw/tool/*`
- Frontend uses Bun as the dependency manager:
  - `frontend/bun.lock` is intentional.
  - `frontend/package-lock.json` deletion is consistent with moving away from npm lockfiles.
- The latest chat UI polish added SVG avatars directly in source. If future work replaces them with generated bitmap avatars, keep final project-consumed assets in `frontend/src/assets` or another committed frontend asset path, not only in a temporary image-generation directory.

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
