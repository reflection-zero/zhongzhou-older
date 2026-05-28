<template>
  <div class="chat-message-area" ref="scrollArea">
    <div v-if="messages.length === 0 && !typing" class="empty-hint">
      您好，我是智能客服"小州"，有什么可以帮您的？
    </div>
    <ChatBubble
      v-for="(msg, idx) in messages"
      :key="idx"
      :role="msg.role"
      :content="msg.content"
      :timestamp="msg.timestamp"
      :type="msg.type"
    />
    <div v-if="typing" class="typing-indicator">
      <span class="dot"></span><span class="dot"></span><span class="dot"></span>
    </div>
  </div>
</template>

<script>
import ChatBubble from './ChatBubble.vue'
export default {
  name: 'ChatMessageArea',
  components: { ChatBubble },
  props: {
    messages: { type: Array, default: () => [] },
    typing: { type: Boolean, default: false }
  },
  watch: {
    messages: { deep: true, handler() { this.scrollToBottom() } },
    typing(val) { if (val) this.scrollToBottom() }
  },
  methods: {
    scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.scrollArea
        if (el) el.scrollTop = el.scrollHeight
      })
    }
  }
}
</script>

<style scoped>
.chat-message-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.empty-hint {
  text-align: center;
  color: #909399;
  margin-top: 40px;
  font-size: 14px;
}
.typing-indicator { padding: 10px 14px; }
.dot {
  display: inline-block;
  width: 8px; height: 8px;
  margin: 0 2px;
  background: #c0c4cc;
  border-radius: 50%;
  animation: bounce 1.4s infinite both;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%,80%,100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
