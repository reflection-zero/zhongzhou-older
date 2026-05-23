import request from '@/utils/request'

export function listBed(query) {
  return request({ url: '/elder/bed/list', method: 'get', params: query })
}

export function getBed(id) {
  return request({ url: '/elder/bed/' + id, method: 'get' })
}

export function addBed(data) {
  return request({ url: '/elder/bed', method: 'post', data: data })
}

export function updateBed(data) {
  return request({ url: '/elder/bed', method: 'put', data: data })
}

export function delBed(ids) {
  return request({ url: '/elder/bed/' + ids, method: 'delete' })
}
