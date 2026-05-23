import request from '@/utils/request'

export function listRoomType(query) {
  return request({ url: '/elder/roomType/list', method: 'get', params: query })
}

export function getRoomType(id) {
  return request({ url: '/elder/roomType/' + id, method: 'get' })
}

export function addRoomType(data) {
  return request({ url: '/elder/roomType', method: 'post', data: data })
}

export function updateRoomType(data) {
  return request({ url: '/elder/roomType', method: 'put', data: data })
}

export function delRoomType(ids) {
  return request({ url: '/elder/roomType/' + ids, method: 'delete' })
}
