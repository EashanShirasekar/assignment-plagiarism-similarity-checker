import api from './api'

export function analyzeAssignment(assignmentId) {
  return api.post(`/analysis/assignments/${assignmentId}`).then((res) => res.data)
}

export function getResults(assignmentId) {
  return api.get(`/analysis/assignments/${assignmentId}`).then((res) => res.data)
}

export function getResult(resultId) {
  return api.get(`/analysis/${resultId}`).then((res) => res.data)
}

export function getComparison(resultId) {
  return api.get(`/analysis/${resultId}/comparison`).then((res) => res.data)
}

export function getTeacherDashboardStats() {
  return api.get('/dashboard/teacher').then((res) => res.data)
}
