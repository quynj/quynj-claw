<template>
  <section class="chat-runtime">
    <header class="chat-header">
      <div>
        <p class="eyebrow">{{ sessionStore.activeSession?.agentName || 'Sunday' }}</p>
        <h2>{{ sessionStore.activeSession?.title || 'New Chat' }}</h2>
      </div>
      <el-tag effect="dark">{{ sessionStore.activeSession?.modelName || 'model' }}</el-tag>
    </header>
    <div ref="feedRef" class="message-feed">
      <el-empty v-if="messageStore.messages.length === 0" description="No messages yet" />
      <MessageCard
        v-for="message in messageStore.messages"
        :key="message.id"
        :message="message"
        :streaming="messageStore.isStreaming(message.id)"
        :generating="message.id === generatingMessageId"
        :active="message.id === messageStore.selectedMessage?.id"
        @click="messageStore.select(message)"
      />
      <div v-if="showPendingGeneration" class="pending-generation">
        <span />
        <span />
        <span />
        <em>{{ sessionStore.activeSession?.agentName || 'Sunday' }} 正在思考</em>
      </div>
    </div>
    <ChatInputBox
      :session-id="sessionStore.activeSessionId"
      :disabled="!sessionStore.activeSessionId"
      :loading="messageStore.sending"
      :cancelling="messageStore.cancelling"
      :status="sessionStore.activeSession?.status || 'idle'"
      @send="send"
      @cancel="cancel"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import ChatInputBox from './ChatInputBox.vue'
import MessageCard from './MessageCard.vue'
import { useMessageStore } from '../../stores/messageStore'
import { useSessionStore } from '../../stores/sessionStore'
import type { MessageAttachment } from '../../types/message'

const sessionStore = useSessionStore()
const messageStore = useMessageStore()
const feedRef = ref<HTMLElement>()
const generatingMessageId = computed(() =>
  [...messageStore.messages]
    .reverse()
    .find((message) => message.role === 'assistant' && messageStore.isStreaming(message.id))?.id
)
const showPendingGeneration = computed(() => messageStore.sending && !generatingMessageId.value)

watch(
  () => [messageStore.messages.length, JSON.stringify(messageStore.messages), messageStore.streamingIds.length],
  async () => {
    await nextTick()
    const feed = feedRef.value
    if (feed) feed.scrollTop = feed.scrollHeight
  }
)

async function send(text: string, attachments: MessageAttachment[]) {
  if (!sessionStore.activeSessionId) return
  await messageStore.send(sessionStore.activeSessionId, text, attachments)
  await sessionStore.refreshSummary()
}

async function cancel() {
  if (!sessionStore.activeSessionId) return
  await messageStore.cancelSend(sessionStore.activeSessionId)
  await sessionStore.refreshSummary()
}
</script>
