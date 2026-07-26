import axios from 'axios'

const API_URL = '/api'

export const productService = {
  async getMenu() {
    const response = await axios.get(`${API_URL}/products?availableOnly=true`)
    return response.data
  },

  async getCategories() {
    const response = await axios.get(`${API_URL}/categories?activeOnly=true`)
    return response.data
  }
}
