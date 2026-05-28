import request from '@/utils/request'

const ADMIN_PREFIX = '/nursing/chat'
const MEMBER_PREFIX = '/member/chat'

// 根据 userType 选择 API 前缀
function prefix(userType) {
  return userType === 'admin' ? ADMIN_PREFIX : MEMBER_PREFIX
}

/** 获取会话列表 */
export function listSessions(userType) {
  return request({ url: `${prefix(userType)}/sessions`, method: 'get' })
}

/** 获取会话消息 */
export function getMessages(sessionId, userType) {
  return request({ url: `${prefix(userType)}/sessions/${sessionId}`, method: 'get' })
}

/** 关闭会话 */
export function closeSession(sessionId, userType) {
  return request({ url: `${prefix(userType)}/sessions/${sessionId}`, method: 'delete' })
}
