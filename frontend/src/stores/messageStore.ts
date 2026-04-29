import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listMessages, sendMessage } from '../api/message'
import type { AgentMessage } from '../types/message'

export const useMessageStore = defineStore('messages', () => {
  const messages = ref<AgentMessage[]>([])
  const selectedMessage = ref<AgentMessage>()
  const sending = ref(false)

  async function load(sessionId: string) {
    messages.value = await listMessages(sessionId)
    selectedMessage.value = undefined
  }

  async function send(sessionId: string, text: string) {
    sending.value = true
    try {
      const response = await sendMessage(sessionId, text, true)
      upsert(response.message)
    } finally {
      sending.value = false
    }
  }

  function upsert(message: AgentMessage) {
    if (messages.value.some((item) => item.id === message.id)) return
    messages.value.push(message)
  }

  function select(message: AgentMessage) {
    selectedMessage.value = message
  }

  return { messages, selectedMessage, sending, load, send, upsert, select }
})
