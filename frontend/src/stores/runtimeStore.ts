import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listTraces } from '../api/trace'
import { subscribeSessionEvents } from '../api/sse'
import { useMessageStore } from './messageStore'
import { useSessionStore } from './sessionStore'
import type { TraceSpan } from '../types/trace'

export const useRuntimeStore = defineStore('runtime', () => {
  const traces = ref<TraceSpan[]>([])
  const eventSource = ref<EventSource>()
  const lastError = ref('')
  let openVersion = 0

  async function open(sessionId: string) {
    close()
    const version = ++openVersion
    const messageStore = useMessageStore()
    const sessionStore = useSessionStore()
    await Promise.all([messageStore.load(sessionId), sessionStore.refreshSummary(), refreshTraces(sessionId)])
    if (version !== openVersion || sessionStore.activeSessionId !== sessionId) return
    eventSource.value = subscribeSessionEvents(sessionId, {
      'message.created': ({ message }) => messageStore.upsert(message, sessionId),
      'message.delta': ({ message, last }) => messageStore.mergeDelta(message, Boolean(last), sessionId),
      'trace.created': ({ trace }) => upsertTrace(trace, sessionId),
      'trace.updated': ({ trace }) => upsertTrace(trace, sessionId),
      'session.updated': ({ session }) => {
        if (sessionStore.activeSessionId !== session.id) return
        sessionStore.replaceSession(session)
        sessionStore.refreshSummary()
      },
      error: ({ message, detail }) => {
        lastError.value = detail || message
      }
    })
  }

  async function refreshTraces(sessionId: string) {
    const loaded = await listTraces(sessionId)
    if (useSessionStore().activeSessionId !== sessionId) return
    traces.value = loaded
  }

  function upsertTrace(trace: TraceSpan, sessionId: string) {
    if (!trace || useSessionStore().activeSessionId !== sessionId) return
    const index = traces.value.findIndex((item) => item.id === trace.id)
    if (index >= 0) {
      traces.value[index] = trace
    } else {
      traces.value.push(trace)
    }
  }

  function close() {
    openVersion += 1
    eventSource.value?.close()
    eventSource.value = undefined
  }

  return { traces, lastError, open, close, refreshTraces }
})
