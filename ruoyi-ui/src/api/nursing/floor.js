import request from '@/utils/request'

export function listFloor(query) {
  return request({ url: '/elder/floor/list', method: 'get', params: query })
}

export function getFloor(id) {
  return request({ url: '/elder/floor/' + id, method: 'get' })
}

export function addFloor(data) {
  return request({ url: '/elder/floor', method: 'post', data: data })
}

export function updateFloor(data) {
  return request({ url: '/elder/floor', method: 'put', data: data })
}

export function delFloor(ids) {
  return request({ url: '/elder/floor/' + ids, method: 'delete' })
}
