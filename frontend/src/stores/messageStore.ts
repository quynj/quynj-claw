import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listMessages, sendMessage } from '../api/message'
import { useSessionStore } from './sessionStore'
import type { AgentMessage } from '../types/message'

export const useMessageStore = defineStore('messages', () => {
  const messages = ref<AgentMessage[]>([])
  const selectedMessage = ref<AgentMessage>()
  const streamingIds = ref<string[]>([])
  const sending = ref(false)

  async function load(sessionId: string) {
    const loaded = await listMessages(sessionId)
    if (useSessionStore().activeSessionId !== sessionId) return
    messages.value = loaded
    streamingIds.value = []
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
    const index = messages.value.findIndex((item) => item.id === message.id)
    if (index >= 0) {
      messages.value[index] = message
      stopStreaming(message.id)
      return
    }
    messages.value.push(message)
  }

  function mergeDelta(message: AgentMessage, last: boolean, expectedSessionId?: string) {
    if (expectedSessionId && message.sessionId !== expectedSessionId) return
    if (useSessionStore().activeSessionId !== message.sessionId) return
    const existing = messages.value.find((item) => item.id === message.id)
    if (!existing) {
      messages.value.push(message)
    } else {
      existing.name = message.name
      existing.role = message.role
      existing.metadata = message.metadata
      existing.rawMsg = message.rawMsg
      for (const block of message.content) {
        const index = existing.content.findIndex((item) => blockKey(item) === blockKey(block))
        if (index >= 0) {
          existing.content[index] = block
        } else {
          existing.content.push(block)
        }
      }
    }
    if (last) stopStreaming(message.id)
    else startStreaming(message.id)
  }

  function select(message: AgentMessage) {
    selectedMessage.value = message
  }

  function startStreaming(id: string) {
    if (!streamingIds.value.includes(id)) streamingIds.value.push(id)
  }

  function stopStreaming(id: string) {
    streamingIds.value = streamingIds.value.filter((item) => item !== id)
  }

  function isStreaming(id: string) {
    return streamingIds.value.includes(id)
  }

  function blockKey(block: AgentMessage['content'][number]) {
    if (block.type === 'tool_use') return `${block.type}:${block.id}`
    if (block.type === 'tool_result') return `${block.type}:${block.toolUseId || block.id || block.name}`
    return block.type
  }

  return { messages, selectedMessage, streamingIds, sending, load, send, upsert, mergeDelta, select, isStreaming }
})
