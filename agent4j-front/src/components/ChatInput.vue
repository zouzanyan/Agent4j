<template>
  <div class="chat-input-wrap">
    <div class="chat-input-inner">
      <el-input
        v-model="modelValue"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 5 }"
        placeholder="Send a message..."
        :disabled="disabled"
        resize="none"
        @keydown.enter.exact.prevent="handleSend"
      />
      <el-button
        type="primary"
        :icon="Promotion"
        :disabled="disabled || !modelValue.trim()"
        circle
        @click="handleSend"
      />
    </div>
  </div>
</template>

<script setup>
import { Promotion } from '@element-plus/icons-vue'

const modelValue = defineModel({ type: String, default: '' })
const props = defineProps({ disabled: Boolean })
const emit = defineEmits(['send'])

function handleSend() {
  if (modelValue.value.trim() && !props.disabled) {
    emit('send')
  }
}
</script>

<style scoped>
.chat-input-wrap {
  padding: 12px 24px 20px;
  background: #f5f5f5;
}
.chat-input-inner {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 24px;
  padding: 6px 6px 6px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.chat-input-inner :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 6px 0;
  font-size: 14px;
  background: transparent;
}
.chat-input-inner :deep(.el-textarea__inner:focus) {
  border: none;
  box-shadow: none;
}
</style>
