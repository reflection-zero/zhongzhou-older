<template>
  <div class="chat-bubble" :class="role">
    <div class="bubble-content" v-if="role === 'assistant' && type === 'error'">
      <i class="el-icon-warning" style="color:#f56c6c;margin-right:4px"></i>
      <span style="color:#f56c6c">{{ content }}</span>
    </div>
    <div class="bubble-content" v-else>
      {{ content }}
    </div>
    <div class="bubble-time" v-if="timestamp">{{ timestamp }}</div>
  </div>
</template>

<script>
export default {
  name: 'ChatBubble',
  props: {
    role: { type: String, required: true },
    content: { type: String, required: true },
    timestamp: { type: String, default: '' },
    type: { type: String, default: '' }
  }
}
</script>

<style scoped>
.chat-bubble { margin-bottom: 12px; max-width: 85%; }
.chat-bubble.user { margin-left: auto; text-align: right; }
.chat-bubble.assistant { margin-right: auto; }

.bubble-content {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 8px;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.6;
}
.user .bubble-content {
  background: #409eff;
  color: #fff;
}
.assistant .bubble-content {
  background: #f0f2f5;
  color: #303133;
}
.bubble-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}
</style>
