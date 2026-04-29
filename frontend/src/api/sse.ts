export function subscribeSessionEvents(sessionId: string, handlers: Record<string, (payload: any) => void>) {
  const source = new EventSource(`/api/sessions/${sessionId}/events`)
  for (const [event, handler] of Object.entries(handlers)) {
    source.addEventListener(event, (message) => handler(JSON.parse((message as MessageEvent).data)))
  }
  return source
}
