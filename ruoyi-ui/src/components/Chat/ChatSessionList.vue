<template>
  <div class="chat-session-list">
    <div class="session-header">
      <el-button type="primary" size="mini" icon="el-icon-plus" @click="$emit('new')">新对话</el-button>
    </div>
    <div class="session-items">
      <div
        v-for="s in sessions"
        :key="s.id"
        class="session-item"
        :class="{ active: s.id === currentId }"
        @click="$emit('select', s.id)"
      >
        <div class="session-title">{{ s.title || '新对话' }}</div>
        <div class="session-time">{{ s.updateTime || s.lastTime }}</div>
      </div>
      <div v-if="sessions.length === 0" class="no-sessions">暂无历史会话</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatSessionList',
  props: {
    sessions: { type: Array, default: () => [] },
    currentId: { type: Number, default: 0 }
  }
}
</script>

<style scoped>
.chat-session-list { border-right: 1px solid #ebeef5; width: 160px; display: flex; flex-direction: column; }
.session-header { padding: 8px; }
.session-items { flex: 1; overflow-y: auto; }
.session-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f2f3f5;
}
.session-item:hover, .session-item.active { background: #ecf5ff; }
.session-title {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-time { font-size: 11px; color: #c0c4cc; margin-top: 2px; }
.no-sessions { text-align: center; color: #909399; padding: 20px; font-size: 13px; }
</style>
