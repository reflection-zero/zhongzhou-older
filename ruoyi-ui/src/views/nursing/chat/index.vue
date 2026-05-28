<template>
  <div class="chat-page">
    <div class="chat-sidebar">
      <ChatSessionList
        :sessions="sessions"
        :currentId="currentSessionId"
        @select="onSelectSession"
        @new="onNewSession"
      />
    </div>
    <div class="chat-main">
      <ChatMessageArea :messages="currentMessages" :typing="typing" />
      <ChatInput :disabled="!wsConnected" @send="onSend" />
    </div>
  </div>
</template>

<script>
import ChatSessionList from '@/components/Chat/ChatSessionList.vue'
import ChatMessageArea from '@/components/Chat/ChatMessageArea.vue'
import ChatInput from '@/components/Chat/ChatInput.vue'
import { mapState } from 'vuex'

export default {
  name: 'ChatPage',
  components: { ChatSessionList, ChatMessageArea, ChatInput },
  computed: {
    ...mapState('chat', ['sessions', 'currentSessionId', 'messages', 'wsConnected', 'typing']),
    currentMessages() {
      return this.messages[this.currentSessionId] || []
    }
  },
  created() {
    this.userId = this.$store.state.user?.userId || 1
    this.initChat()
  },
  beforeDestroy() {
    this.$store.dispatch('chat/disconnect')
  },
  methods: {
    async initChat() {
      await this.$store.dispatch('chat/initWebSocket', { userId: this.userId, userType: 'admin' })
      this.$store.dispatch('chat/fetchSessions', 'admin')
    },
    onSelectSession(sessionId) {
      this.$store.dispatch('chat/loadMessages', { sessionId, userType: 'admin' })
    },
    onNewSession() {
      this.$store.commit('chat/SET_CURRENT_SESSION', 0)
      this.$store.commit('chat/SET_MESSAGES', { sessionId: 0, messages: [] })
    },
    onSend(content) {
      this.$store.commit('chat/ADD_MESSAGE', {
        sessionId: this.currentSessionId || 0,
        message: { role: 'user', content }
      })
      this.$store.dispatch('chat/sendMessage', { sessionId: this.currentSessionId, content })
    }
  }
}
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 84px);
  background: #fff;
}
.chat-sidebar {
  width: 200px;
  border-right: 1px solid #e8e8e8;
  flex-shrink: 0;
  overflow: hidden;
}
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
