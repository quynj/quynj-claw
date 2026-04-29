<template>
  <dl v-if="sessionStore.summary" class="kv-list">
    <template v-for="(value, key) in sessionStore.summary" :key="key">
      <dt>{{ key }}</dt>
      <dd>{{ value }}</dd>
    </template>
  </dl>
  <el-empty v-else description="No summary" />
</template>

<script setup lang="ts">
import { useSessionStore } from '../../stores/sessionStore'
const sessionStore = useSessionStore()
</script>
