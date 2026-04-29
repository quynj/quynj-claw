import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listMessages, sendMessage } from '../api/message'
import { useSessionStore } from './sessionStore'
import type { AgentMessage } from '../types/message'

export const useMessageStore = defineStore('messages', () => {
  const messages = ref<AgentMessage[]>([])
  const selectedMessage = ref<AgentMessage>()
  const sending = ref(false)

  async function load(sessionId: string) {
    const loaded = await listMessages(sessionId)
    if (useSessionStore().activeSessionId !== sessionId) return
    messages.value = loaded
    selectedMessage.value = undefined
  }

  async function send(sessionId: string, text: string) {
    sending.value = true
    try {
      const response = await sendMessage(sessionId, text, true)
      upsert(response.message, sessionId)
    } finally {
      sending.value = false
    }
  }

  function upsert(message: AgentMessage, expectedSessionId?: string) {
    if (expectedSessionId && message.sessionId !== expectedSessionId) return
    if (useSessionStore().activeSessionId !== message.sessionId) return
    if (messages.value.some((item) => item.id === message.id)) return
    messages.value.push(message)
  }

  function select(message: AgentMessage) {
    selectedMessage.value = message
  }

  return { messages, selectedMessage, sending, load, send, upsert, select }
})
