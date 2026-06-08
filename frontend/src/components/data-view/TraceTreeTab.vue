<template>
  <el-empty v-if="runtimeStore.traces.length === 0" description="No trace events yet" />
  <div v-else class="trace-layout">
    <div class="trace-list">
      <button
        v-for="trace in orderedTraces"
        :key="trace.id"
        type="button"
        class="trace-row"
        :class="[trace.status, { active: trace.id === selectedId }]"
        @click="selectedId = trace.id"
      >
        <span class="trace-icon">
          <LoaderCircle v-if="trace.status === 'running'" :size="15" />
          <CheckCircle v-else-if="trace.status === 'success'" :size="15" />
          <XCircle v-else-if="trace.status === 'error'" :size="15" />
          <CircleSlash v-else :size="15" />
        </span>
        <span class="trace-main">
          <strong>{{ trace.name }}</strong>
          <small>{{ trace.spanType }} · {{ timeLabel(trace) }}</small>
        </span>
        <span class="trace-duration">{{ durationLabel(trace) }}</span>
      </button>
    </div>

    <div v-if="selectedTrace" class="trace-detail">
      <dl class="kv-list compact">
        <dt>id</dt>
        <dd>{{ selectedTrace.id }}</dd>
        <dt>type</dt>
        <dd>{{ selectedTrace.spanType }}</dd>
        <dt>status</dt>
        <dd>{{ selectedTrace.status }}</dd>
        <dt>parent</dt>
        <dd>{{ selectedTrace.parentSpanId || '-' }}</dd>
      </dl>

      <h3>input</h3>
      <RawJsonViewer :value="selectedTrace.input ?? {}" />
      <h3>output</h3>
      <RawJsonViewer :value="selectedTrace.output ?? {}" />
      <h3>metadata</h3>
      <RawJsonViewer :value="selectedTrace.metadata ?? {}" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { CheckCircle, CircleSlash, LoaderCircle, XCircle } from 'lucide-vue-next'
import RawJsonViewer from '../chat/blocks/RawJsonViewer.vue'
import { useRuntimeStore } from '../../stores/runtimeStore'
import type { TraceSpan } from '../../types/trace'

const runtimeStore = useRuntimeStore()
const selectedId = ref('')

const orderedTraces = computed(() =>
  [...runtimeStore.traces].sort((left, right) => Date.parse(left.startedAt) - Date.parse(right.startedAt))
)

const selectedTrace = computed(() =>
  orderedTraces.value.find((trace) => trace.id === selectedId.value) || orderedTraces.value.at(-1)
)

watch(
  () => runtimeStore.traces.map((trace) => trace.id).join(','),
  () => {
    if (!selectedId.value || !runtimeStore.traces.some((trace) => trace.id === selectedId.value)) {
      selectedId.value = orderedTraces.value.at(-1)?.id || ''
    }
  },
  { immediate: true }
)

function durationLabel(trace: TraceSpan) {
  if (trace.status === 'running') return 'running'
  if (typeof trace.durationMs !== 'number') return '-'
  return `${trace.durationMs}ms`
}

function timeLabel(trace: TraceSpan) {
  const startedAt = new Date(trace.startedAt)
  if (Number.isNaN(startedAt.getTime())) return '-'
  return startedAt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}
</script>
