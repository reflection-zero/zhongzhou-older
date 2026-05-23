import request from '@/utils/request'

export function listContract(query) {
  return request({ url: '/nursing/contract/list', method: 'get', params: query })
}

export function getContract(id) {
  return request({ url: '/nursing/contract/' + id, method: 'get' })
}

export function addContract(data) {
  return request({ url: '/nursing/contract', method: 'post', data: data })
}

export function updateContract(data) {
  return request({ url: '/nursing/contract', method: 'put', data: data })
}

export function delContract(ids) {
  return request({ url: '/nursing/contract/' + ids, method: 'delete' })
}
