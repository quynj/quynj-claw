# Architecture

## Backend Package Map

Base package: `com.github.quynj.agentconsole`

- `api`
  - `SessionController`: session CRUD and summary endpoint.
  - `ChatController`: send message endpoint.
  - `MessageController`: list/find UI message projections.
  - `TraceController`: trace list/find placeholders.
  - `SseController`: session event stream.
  - `GlobalExceptionHandler`: unified `Result` error responses.
- `application`
  - `ConversationService`: coordinates UI store lifecycle and status updates.
  - `AgentScopeRuntimeService`: chat flow through AgentScope.
  - `MessageProjectionService`: UI message projection access.
  - `SummaryService`: summary access.
  - `RealtimeEventService`: per-session Spring `SseEmitter` registry.
- `agentscope`
  - `AgentFactory`: builds a fresh `ReActAgent` per request.
  - `AgentMemoryFactory`: creates `AutoContextMemory`, with `InMemoryMemory` fallback.
  - `AgentSessionStore`: wraps AgentScope `JsonSession`.
  - `AgentScopeMessageMapper`: maps frontend DTOs to/from AgentScope `Msg`.
  - `AgentScopeTraceMapper`: placeholder mapper.
- `store`
  - `LocalSessionStore`: `sessions.json`.
  - `LocalMessageStore`: `messages/{sessionId}.json`.
  - `LocalSummaryStore`: `summaries/{sessionId}.json`.
  - `LocalTraceStore`: `traces/{sessionId}.json`.
- `dto`
  - frontend/backend API DTOs.
- `common`
  - `Result`, `PageResult`, `IdGenerator`, `JsonFileUtils`.

## Runtime Flow

`POST /api/sessions/{sessionId}/messages` currently follows this path:

1. `ConversationService.get(sessionId)` validates the UI session exists.
2. `AgentScopeMessageMapper.toUserProjection(...)` creates a UI-only user message.
3. `MessageProjectionService.append(...)` persists that user message to UI JSON.
4. `ConversationService.markRunning(...)` updates UI session status.
5. `RealtimeEventService.publishSessionUpdated(...)` sends SSE.
6. `AgentFactory.createAgent(session)` creates a fresh `ReActAgent`.
7. `AgentFactory` registers built-in Java tools into an AgentScope `Toolkit`.
8. `AgentFactory` loads project-local skills from `.agents/skills` into an AgentScope `SkillBox`.
9. `AgentSessionStore.loadIfExists(agent, session.agentscopeSessionId)` loads only this AgentScope session.
10. Runtime delegates execution to AgentScope Java:
    - non-streaming requests use `agent.call(userMsg).block()`.
    - streaming requests use `agent.stream(userMsg, StreamOptions...)`, publish `message.delta`, and persist completed messages after the stream finishes.
11. `AgentSessionStore.save(agent, session.agentscopeSessionId)` saves only this AgentScope session.
12. The AgentScope `Msg` is mapped to an assistant UI projection.
13. Message/session/summary stores are updated.
14. SSE emits `message.created` and `session.updated`.

On runtime exceptions, the service appends an error block message, marks the session `error`, emits `error`, emits the error message, and lets `GlobalExceptionHandler` return a unified error response.

## Session Isolation

Current isolation mechanisms:

- Session IDs are backend-generated in `IdGenerator.sessionId()`.
- `ChatSessionDTO.agentscopeSessionId` is set equal to the generated session ID.
- `AgentFactory` creates a new `ReActAgent` and `Memory` for every call.
- `AgentSessionStore` loads/saves only the current `agentscopeSessionId`.
- UI messages are not replayed into the prompt in `AgentFactory` or `AgentScopeRuntimeService`.
- No static/global Agent or Memory is present in the scanned code.
- UI store files are per-session for messages, summaries, and traces.
- Built-in tools and skills are registered on the per-call agent through AgentScope `Toolkit` and `SkillBox`; they are not session memory.

Keep this invariant: the only cross-call Agent state source is AgentScope `JsonSession` for the current session key.

## Local Storage

Configured in `src/main/resources/application.yml`:

```yaml
agent-console:
  base-dir: ${user.dir}/.agents
  ui-store-path: ${user.dir}/.agents/ui-store
  agentscope-session-store-path: ${user.dir}/.agents/agentscope-sessions
  skills-path: ${user.dir}/.agents/skills
```

UI JSON layout:

```text
.agents/ui-store/
  sessions.json
  messages/{sessionId}.json
  summaries/{sessionId}.json
  traces/{sessionId}.json
```

AgentScope state layout:

```text
.agents/agentscope-sessions/
```

`JsonFileUtils.writeAtomic(...)` writes via temp file and move/replace. Store methods are synchronized.

## Skills And Tools

Project-local skills live under:

```text
.agents/skills/
  agent-console-agent/SKILL.md
  conventional-commit/SKILL.md
  nano-memory/SKILL.md
  skills-creator/SKILL.md
```

`AgentFactory.createSkillBox(...)` uses AgentScope `FileSystemSkillRepository` to read this directory and register every skill into a `SkillBox`.

Built-in Java tools live under:

```text
src/main/java/com/github/quynj/agentconsole/tool/
```

Registered tool beans:

- `CalculatorTools`
- `DateTimeTools`
- `ListFileTool`
- `SystemInfoTools`

Additional AgentScope tools registered by `AgentFactory.createToolkit(...)`:

- `ReadFileTool`, exposing `view_text_file` and `list_directory`
- `WriteFileTool`, exposing `write_text_file` and `insert_text_file`
- `ContextOffloadTool`, exposing `context_reload` when the created memory is `AutoContextMemory`

`AgentFactory.createToolkit(...)` registers these beans/tools with AgentScope `Toolkit.registerTool(...)`.

## Frontend Map

- `frontend/src/views/AgentConsoleView.vue`: three-column shell.
- `frontend/src/components/session`: session sidebar and items.
- `frontend/src/components/chat`: chat feed, input, message cards, content block renderers.
- `frontend/src/components/data-view`: Summary / Message / Trace tabs.
- `frontend/src/stores/sessionStore.ts`: session list, active session, summary.
- `frontend/src/stores/messageStore.ts`: messages and send state.
- `frontend/src/stores/runtimeStore.ts`: SSE lifecycle and trace loading.
- `frontend/src/api`: REST and SSE clients.

## API Surface

Implemented endpoints:

- `GET /api/sessions`
- `POST /api/sessions`
- `GET /api/sessions/{sessionId}`
- `PATCH /api/sessions/{sessionId}`
- `DELETE /api/sessions/{sessionId}`
- `GET /api/sessions/{sessionId}/summary`
- `GET /api/sessions/{sessionId}/messages`
- `POST /api/sessions/{sessionId}/messages`
- `GET /api/messages/{messageId}`
- `GET /api/sessions/{sessionId}/events`
- `GET /api/sessions/{sessionId}/traces`
- `GET /api/traces/{traceId}`

SSE event names:

- `message.created`
- `message.delta`
- `session.updated`
- `error`

`trace.created` is not implemented yet.
