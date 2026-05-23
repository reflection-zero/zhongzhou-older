import request from '@/utils/request'

export function listNursingPlan(query) {
  return request({ url: '/nursing/nursingPlan/list', method: 'get', params: query })
}

export function getNursingPlan(id) {
  return request({ url: '/nursing/nursingPlan/' + id, method: 'get' })
}

export function addNursingPlan(data) {
  return request({ url: '/nursing/nursingPlan', method: 'post', data: data })
}

export function updateNursingPlan(data) {
  return request({ url: '/nursing/nursingPlan', method: 'put', data: data })
}

export function delNursingPlan(ids) {
  return request({ url: '/nursing/nursingPlan/' + ids, method: 'delete' })
}
