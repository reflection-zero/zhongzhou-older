import request from '@/utils/request'

export function listAlertData(query) {
  return request({ url: '/nursing/alertData/list', method: 'get', params: query })
}

export function getAlertData(id) {
  return request({ url: '/nursing/alertData/' + id, method: 'get' })
}

export function addAlertData(data) {
  return request({ url: '/nursing/alertData', method: 'post', data: data })
}

export function updateAlertData(data) {
  return request({ url: '/nursing/alertData', method: 'put', data: data })
}

export function delAlertData(ids) {
  return request({ url: '/nursing/alertData/' + ids, method: 'delete' })
}
