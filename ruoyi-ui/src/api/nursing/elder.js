import request from '@/utils/request'

export function listElder(query) {
  return request({ url: '/nursing/elder/list', method: 'get', params: query })
}

export function getElder(id) {
  return request({ url: '/nursing/elder/' + id, method: 'get' })
}

export function addElder(data) {
  return request({ url: '/nursing/elder', method: 'post', data: data })
}

export function updateElder(data) {
  return request({ url: '/nursing/elder', method: 'put', data: data })
}

export function delElder(ids) {
  return request({ url: '/nursing/elder/' + ids, method: 'delete' })
}
