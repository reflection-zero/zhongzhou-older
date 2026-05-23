import request from '@/utils/request'

export function listCheckIn(query) {
  return request({ url: '/nursing/checkIn/list', method: 'get', params: query })
}

export function getCheckIn(id) {
  return request({ url: '/nursing/checkIn/' + id, method: 'get' })
}

export function addCheckIn(data) {
  return request({ url: '/nursing/checkIn', method: 'post', data: data })
}

export function updateCheckIn(data) {
  return request({ url: '/nursing/checkIn', method: 'put', data: data })
}

export function delCheckIn(ids) {
  return request({ url: '/nursing/checkIn/' + ids, method: 'delete' })
}
