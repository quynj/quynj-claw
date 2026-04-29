import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { createSession, deleteSession, getSummary, listSessions, updateSession } from '../api/session'
import type { ChatSession, SessionSummary } from '../types/session'

export const useSessionStore = defineStore('sessions', () => {
  const sessions = ref<ChatSession[]>([])
  const activeSessionId = ref<string>()
  const summary = ref<SessionSummary>()
  const keyword = ref('')

  const activeSession = computed(() => sessions.value.find((item) => item.id === activeSessionId.value))

  async function loadSessions() {
    const page = await listSessions(keyword.value)
    sessions.value = page.items
    if (!activeSessionId.value && sessions.value.length > 0) {
      activeSessionId.value = sessions.value[0].id
    }
  }

  async function refreshSummary() {
    if (!activeSessionId.value) return
    summary.value = await getSummary(activeSessionId.value)
  }

  async function newSession() {
    const session = await createSession({ title: 'New Chat' })
    sessions.value.unshift(session)
    activeSessionId.value = session.id
    summary.value = await getSummary(session.id)
  }

  async function renameSession(id: string, title: string) {
    const updated = await updateSession(id, { title })
    replaceSession(updated)
    if (activeSessionId.value === id) await refreshSummary()
  }

  async function removeSession(id: string) {
    await deleteSession(id)
    sessions.value = sessions.value.filter((item) => item.id !== id)
    if (activeSessionId.value === id) {
      activeSessionId.value = sessions.value[0]?.id
      summary.value = undefined
    }
  }

  function replaceSession(session: ChatSession) {
    const index = sessions.value.findIndex((item) => item.id === session.id)
    if (index >= 0) sessions.value[index] = session
    else sessions.value.unshift(session)
  }

  return {
    sessions,
    activeSessionId,
    activeSession,
    summary,
    keyword,
    loadSessions,
    refreshSummary,
    newSession,
    renameSession,
    removeSession,
    replaceSession
  }
})
