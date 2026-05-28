/**
 * 客服聊天 WebSocket 客户端
 * 支持自动重连、退避等待、消息回调
 */
const WS_BASE = process.env.VUE_APP_WS_BASE || 'ws://localhost:9000'

class ChatWebSocket {
  constructor(userId, userType) {
    this.userId = userId
    this.userType = userType // 'admin' | 'member'
    this.ws = null
    this.listeners = { message: [], typing: [], error: [], open: [], close: [], session: [] }
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.manualClose = false
  }

  connect() {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return
    }
    this.manualClose = false
    const url = `${WS_BASE}/ws/chat/${this.userId}/${this.userType}`
    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      this.reconnectAttempts = 0
      this.listeners.open.forEach(fn => fn())
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        const type = data.type
        if (type && this.listeners[type]) {
          this.listeners[type].forEach(fn => fn(data))
        }
      } catch (e) {
        console.error('Chat WS parse error:', e)
      }
    }

    this.ws.onclose = () => {
      if (!this.manualClose) {
        this.scheduleReconnect()
      }
      this.listeners.close.forEach(fn => fn())
    }

    this.ws.onerror = (e) => {
      console.error('Chat WS error:', e)
    }
  }

  scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return
    if (this.reconnectTimer) return
    // 退避: 2s, 4s, 8s, 16s, 32s
    const delay = Math.min(2000 * Math.pow(2, this.reconnectAttempts), 30000)
    this.reconnectAttempts++
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect()
    }, delay)
  }

  send(sessionId, content, action = 'send') {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn('Chat WS not connected, reconnecting...')
      this.connect()
      // 简单队列：重连后不重发，消息丢失（用户看到发送失败会重试）
      return false
    }
    this.ws.send(JSON.stringify({ sessionId: sessionId || 0, content, action }))
    return true
  }

  on(type, callback) {
    if (this.listeners[type]) {
      this.listeners[type].push(callback)
    }
  }

  close() {
    this.manualClose = true
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
}

export default ChatWebSocket
