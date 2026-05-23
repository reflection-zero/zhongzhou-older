import request from '@/utils/request'

export function listHealthAssessment(query) {
  return request({ url: '/nursing/healthAssessment/list', method: 'get', params: query })
}

export function getHealthAssessment(id) {
  return request({ url: '/nursing/healthAssessment/' + id, method: 'get' })
}

export function addHealthAssessment(data) {
  return request({ url: '/nursing/healthAssessment', method: 'post', data: data })
}

export function updateHealthAssessment(data) {
  return request({ url: '/nursing/healthAssessment', method: 'put', data: data })
}

export function delHealthAssessment(ids) {
  return request({ url: '/nursing/healthAssessment/' + ids, method: 'delete' })
}
