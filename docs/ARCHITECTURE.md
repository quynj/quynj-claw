# Architecture

## Backend Package Map

Base package: `com.github.quynj.quynjclaw`

- `api`
  - `SessionController`: session CRUD and summary endpoint.
  - `ChatController`: send message endpoint.
  - `MessageController`: list/find UI message projections.
  - `TraceController`: trace list/find endpoints.
  - `SseController`: session event stream.
  - `GlobalExceptionHandler`: unified `Result` error responses.
- `application`
  - `ConversationService`: coordinates UI store lifecycle and status updates.
  - `AgentScopeRuntimeService`: chat flow through AgentScope.
  - `MessageProjectionService`: UI message projection access.
  - `SummaryService`: summary access.
  - `RealtimeEventService`: per-session Spring `SseEmitter` registry.
  - `TraceService`: records console-level runtime trace spans and publishes trace SSE events.
- `agentscope`
  - `AgentFactory`: builds a fresh `ReActAgent` per request.
  - `AgentMemoryFactory`: creates `AutoContextMemory`, with `InMemoryMemory` fallback.
  - `AgentSessionStore`: wraps AgentScope `JsonSession`.
  - `AgentScopeMessageMapper`: maps frontend DTOs to/from AgentScope `Msg`.
  - `AgentScopeTraceMapper`: reserved mapper for future AgentScope telemetry span conversion.
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
4. `TraceService` records a root chat-request span and console-level runtime spans such as message persistence, AgentScope agent creation, session load/save, blocking call, stream call, stream events, errors, and cancellation.
5. `ConversationService.markRunning(...)` updates UI session status.
6. `RealtimeEventService.publishSessionUpdated(...)` sends SSE.
7. `AgentFactory.createAgent(session)` creates a fresh `ReActAgent`.
8. `AgentFactory` registers built-in Java tools into an AgentScope `Toolkit`.
9. `AgentFactory` loads project-local skills from `.agents/skills` into an AgentScope `SkillBox`.
10. `AgentSessionStore.loadIfExists(agent, session.agentscopeSessionId)` loads only this AgentScope session.
11. Runtime delegates execution to AgentScope Java:
    - non-streaming requests use `agent.call(userMsg).block()`.
    - streaming requests use `agent.stream(userMsg, StreamOptions...)`, publish `message.delta`, and persist completed messages after the stream finishes.
12. `AgentSessionStore.save(agent, session.agentscopeSessionId)` saves only this AgentScope session.
13. The AgentScope `Msg` is mapped to an assistant UI projection.
14. Message/session/summary/trace stores are updated.
15. SSE emits `message.created`, `session.updated`, `trace.created`, and `trace.updated`.

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
quynj-claw:
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
  quynj-claw-agent/SKILL.md
  conventional-commit/SKILL.md
  nano-memory/SKILL.md
  skills-creator/SKILL.md
```

`AgentFactory.createSkillBox(...)` uses AgentScope `FileSystemSkillRepository` to read this directory and register every skill into a `SkillBox`.

Built-in Java tools live under:

```text
src/main/java/com/github/quynj/quynjclaw/tool/
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

- `frontend/src/views/QuynjClawView.vue`: three-column shell.
- `frontend/src/components/session`: session sidebar and items.
- `frontend/src/components/chat`: chat feed, input, message cards, content block renderers.
- `frontend/src/components/chat/MessageCard.vue`: role-aware presentation for message cards. It displays `You` for user messages, the configured agent name for assistant messages, and hides raw role/type labels from the chat feed.
- `frontend/src/components/chat/ChatInputBox.vue`: message composer with image attachments, voice input when supported, send/cancel controls, and a More menu for local UI actions.
- `frontend/src/composables/useChatAvatars.ts`: client-only avatar selection state. It references static avatar assets, randomizes the user/agent avatar pair, and persists the choice in `localStorage`.
- `frontend/src/assets/avatars`: committed static SVG avatars used by the chat feed.
- `frontend/src/components/data-view`: Summary / Message / Trace tabs.
- `frontend/src/stores/sessionStore.ts`: session list, active session, summary.
- `frontend/src/stores/messageStore.ts`: messages and send state.
- `frontend/src/stores/runtimeStore.ts`: SSE lifecycle and trace loading.
- `frontend/src/api`: REST and SSE clients.

Avatar state is intentionally not part of backend DTOs, UI JSON projection files, or AgentScope session state. It is local presentation state only.

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
- `trace.created`
- `trace.updated`
- `session.updated`
- `error`
