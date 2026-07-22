import axios from 'axios';

const API_URL = '/api/notifications';

export const notificationService = {
  getUserNotifications: async (userId) => {
    const response = await axios.get(`${API_URL}/user/${userId}`);
    return response.data;
  }
};
