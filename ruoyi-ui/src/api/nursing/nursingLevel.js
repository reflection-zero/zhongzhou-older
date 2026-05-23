import request from '@/utils/request'

export function listNursingLevel(query) {
  return request({ url: '/nursing/nursingLevel/list', method: 'get', params: query })
}

export function getNursingLevel(id) {
  return request({ url: '/nursing/nursingLevel/' + id, method: 'get' })
}

export function addNursingLevel(data) {
  return request({ url: '/nursing/nursingLevel', method: 'post', data: data })
}

export function updateNursingLevel(data) {
  return request({ url: '/nursing/nursingLevel', method: 'put', data: data })
}

export function delNursingLevel(ids) {
  return request({ url: '/nursing/nursingLevel/' + ids, method: 'delete' })
}
