<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <h2>AI Chat</h2>
      <el-button type="primary" :icon="Plus" circle size="small" @click="$emit('new-chat')" />
    </div>
    <div class="conversation-list">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        :class="['conv-item', { active: conv.id === currentId }]"
        @click="$emit('select', conv.id)"
      >
        <el-icon><ChatLineSquare /></el-icon>
        <span class="conv-title">{{ conv.title || 'New Chat' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Plus, ChatLineSquare } from '@element-plus/icons-vue'
defineProps({ conversations: Array, currentId: Number })
defineEmits(['select', 'new-chat'])
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  background: #1e1e2e;
  color: #cdd6f4;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #313244;
}
.sidebar-header h2 {
  font-size: 18px;
  font-weight: 600;
}
.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}
.conv-item:hover {
  background: #313244;
}
.conv-item.active {
  background: #45475a;
}
.conv-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
