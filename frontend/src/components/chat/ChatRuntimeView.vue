<template>
  <section class="chat-runtime">
    <header class="chat-header">
      <div>
        <p class="eyebrow">{{ sessionStore.activeSession?.status || 'idle' }}</p>
        <h2>{{ sessionStore.activeSession?.title || 'New Chat' }}</h2>
      </div>
      <el-tag effect="dark">{{ sessionStore.activeSession?.modelName || 'model' }}</el-tag>
    </header>
    <div class="message-feed">
      <el-empty v-if="messageStore.messages.length === 0" description="No messages yet" />
      <MessageCard
        v-for="message in messageStore.messages"
        :key="message.id"
        :message="message"
        :active="message.id === messageStore.selectedMessage?.id"
        @click="messageStore.select(message)"
      />
    </div>
    <ChatInputBox :disabled="!sessionStore.activeSessionId" :loading="messageStore.sending" @send="send" />
  </section>
</template>

<script setup lang="ts">
import ChatInputBox from './ChatInputBox.vue'
import MessageCard from './MessageCard.vue'
import { useMessageStore } from '../../stores/messageStore'
import { useSessionStore } from '../../stores/sessionStore'

const sessionStore = useSessionStore()
const messageStore = useMessageStore()

async function send(text: string) {
  if (!sessionStore.activeSessionId) return
  await messageStore.send(sessionStore.activeSessionId, text)
  await sessionStore.refreshSummary()
}
</script>
