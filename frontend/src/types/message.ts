export type MessageRole = 'system' | 'user' | 'assistant' | 'tool'

export type ContentBlock =
  | TextBlock
  | ToolUseBlock
  | ToolResultBlock
  | ErrorBlock
  | ImageBlock
  | AudioBlock
  | VideoBlock

export interface TextBlock {
  type: 'text'
  text: string
}

export interface ToolUseBlock {
  type: 'tool_use'
  id: string
  name: string
  input: Record<string, unknown>
}

export interface ToolResultBlock {
  type: 'tool_result'
  toolUseId: string
  name: string
  output: unknown
  isError?: boolean
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
