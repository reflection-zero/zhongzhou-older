import request from '@/utils/request'

export function listNursingProject(query) {
  return request({ url: '/nursing/project/list', method: 'get', params: query })
}

export function getNursingProject(id) {
  return request({ url: '/nursing/project/' + id, method: 'get' })
}

export function addNursingProject(data) {
  return request({ url: '/nursing/project', method: 'post', data: data })
}

export function updateNursingProject(data) {
  return request({ url: '/nursing/project', method: 'put', data: data })
}

export function delNursingProject(ids) {
  return request({ url: '/nursing/project/' + ids, method: 'delete' })
}
