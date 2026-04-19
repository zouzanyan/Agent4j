<template>
  <div :class="['message-item', message.role]">
    <div class="message-avatar">
      <el-avatar v-if="message.role === 'user'" :size="36" style="background: #95ec69">
        <span style="color: #000; font-weight: 600; font-size: 14px">U</span>
      </el-avatar>
      <el-avatar v-else :size="36" style="background: #409eff">
        <span style="color: #fff; font-weight: 600; font-size: 14px">AI</span>
      </el-avatar>
    </div>
    <div class="message-body">
      <!-- reasoning block -->
      <ReasoningBlock
        v-if="message.reasoning"
        :content="message.reasoning"
        :streaming="message.isStreaming && !message.content"
      />

      <!-- tool call indicators -->
      <div v-if="message.toolCalls.length" class="tool-calls">
        <ToolCallIndicator
          v-for="(tc, i) in message.toolCalls"
          :key="i"
          :tool-name="tc.toolName"
          :tool-args="tc.toolArgs"
        />
      </div>

      <!-- user bubble -->
      <div v-if="message.role === 'user'" class="user-text">{{ message.content }}</div>

      <!-- assistant bubble - only when正文 content exists -->
      <div v-else-if="message.content" class="assistant-text">
        <div class="markdown-body" v-html="renderedContent"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ReasoningBlock from './ReasoningBlock.vue'
import ToolCallIndicator from './ToolCallIndicator.vue'
import { renderMarkdown } from '@/composables/useMarkdown'

const props = defineProps({ message: Object })

const renderedContent = computed(() => {
  if (!props.message.content) return ''
  return renderMarkdown(props.message.content)
})
</script>

<style scoped>
.message-item {
  display: flex;
  gap: 12px;
  padding: 20px 0;
}
.message-item.user {
  flex-direction: row-reverse;
}
.message-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}
.message-body {
  max-width: 80%;
  min-width: 60px;
}
.tool-calls {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* user bubble */
.user-text {
  background: #95ec69;
  color: #000;
  padding: 10px 16px;
  border-radius: 16px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
  font-size: 14px;
}

/* assistant bubble */
.assistant-text {
  background: #fff;
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.7;
  word-break: break-word;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  font-size: 14px;
}

/* markdown styles */
.markdown-body :deep(p) {
  margin: 4px 0;
}
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 12px 0 4px;
}
.markdown-body :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
  font-size: 13px;
}
.markdown-body :deep(code) {
  font-family: 'Consolas', 'Fira Code', monospace;
  font-size: 13px;
}
.markdown-body :deep(strong) {
  font-weight: 600;
}
.markdown-body :deep(ul) {
  padding-left: 20px;
  margin: 4px 0;
}
.markdown-body :deep(li) {
  margin: 2px 0;
}
</style>
