import api from './api'

export function register({ name, email, password, role }) {
  return api.post('/auth/register', { name, email, password, role }).then((res) => res.data)
}

export function login({ email, password }) {
  return api.post('/auth/login', { email, password }).then((res) => res.data)
}
