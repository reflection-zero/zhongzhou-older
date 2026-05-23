import request from '@/utils/request'

export function listAlertRule(query) {
  return request({ url: '/nursing/alertRule/list', method: 'get', params: query })
}

export function getAlertRule(id) {
  return request({ url: '/nursing/alertRule/' + id, method: 'get' })
}

export function addAlertRule(data) {
  return request({ url: '/nursing/alertRule', method: 'post', data: data })
}

export function updateAlertRule(data) {
  return request({ url: '/nursing/alertRule', method: 'put', data: data })
}

export function delAlertRule(ids) {
  return request({ url: '/nursing/alertRule/' + ids, method: 'delete' })
}
