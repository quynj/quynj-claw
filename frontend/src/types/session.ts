export interface ChatSession {
  id: string
  title: string
  agentName: string
  agentscopeSessionId: string
  status: 'idle' | 'running' | 'done' | 'error' | 'stopped'
  modelName?: string
  systemPrompt?: string
  temperature?: number
  messageCount: number
  traceCount: number
  totalTokens: number
  promptTokens: number
  completionTokens: number
  durationMs: number
  lastMessagePreview?: string
  createdAt: string
  updatedAt: string
  deletedAt?: string | null
}

export interface SessionSummary {
  sessionId: string
  title: string
  agentName: string
  status: string
  messageCount: number
  traceCount: number
  totalTokens: number
  promptTokens: number
  completionTokens: number
  durationMs: number
  createdAt: string
  updatedAt: string
}
