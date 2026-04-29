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

  async function open(sessionId: string) {
    close()
    const messageStore = useMessageStore()
    const sessionStore = useSessionStore()
    await Promise.all([messageStore.load(sessionId), sessionStore.refreshSummary(), refreshTraces(sessionId)])
    eventSource.value = subscribeSessionEvents(sessionId, {
      'message.created': ({ message }) => messageStore.upsert(message),
      'session.updated': ({ session }) => {
        sessionStore.replaceSession(session)
        sessionStore.refreshSummary()
      },
      error: ({ message, detail }) => {
        lastError.value = detail || message
      }
    })
  }

  async function refreshTraces(sessionId: string) {
    traces.value = await listTraces(sessionId)
  }

  function close() {
    eventSource.value?.close()
    eventSource.value = undefined
  }

  return { traces, lastError, open, close, refreshTraces }
})
