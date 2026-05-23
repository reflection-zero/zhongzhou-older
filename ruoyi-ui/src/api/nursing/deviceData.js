import request from '@/utils/request'

export function listDeviceData(query) {
  return request({ url: '/nursing/data/list', method: 'get', params: query })
}

export function getDeviceData(id) {
  return request({ url: '/nursing/data/' + id, method: 'get' })
}

export function addDeviceData(data) {
  return request({ url: '/nursing/data', method: 'post', data: data })
}

export function updateDeviceData(data) {
  return request({ url: '/nursing/data', method: 'put', data: data })
}

export function delDeviceData(ids) {
  return request({ url: '/nursing/data/' + ids, method: 'delete' })
}
