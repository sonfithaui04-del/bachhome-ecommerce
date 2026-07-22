import React, { createContext, useState, useContext, useEffect } from 'react'
import { authService } from '../services/authService'

const AuthContext = createContext()

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const currentUser = authService.getCurrentUser()
    setUser(currentUser)
    setLoading(false)
  }, [])

  const login = async (credentials) => {
    const data = await authService.login(credentials)
    setUser(data)
    return data
  }

  const register = async (userData) => {
    const data = await authService.register(userData)
    setUser(data)
    return data
  }

  const logout = () => {
    authService.logout()
    setUser(null)
  }

  const getToken = () => {
    return authService.getToken()
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading, getToken }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
