<template>
  <aside class="data-panel">
    <el-tabs v-model="tab" stretch>
      <el-tab-pane label="Summary" name="summary">
        <SessionSummaryTab />
      </el-tab-pane>
      <el-tab-pane label="Message" name="message">
        <MessageDetailTab />
      </el-tab-pane>
      <el-tab-pane label="Trace" name="trace">
        <TraceTreeTab />
      </el-tab-pane>
    </el-tabs>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SessionSummaryTab from './SessionSummaryTab.vue'
import MessageDetailTab from './MessageDetailTab.vue'
import TraceTreeTab from './TraceTreeTab.vue'

const tab = ref('summary')
</script>
