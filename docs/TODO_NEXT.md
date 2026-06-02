# TODO Next

This list is ordered by smallest useful next step first.

## Recently Completed

- Center chat UI refresh.
  - The grid-line background was replaced with a softer layered surface.
  - Message cards now show avatar, display name, and timestamp.
  - User messages display as `You`; assistant messages use the configured agent name.
  - Message role/type labels such as `user` and `assistant` are no longer shown in the feed.
  - Ten static SVG avatars were added under `frontend/src/assets/avatars`.
  - The input toolbar More menu currently exposes one action: `随机头像`, which randomizes the user and agent avatars together.
  - Avatar UI selection is client-only state persisted in `localStorage`.
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

Add a simple frontend smoke test or documented manual QA path for create/send/switch/delete plus the chat UI controls:

- Verify the input toolbar button spacing remains visually uniform.
- Verify the More menu opens and `随机头像` updates both visible avatars.
- Verify the avatar choice survives a browser refresh through `localStorage`.

## High Priority

- Consider exposing additional AutoContext tuning settings if real sessions show the current defaults are too aggressive or too loose.
- Recheck AgentScope Java official docs/source for `AutoContextMemory`, `AutoContextConfig`, `JsonSession`, and model builders before changing integration code.

## AgentScope Integration TODOs

- Confirm whether `new AutoContextMemory(config, model)` is still the recommended AgentScope Java API.
- Confirm whether `JsonSession` should be constructed with a root path or per-session path.
- Confirm whether `session.delete(SimpleSessionKey.of(sessionId))` is the official deletion path.
- Refine provider-specific AgentScope content block mapping as real tool-use payloads are observed.
- Add real telemetry/trace mapping from AgentScope observability if available.
- Improve streaming from event-level updates to token/text incremental updates if AgentScope Java exposes suitable deltas.

## Frontend TODOs

- Expand the input toolbar More menu only with actions that fit the chat workflow, for example:
  - clear local avatar preference
  - toggle compact message density
  - toggle timestamps or rich metadata
  - export/copy current session projection
- Add an explicit visual QA checklist for desktop widths because the app currently assumes a `min-width` desktop console layout.
- Show runtime errors from `runtimeStore.lastError` in the chat view.
- Improve empty/error/loading states for Summary and Trace tabs.
- Consider route setup if future navigation grows beyond the single `QuynjClawView`.
- Consider chunk splitting if Vite large chunk warning becomes operationally annoying.

## Operational TODOs

- Document model provider setup for Ollama, DashScope, and OpenAI separately.
- Keep Bun as the canonical frontend package workflow and avoid reintroducing `package-lock.json`.
- Add a README or link `AGENTS.md` from README if this becomes a public-facing repo.
