<template>
  <div class="chat-trigger">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <i class="el-icon-service chat-icon" @click="openChat" title="问题咨询"></i>
    </el-badge>
    <ChatPanel :visible="panelVisible" @update:visible="val => panelVisible = val" />
  </div>
</template>

<script>
import ChatPanel from './ChatPanel.vue'
import { mapState } from 'vuex'

export default {
  name: 'ChatButton',
  components: { ChatPanel },
  data() { return { panelVisible: false } },
  computed: {
    ...mapState('chat', ['unreadCount'])
  },
  methods: {
    openChat() {
      this.panelVisible = true
      this.$store.commit('chat/RESET_UNREAD')
    }
  }
}
</script>

<style scoped>
.chat-icon {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  vertical-align: middle;
}
.chat-icon:hover { color: #409eff; }
</style>
