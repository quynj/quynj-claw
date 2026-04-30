<template>
  <section class="chat-runtime">
    <header class="chat-header">
      <div>
        <p class="eyebrow">{{ sessionStore.activeSession?.status || 'idle' }}</p>
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
        :active="message.id === messageStore.selectedMessage?.id"
        @click="messageStore.select(message)"
      />
    </div>
    <ChatInputBox :disabled="!sessionStore.activeSessionId" :loading="messageStore.sending" @send="send" />
  </section>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import ChatInputBox from './ChatInputBox.vue'
import MessageCard from './MessageCard.vue'
import { useMessageStore } from '../../stores/messageStore'
import { useSessionStore } from '../../stores/sessionStore'

const sessionStore = useSessionStore()
const messageStore = useMessageStore()
const feedRef = ref<HTMLElement>()

watch(
  () => [messageStore.messages.length, JSON.stringify(messageStore.messages), messageStore.streamingIds.length],
  async () => {
    await nextTick()
    const feed = feedRef.value
    if (feed) feed.scrollTop = feed.scrollHeight
  }
)

async function send(text: string) {
  if (!sessionStore.activeSessionId) return
  await messageStore.send(sessionStore.activeSessionId, text)
  await sessionStore.refreshSummary()
}
</script>
