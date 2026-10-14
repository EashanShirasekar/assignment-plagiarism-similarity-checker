import axios from 'axios'

// Use the production backend URL by default.
// Vercel can override this through VITE_API_BASE_URL.
const baseURL = (
  import.meta.env.VITE_API_BASE_URL ||
  'https://assignment-plagiarism-similarity-checker.onrender.com/api'
).replace(/\/+$/, '')

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Attach the saved JWT token to authenticated requests.
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('pc_token')

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error)
)

// Handle expired or invalid authentication sessions.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('pc_token')
      localStorage.removeItem('pc_user')

      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

/**
 * Extract a readable message from the backend ErrorResponse.
 */
export function extractErrorMessage(
  error,
  fallback = 'Something went wrong. Please try again.'
) {
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