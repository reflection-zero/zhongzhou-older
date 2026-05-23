import request from '@/utils/request'

export function listDevice(query) {
  return request({ url: '/nursing/device/list', method: 'get', params: query })
}

export function getDevice(id) {
  return request({ url: '/nursing/device/' + id, method: 'get' })
}

export function addDevice(data) {
  return request({ url: '/nursing/device', method: 'post', data: data })
}

export function updateDevice(data) {
  return request({ url: '/nursing/device', method: 'put', data: data })
}

export function delDevice(ids) {
  return request({ url: '/nursing/device/' + ids, method: 'delete' })
}
