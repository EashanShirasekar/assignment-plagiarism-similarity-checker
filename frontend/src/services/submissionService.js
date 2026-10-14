import api from './api'

export function submitAssignment(payload) {
  return api.post('/submissions', payload).then((res) => res.data)
}

export function getSubmission(id) {
  return api.get(`/submissions/${id}`).then((res) => res.data)
}

export function getSubmissionsForAssignment(assignmentId) {
  return api.get(`/assignments/${assignmentId}/submissions`).then((res) => res.data)
}
