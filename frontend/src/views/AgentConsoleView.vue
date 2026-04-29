<template>
  <main class="console-shell">
    <SessionSidebar />
    <ChatRuntimeView />
    <DataViewPanel />
  </main>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, watch } from 'vue'
import SessionSidebar from '../components/session/SessionSidebar.vue'
import ChatRuntimeView from '../components/chat/ChatRuntimeView.vue'
import DataViewPanel from '../components/data-view/DataViewPanel.vue'
import { useRuntimeStore } from '../stores/runtimeStore'
import { useSessionStore } from '../stores/sessionStore'

const sessionStore = useSessionStore()
const runtimeStore = useRuntimeStore()

onMounted(async () => {
  await sessionStore.loadSessions()
  if (!sessionStore.activeSessionId) {
    await sessionStore.newSession()
  }
})

watch(
  () => sessionStore.activeSessionId,
  (id) => {
    if (id) runtimeStore.open(id)
  },
  { immediate: true }
)

onBeforeUnmount(() => runtimeStore.close())
</script>
