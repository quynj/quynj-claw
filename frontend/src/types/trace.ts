export interface TraceSpan {
  id: string
  sessionId: string
  parentSpanId?: string
  name: string
  spanType: 'agent' | 'llm' | 'tool' | 'memory' | 'retriever' | 'workflow' | 'system'
  status: 'running' | 'success' | 'error' | 'cancelled'
  input?: unknown
  output?: unknown
  metadata?: Record<string, unknown>
  durationMs?: number
  startedAt: string
  endedAt?: string
  children?: TraceSpan[]
}
