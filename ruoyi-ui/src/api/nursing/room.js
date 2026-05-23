import request from '@/utils/request'

export function listRoom(query) {
  return request({ url: '/elder/room/list', method: 'get', params: query })
}

export function getRoom(id) {
  return request({ url: '/elder/room/' + id, method: 'get' })
}

export function addRoom(data) {
  return request({ url: '/elder/room', method: 'post', data: data })
}

export function updateRoom(data) {
  return request({ url: '/elder/room', method: 'put', data: data })
}

export function delRoom(ids) {
  return request({ url: '/elder/room/' + ids, method: 'delete' })
}
