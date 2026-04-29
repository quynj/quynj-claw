<template>
  <aside class="session-sidebar">
    <div class="sidebar-top">
      <div>
        <p class="eyebrow">AgentScope</p>
        <h1>Agent Console</h1>
      </div>
      <el-button :icon="Plus" circle @click="sessionStore.newSession" />
    </div>
    <el-input v-model="sessionStore.keyword" placeholder="Search sessions" :prefix-icon="Search" @change="sessionStore.loadSessions" />
    <div class="session-list">
      <SessionItem
        v-for="session in sessionStore.sessions"
        :key="session.id"
        :session="session"
        :active="session.id === sessionStore.activeSessionId"
        @select="sessionStore.activeSessionId = session.id"
        @rename="rename"
        @delete="sessionStore.removeSession"
      />
    </div>
  </aside>
</template>

<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import SessionItem from './SessionItem.vue'
import { useSessionStore } from '../../stores/sessionStore'

const sessionStore = useSessionStore()

async function rename(id: string, currentTitle: string) {
  const { value } = await ElMessageBox.prompt('Rename session', 'Session', {
    inputValue: currentTitle,
    inputPattern: /\S+/,
    inputErrorMessage: 'Title is required'
  })
  await sessionStore.renameSession(id, value)
}
</script>
