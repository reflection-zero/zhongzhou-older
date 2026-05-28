import { listSessions, getMessages } from '@/api/nursing/chat'
import ChatWebSocket from '@/utils/chatWebSocket'

const state = {
  sessions: [],
  currentSessionId: null,
  messages: {},
  wsConnected: false,
  typing: false,
  unreadCount: 0
}

const mutations = {
  SET_SESSIONS(state, sessions) { state.sessions = sessions },
  SET_CURRENT_SESSION(state, sessionId) { state.currentSessionId = sessionId },
  SET_MESSAGES(state, { sessionId, messages }) { state.messages[sessionId] = messages },
  ADD_MESSAGE(state, { sessionId, message }) {
    if (!state.messages[sessionId]) state.messages[sessionId] = []
    state.messages[sessionId].push(message)
  },
  PREPEND_MESSAGE(state, { sessionId, message }) {
    if (!state.messages[sessionId]) state.messages[sessionId] = []
    state.messages[sessionId].unshift(message)
  },
  SET_WS_CONNECTED(state, connected) { state.wsConnected = connected },
  SET_TYPING(state, typing) { state.typing = typing },
  ADD_SESSION(state, session) {
    // 新会话放到顶部
    state.sessions.unshift(session)
  },
  UPDATE_SESSION_TITLE(state, { sessionId, title }) {
    const s = state.sessions.find(v => v.id === sessionId)
    if (s) s.title = title
  },
  INCREMENT_UNREAD(state) { state.unreadCount++ },
  RESET_UNREAD(state) { state.unreadCount = 0 }
}

const actions = {
  /** 初始化 WebSocket 连接 */
  initWebSocket({ commit, dispatch, state }, { userId, userType }) {
    return new Promise((resolve) => {
      const ws = new ChatWebSocket(userId, userType)
      ws.on('open', () => {
        commit('SET_WS_CONNECTED', true)
        resolve(ws)
      })
      ws.on('close', () => {
        commit('SET_WS_CONNECTED', false)
      })
      ws.on('message', (data) => {
        dispatch('handleMessage', data)
      })
      ws.on('typing', (data) => {
        commit('SET_TYPING', data.status)
      })
      ws.on('session', (data) => {
        if (data.sessionId && state.currentSessionId === 0) {
          commit('SET_CURRENT_SESSION', data.sessionId)
        }
      })
      ws.on('error', (data) => {
        dispatch('handleError', data)
      })
      ws.connect()
      state._ws = ws
    })
  },

  /** 处理收到的消息 */
  handleMessage({ commit, state }, data) {
    const sid = data.sessionId
    if (sid === state.currentSessionId) {
      commit('ADD_MESSAGE', { sessionId: sid, message: data })
    } else {
      commit('INCREMENT_UNREAD')
    }
    // 更新会话列表中的最后消息
    const session = state.sessions.find(s => s.id === sid)
    if (session) {
      session.lastMessage = data.content
    }
  },

  /** 处理错误消息 */
  handleError({ commit }, data) {
    const sid = data.sessionId
    if (sid) {
      commit('ADD_MESSAGE', { sessionId: sid, message: { role: 'assistant', content: data.content, type: 'error' } })
    }
  },

  /** 发送消息 */
  sendMessage({ state }, { sessionId, content }) {
    if (state._ws) {
      state._ws.send(sessionId || 0, content)
    }
  },

  /** 获取会话列表 */
  fetchSessions({ commit }, userType) {
    return listSessions(userType).then(res => {
      commit('SET_SESSIONS', res.data || [])
      return res.data
    })
  },

  /** 加载会话消息 */
  loadMessages({ commit }, { sessionId, userType }) {
    commit('SET_CURRENT_SESSION', sessionId)
    return getMessages(sessionId, userType).then(res => {
      const msgs = (res.data || []).map(m => ({
        role: m.role,
        content: m.content,
        timestamp: m.createTime
      }))
      commit('SET_MESSAGES', { sessionId, messages: msgs })
      return msgs
    })
  },

  /** 创建新会话 */
  createSession({ commit, state }) {
    commit('SET_CURRENT_SESSION', 0) // 0 = 新会话标记
    commit('SET_MESSAGES', { sessionId: 0, messages: [] })
    state._ws.send(0, '', 'new')
  },

  /** 关闭 WebSocket */
  disconnect({ state }) {
    if (state._ws) {
      state._ws.close()
      state._ws = null
      state.wsConnected = false
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
