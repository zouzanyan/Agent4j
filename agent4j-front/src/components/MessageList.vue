<template>
  <div ref="scrollRef" class="message-list" @scroll="onScroll">
    <div class="message-list-inner">
      <div v-if="messages.length === 0" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><ChatDotRound /></el-icon>
        <p>Send a message to start</p>
      </div>
      <MessageItem v-for="msg in messages" :key="msg.id" :message="msg" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import MessageItem from './MessageItem.vue'

const props = defineProps({ messages: Array })
const scrollRef = ref(null)
let isNearBottom = true

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value && isNearBottom) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  })
}

// new message → always scroll down
watch(
  () => props.messages.length,
  () => {
    isNearBottom = true
    scrollToBottom()
  }
)

// streaming content → only scroll if user is near bottom
watch(
  () => props.messages.reduce((acc, m) => acc + m.content.length + m.reasoning.length + m.toolCalls.length, 0),
  () => scrollToBottom()
)

function onScroll() {
  const el = scrollRef.value
  if (!el) return
  isNearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 100
}
</script>

<style scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  background: #f5f5f5;
}
.message-list-inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #c0c4cc;
  gap: 12px;
  font-size: 14px;
}
</style>
