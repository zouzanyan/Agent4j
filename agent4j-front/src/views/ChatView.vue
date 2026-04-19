<template>
  <div class="chat-layout">
    <Sidebar
      :conversations="state.conversations"
      :current-id="state.currentConversationId"
      @select="selectConversation"
      @new-chat="newConversation"
    />
    <div class="chat-main">
      <MessageList :messages="state.messages" />
      <ChatInput
        v-model="state.inputValue"
        :disabled="state.isStreaming"
        @send="sendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
import MessageList from '@/components/MessageList.vue'
import ChatInput from '@/components/ChatInput.vue'
import { useChat } from '@/composables/useChat'

const { state, selectConversation, newConversation, sendMessage } = useChat()
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
</style>
