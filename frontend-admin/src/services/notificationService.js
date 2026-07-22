import axios from 'axios';

const API_URL = '/api/notifications';

export const notificationService = {
  // Admin fetches ALL notifications
  getAllNotifications: async () => {
    const response = await axios.get(`${API_URL}`);
    return response.data;
  }
};
