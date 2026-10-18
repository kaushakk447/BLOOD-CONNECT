import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || '/api';

const client = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle token expiration
client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post(`${API_URL}/auth/refresh`, {
          refreshToken,
        });
        
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        
        originalRequest.headers.Authorization = `Bearer ${response.data.accessToken}`;
        return client(originalRequest);
      } catch (err) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  register: (data) => client.post('/auth/register', data),
  login: (data) => client.post('/auth/login', data),
  refresh: (refreshToken) => client.post('/auth/refresh', { refreshToken }),
};

// Blood Request API
export const bloodRequestAPI = {
  create: (data) => client.post('/blood-requests', data),
  getAll: () => client.get('/blood-requests/all'),
  getById: (id) => client.get(`/blood-requests/${id}`),
  getByUser: () => client.get('/blood-requests'),
  update: (id, data) => client.patch(`/blood-requests/${id}`, data),
};

// Donor API
export const donorAPI = {
  createProfile: (data) => client.post('/donors/profile', data),
  getProfile: () => client.get('/donors/profile'),
  updateAvailability: (data) => client.patch('/donors/availability', data),
  getRequests: () => client.get('/donors/requests'),
  getPendingRequests: () => client.get('/donors/requests/pending'),
  acceptRequest: (id) => client.post(`/donors/requests/${id}/accept`),
  declineRequest: (id) => client.post(`/donors/requests/${id}/decline`),
};

// Notification API
export const notificationAPI = {
  getAll: () => client.get('/notifications'),
  getUnread: () => client.get('/notifications/unread'),
  markAsRead: (id) => client.patch(`/notifications/${id}/read`),
  markAllAsRead: () => client.patch('/notifications/read-all'),
};

// Admin API
export const adminAPI = {
  getAllUsers: () => client.get('/admin/users'),
  getPendingDonors: () => client.get('/admin/donors/pending'),
  verifyDonor: (id, verified) => client.post(`/admin/donors/${id}/verify`, { verified }),
};

export default client;
