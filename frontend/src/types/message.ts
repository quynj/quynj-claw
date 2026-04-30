export type MessageRole = 'system' | 'user' | 'assistant' | 'tool'

export type ContentBlock =
  | TextBlock
  | ThinkingBlock
  | ToolUseBlock
  | ToolResultBlock
  | ErrorBlock
  | ImageBlock
  | AudioBlock
  | VideoBlock

export interface TextBlock {
  type: 'text'
  text: string
  metadata?: Record<string, unknown> | null
}

export interface ThinkingBlock {
  type: 'thinking'
  thinking: string
  metadata?: Record<string, unknown> | null
}

export interface ToolUseBlock {
  type: 'tool_use'
  id: string
  name: string
  input: Record<string, unknown>
  content?: string | null
  metadata?: Record<string, unknown> | null
}

export interface ToolResultBlock {
  type: 'tool_result'
  toolUseId: string
  id?: string
  name: string
  output: unknown
  isError?: boolean
  metadata?: Record<string, unknown> | null
}

export interface ErrorBlock {
  type: 'error'
  message: string
  detail?: string
}

export interface ImageBlock {
  type: 'image'
  url: string
  alt?: string
}

export interface AudioBlock {
  type: 'audio'
  url: string
}

export interface VideoBlock {
  type: 'video'
  url: string
}

export interface AgentMessage {
  id: string
  sessionId: string
  name: string
  role: MessageRole
  content: ContentBlock[]
  metadata?: Record<string, unknown>
  rawMsg?: unknown
  createdAt: string
}
