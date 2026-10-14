import api from './api'

export function getAssignments() {
  return api.get('/assignments').then((res) => res.data)
}

export function getAssignment(id) {
  return api.get(`/assignments/${id}`).then((res) => res.data)
}

export function createAssignment(payload) {
  return api.post('/assignments', payload).then((res) => res.data)
}

export function updateAssignment(id, payload) {
  return api.put(`/assignments/${id}`, payload).then((res) => res.data)
}

export function deleteAssignment(id) {
  return api.delete(`/assignments/${id}`).then((res) => res.data)
}
