<template>
  <div class="tool-indicator">
    <div class="tool-dot" />
    <span class="tool-label">{{ toolName }}</span>
  </div>
</template>

<script setup>
defineProps({ toolName: String, toolArgs: String })
</script>

<style scoped>
.tool-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 12px;
  font-size: 12px;
  color: #409eff;
}
.tool-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  animation: pulse 1.2s infinite;
}
@keyframes pulse {
  0% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1); }
}
</style>
