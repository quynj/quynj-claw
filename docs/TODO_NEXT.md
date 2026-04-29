# TODO Next

This list is ordered by smallest useful next step first.

## Recently Completed

- Message projection freshness in the current chat view is fixed.
  - Backend publishes `message.created` for the saved user projection immediately after append.
  - Frontend still upserts returned assistant messages and SSE messages by ID, so duplicate assistant inserts are avoided.
  - Frontend guards against stale session loads, stale POST responses, and stale SSE events when the user switches sessions quickly.
- `messageCount` now counts UI messages, not only assistant responses.
  - Successful turns add two messages: user + assistant.
  - Failed turns add two messages: user + error.
  - Session and summary counts stay consistent because summary mirrors the session store.
- A focused backend smoke test now covers session/message/summary/delete lifecycle without starting the model runtime.

## Next Smallest Task

Add a simple frontend smoke test or documented manual QA path for create/send/switch/delete.

## High Priority

- Wire `agent-console.memory.max-context-tokens` into `AgentMemoryFactory` if the official AgentScope Java API supports it.
- Recheck AgentScope Java official docs/source for `AutoContextMemory`, `AutoContextConfig`, `JsonSession`, and model builders before changing integration code.

## AgentScope Integration TODOs

- Confirm whether `new AutoContextMemory(config, model)` is still the recommended AgentScope Java API.
- Confirm whether `JsonSession` should be constructed with a root path or per-session path.
- Confirm whether `session.delete(SimpleSessionKey.of(sessionId))` is the official deletion path.
- Map AgentScope tool use and tool result blocks into `ContentBlockDTO` instead of text-only fallback.
- Add real telemetry/trace mapping from AgentScope observability if available.
- Evaluate stream support in AgentScope Java and add `message.delta` only if supported by the official API.

## Frontend TODOs

- Show runtime errors from `runtimeStore.lastError` in the chat view.
- Improve empty/error/loading states for Summary and Trace tabs.
- Consider route setup if future navigation grows beyond the single `AgentConsoleView`.
- Consider chunk splitting if Vite large chunk warning becomes operationally annoying.

## Operational TODOs

- Document model provider setup for Ollama, DashScope, and OpenAI separately.
- Keep Bun as the canonical frontend package workflow and avoid reintroducing `package-lock.json`.
- Add a README or link `AGENTS.md` from README if this becomes a public-facing repo.
