import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'https://assignment-plagiarism-similarity-checker.onrender.com/api'

const api = axios.create({ baseURL })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('pc_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('pc_token')
      localStorage.removeItem('pc_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

/** Pulls a readable message out of the backend's ErrorResponse shape. */
export function extractErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (error?.response?.data) {
    const data = error.response.data
    if (data.details && data.details.length > 0) {
      return data.details.join(', ')
    }
    if (data.message) {
      return data.message
    }
  }
  return fallback
}

export default api
