const USER_ID = 'user123'
const headers = { 'X-User-Id': USER_ID }

export async function fetchConversations() {
  const res = await fetch('/api/conversations', { headers })
  const json = await res.json()
  return json.data || []
}

export async function fetchConversationDetail(id) {
  const res = await fetch(`/api/conversations/${id}`, { headers })
  const json = await res.json()
  return json.data
}

export async function sendChat(conversationId, message) {
  const res = await fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-User-Id': USER_ID },
    body: JSON.stringify({ conversationId, message })
  })
  const json = await res.json()
  return json.data
}

export async function streamChat(conversationId, message, onEvent) {
  const res = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-User-Id': USER_ID },
    body: JSON.stringify({ conversationId, message })
  })

  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`)
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n\n')
    buffer = parts.pop()

    for (const part of parts) {
      for (const line of part.split('\n')) {
        if (line.startsWith('data:')) {
          const jsonStr = line.slice(5).trim()
          if (jsonStr) {
            try {
              onEvent(JSON.parse(jsonStr))
            } catch (e) {
              console.warn('Failed to parse SSE data:', jsonStr)
            }
          }
        }
      }
    }
  }

  // handle remaining buffer
  if (buffer.trim()) {
    for (const line of buffer.split('\n')) {
      if (line.startsWith('data:')) {
        const jsonStr = line.slice(5).trim()
        if (jsonStr) {
          try { onEvent(JSON.parse(jsonStr)) } catch (e) { /* ignore */ }
        }
      }
    }
  }
}
