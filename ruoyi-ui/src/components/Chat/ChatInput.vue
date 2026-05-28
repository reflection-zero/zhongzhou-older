<template>
  <div class="chat-input-area">
    <el-input
      v-model="text"
      type="textarea"
      :rows="2"
      placeholder="输入您的问题，按 Enter 发送"
      :disabled="disabled"
      @keydown.native="onKeydown"
    />
    <el-button
      type="primary"
      size="small"
      :disabled="!text.trim() || disabled"
      @click="onSend"
      style="margin-top:6px;"
    >发送</el-button>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: { disabled: { type: Boolean, default: false } },
  data() { return { text: '' } },
  methods: {
    onKeydown(e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        this.onSend()
      }
    },
    onSend() {
      const content = this.text.trim()
      if (!content || this.disabled) return
      this.$emit('send', content)
      this.text = ''
    }
  }
}
</script>

<style scoped>
.chat-input-area { padding: 10px; border-top: 1px solid #ebeef5; }
</style>
