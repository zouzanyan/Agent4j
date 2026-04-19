<template>
  <div class="reasoning-block">
    <div class="reasoning-header" @click="expanded = !expanded">
      <el-icon :size="14" color="#e6a23c"><Cpu /></el-icon>
      <span class="reasoning-label">{{ streaming ? 'Thinking...' : 'Thinking process' }}</span>
      <el-icon :size="12" class="toggle-icon" :class="{ rotated: expanded }">
        <ArrowRight />
      </el-icon>
    </div>
    <transition name="slide">
      <div v-if="expanded" class="reasoning-content">{{ content }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Cpu, ArrowRight } from '@element-plus/icons-vue'

defineProps({ content: String, streaming: Boolean })
const expanded = ref(true)
</script>

<style scoped>
.reasoning-block {
  margin-bottom: 10px;
  border: 1px solid #faecd8;
  border-radius: 8px;
  overflow: hidden;
  background: #fdf6ec;
}
.reasoning-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  cursor: pointer;
  user-select: none;
}
.reasoning-header:hover {
  background: rgba(230, 162, 60, 0.08);
}
.reasoning-label {
  font-size: 13px;
  color: #e6a23c;
  font-weight: 500;
}
.toggle-icon {
  margin-left: auto;
  transition: transform 0.2s;
  color: #e6a23c;
}
.toggle-icon.rotated {
  transform: rotate(90deg);
}
.reasoning-content {
  white-space: pre-wrap;
  font-size: 13px;
  color: #666;
  padding: 8px 12px;
  border-top: 1px solid #faecd8;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
}
.slide-enter-active,
.slide-leave-active {
  transition: all 0.2s ease;
}
.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
}
</style>
