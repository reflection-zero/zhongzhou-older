import request from '@/utils/request'

export function listCheckInConfig(query) {
  return request({ url: '/nursing/config/list', method: 'get', params: query })
}

export function getCheckInConfig(id) {
  return request({ url: '/nursing/config/' + id, method: 'get' })
}

export function addCheckInConfig(data) {
  return request({ url: '/nursing/config', method: 'post', data: data })
}

export function updateCheckInConfig(data) {
  return request({ url: '/nursing/config', method: 'put', data: data })
}

export function delCheckInConfig(ids) {
  return request({ url: '/nursing/config/' + ids, method: 'delete' })
}
