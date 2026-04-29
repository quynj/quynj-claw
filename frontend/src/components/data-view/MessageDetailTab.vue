<template>
  <div v-if="messageStore.selectedMessage" class="detail-stack">
    <dl class="kv-list compact">
      <dt>id</dt>
      <dd>{{ messageStore.selectedMessage.id }}</dd>
      <dt>role</dt>
      <dd>{{ messageStore.selectedMessage.role }}</dd>
      <dt>name</dt>
      <dd>{{ messageStore.selectedMessage.name }}</dd>
    </dl>
    <h3>metadata</h3>
    <RawJsonViewer :value="messageStore.selectedMessage.metadata" />
    <h3>content</h3>
    <RawJsonViewer :value="messageStore.selectedMessage.content" />
    <h3>rawMsg</h3>
    <RawJsonViewer :value="messageStore.selectedMessage.rawMsg" />
  </div>
  <el-empty v-else description="Select a message" />
</template>

<script setup lang="ts">
import RawJsonViewer from '../chat/blocks/RawJsonViewer.vue'
import { useMessageStore } from '../../stores/messageStore'
const messageStore = useMessageStore()
</script>
