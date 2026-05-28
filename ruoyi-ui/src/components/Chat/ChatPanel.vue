<template>
  <el-drawer
    title="问题咨询"
    :visible.sync="visible"
    direction="rtl"
    size="500px"
    :before-close="onClose"
    :modal="false"
    custom-class="chat-drawer"
  >
    <div class="chat-panel">
      <ChatSessionList
        :sessions="sessions"
        :currentId="currentSessionId"
        @select="onSelectSession"
        @new="onNewSession"
      />
      <div class="chat-main">
        <ChatMessageArea :messages="currentMessages" :typing="typing" />
        <ChatInput :disabled="!wsConnected" @send="onSend" />
      </div>
    </div>
  </el-drawer>
</template>

<script>
import ChatSessionList from './ChatSessionList.vue'
import ChatMessageArea from './ChatMessageArea.vue'
import ChatInput from './ChatInput.vue'
import { mapState } from 'vuex'

export default {
  name: 'ChatPanel',
  components: { ChatSessionList, ChatMessageArea, ChatInput },
  props: { visible: Boolean },
  computed: {
    ...mapState('chat', ['sessions', 'currentSessionId', 'messages', 'wsConnected', 'typing']),
    currentMessages() {
      return this.messages[this.currentSessionId] || []
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.$store.dispatch('chat/initWebSocket', { userId: this.userId, userType: 'admin' })
        this.$store.dispatch('chat/fetchSessions', 'admin')
      }
    }
  },
  data() { return { userId: 1 } }, // admin userId from store
  created() {
    // 从 Vuex 获取当前管理员的 userId
    const user = this.$store.state.user
    if (user && user.userId) this.userId = user.userId
  },
  methods: {
    onSelectSession(sessionId) {
      this.$store.dispatch('chat/loadMessages', { sessionId, userType: 'admin' })
    },
    onNewSession() {
      this.$store.commit('chat/SET_CURRENT_SESSION', 0)
      this.$store.commit('chat/SET_MESSAGES', { sessionId: 0, messages: [] })
    },
    onSend(content) {
      // 先乐观更新 UI
      this.$store.commit('chat/ADD_MESSAGE', {
        sessionId: this.currentSessionId || 0,
        message: { role: 'user', content }
      })
      this.$store.dispatch('chat/sendMessage', { sessionId: this.currentSessionId, content })
    },
    onClose(done) {
      this.$store.dispatch('chat/disconnect')
      done()
    }
  }
}
</script>

<style scoped>
.chat-panel { display: flex; height: 100%; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
</style>
<style>
.chat-drawer .el-drawer__body { padding: 0; height: 100%; overflow: hidden; }
</style>
