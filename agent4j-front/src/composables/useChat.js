import { reactive, ref } from 'vue'
import { fetchConversations, fetchConversationDetail, streamChat } from '@/api'

const state = reactive({
  conversations: [],
  currentConversationId: null,
  messages: [],
  inputValue: '',
  isStreaming: false
})

let loaded = false

export function useChat() {
  async function loadConversations() {
    state.conversations = await fetchConversations()
  }

  async function selectConversation(id) {
    state.currentConversationId = id
    const detail = await fetchConversationDetail(id)
    state.messages = (detail?.messages || []).map(m => ({
      id: m.id,
      role: m.role,
      content: m.content,
      reasoning: '',
      toolCalls: [],
      isStreaming: false
    }))
  }

  function newConversation() {
    state.currentConversationId = null
    state.messages = []
    state.inputValue = ''
  }

  async function sendMessage() {
    const text = state.inputValue.trim()
    if (!text || state.isStreaming) return

    state.inputValue = ''
    state.isStreaming = true

    // push user message
    state.messages.push({
      id: Date.now(),
      role: 'user',
      content: text,
      reasoning: '',
      toolCalls: [],
      isStreaming: false
    })

    // push placeholder assistant message
    state.messages.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: '',
      reasoning: '',
      toolCalls: [],
      isStreaming: true
    })
    // take reactive proxy reference — mutations on this trigger Vue re-renders
    const msg = state.messages[state.messages.length - 1]

    try {
      await streamChat(state.currentConversationId, text, (event) => {
        switch (event.type) {
          case 'reasoning':
            msg.reasoning += event.content || ''
            break
          case 'tool_call':
            {
              const idx = event.index ?? 0
              const existing = msg.toolCalls.find(tc => tc._index === idx)
              if (existing) {
                existing.toolName = event.toolName || existing.toolName
                existing.toolArgs = event.toolArgs || existing.toolArgs
              } else {
                msg.toolCalls.push({ _index: idx, toolName: event.toolName, toolArgs: event.toolArgs })
              }
            }
            break
          case 'token':
            msg.content += event.content || ''
            break
          case 'complete':
            msg.content = event.content || msg.content
            msg.isStreaming = false
            if (event.conversationId && !state.currentConversationId) {
              state.currentConversationId = event.conversationId
            }
            loadConversations()
            break
          case 'error':
            msg.content = event.content || 'Unknown error'
            msg.isStreaming = false
            break
        }
      })
    } catch (e) {
      msg.content = 'Request failed: ' + e.message
      msg.isStreaming = false
    } finally {
      state.isStreaming = false
    }
  }

  // auto-load on first use
  if (!loaded) {
    loaded = true
    loadConversations()
  }

  return { state, loadConversations, selectConversation, newConversation, sendMessage }
}
