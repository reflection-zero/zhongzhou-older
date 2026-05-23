import request from '@/utils/request'

export function listNursingTask(query) {
  return request({ url: '/nursing/nursingTask/list', method: 'get', params: query })
}

export function getNursingTask(id) {
  return request({ url: '/nursing/nursingTask/' + id, method: 'get' })
}

export function addNursingTask(data) {
  return request({ url: '/nursing/nursingTask', method: 'post', data: data })
}

export function updateNursingTask(data) {
  return request({ url: '/nursing/nursingTask', method: 'put', data: data })
}

export function delNursingTask(ids) {
  return request({ url: '/nursing/nursingTask/' + ids, method: 'delete' })
}
