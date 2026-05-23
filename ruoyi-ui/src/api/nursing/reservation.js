import request from '@/utils/request'

export function listReservation(query) {
  return request({ url: '/nursing/reservation/list', method: 'get', params: query })
}

export function getReservation(id) {
  return request({ url: '/nursing/reservation/' + id, method: 'get' })
}

export function addReservation(data) {
  return request({ url: '/nursing/reservation', method: 'post', data: data })
}

export function updateReservation(data) {
  return request({ url: '/nursing/reservation', method: 'put', data: data })
}

export function delReservation(ids) {
  return request({ url: '/nursing/reservation/' + ids, method: 'delete' })
}
