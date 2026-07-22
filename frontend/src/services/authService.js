import axios from 'axios'

const API_URL = '/api/auth'

export const authService = {
  async register(userData) {
    const response = await axios.post(`${API_URL}/register`, userData)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data))
    }
    return response.data
  },

  async login(credentials) {
    try {
      const response = await axios.post(`${API_URL}/login`, credentials)
      if (response.data.token) {
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data))
      }
      return response.data
    } catch (error) {
      if (error.response) {
        // Server trả về response lỗi (4xx, 5xx)
        console.error('[AUTH-SERVICE] Server Error Data:', error.response.data)
        const message = error.response.data?.message || `Lỗi hệ thống: ${error.response.status}`
        throw new Error(message)
      } else if (error.request) {
        // Request đã gửi nhưng không nhận được phản hồi
        console.error('[AUTH-SERVICE] No Response:', error.request)
        throw new Error('Server không phản hồi. Vui lòng kiểm tra kết nối mạng.')
      } else {
        // Có lỗi khi thiết lập request
        console.error('[AUTH-SERVICE] Setup Error:', error.message)
        throw new Error('Lỗi cấu hình đăng nhập.')
      }
    }
  },

  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  getCurrentUser() {
    return JSON.parse(localStorage.getItem('user'))
  },

  getToken() {
    return localStorage.getItem('token')
  }
}
