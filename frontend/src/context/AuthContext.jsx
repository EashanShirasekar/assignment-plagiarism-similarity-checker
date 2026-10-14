import React, { createContext, useContext, useEffect, useState } from 'react'
import * as authService from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('pc_token')
    const storedUser = localStorage.getItem('pc_user')
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser))
      } catch {
        localStorage.removeItem('pc_token')
        localStorage.removeItem('pc_user')
      }
    }
    setLoading(false)
  }, [])

  function persistSession(authResponse) {
    const currentUser = {
      id: authResponse.userId,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
    }
    localStorage.setItem('pc_token', authResponse.token)
    localStorage.setItem('pc_user', JSON.stringify(currentUser))
    setUser(currentUser)
    return currentUser
  }

  async function login(credentials) {
    const response = await authService.login(credentials)
    return persistSession(response)
  }

  async function register(details) {
    const response = await authService.register(details)
    return persistSession(response)
  }

  function logout() {
    localStorage.removeItem('pc_token')
    localStorage.removeItem('pc_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
