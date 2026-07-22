import React, { createContext, useState, useContext, useEffect } from 'react'
import axios from 'axios'

const AuthContext = createContext()

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('adminToken')
    const savedUser = localStorage.getItem('adminUser')
    if (token && savedUser) {
      setUser(JSON.parse(savedUser))
    }
    setLoading(false)
  }, [])

  const login = async (credentials) => {
    try {
      const response = await axios.post('/api/auth/login', credentials)
      const { token, ...userData } = response.data
      
      // Check if user is admin
      if (userData.role !== 'ADMIN') {
        throw new Error('Access denied. Admin privileges required.')
      }

      localStorage.setItem('adminToken', token)
      localStorage.setItem('adminUser', JSON.stringify(userData))
      setUser(userData)
      return userData
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminUser')
    setUser(null)
  }

  const getToken = () => {
    return localStorage.getItem('adminToken')
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, getToken }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
