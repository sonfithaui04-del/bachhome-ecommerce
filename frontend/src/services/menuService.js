import axios from 'axios'

const API_URL = '/api'

export const menuService = {
  async getMenu() {
    const response = await axios.get(`${API_URL}/menu?availableOnly=true`)
    return response.data
  },

  async getCategories() {
    const response = await axios.get(`${API_URL}/categories?activeOnly=true`)
    return response.data
  }
}
