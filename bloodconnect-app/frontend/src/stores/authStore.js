import { create } from 'zustand';
import { authAPI } from "../services/api";

export const useAuthStore = create((set) => ({
  user: null,
  accessToken: localStorage.getItem('accessToken') || null,
  refreshToken: localStorage.getItem('refreshToken') || null,
  isLoading: false,
  error: null,

  register: async (data) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authAPI.register(data);
      set({ isLoading: false });
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Registration failed';
      set({ isLoading: false, error: message });
      throw error;
    }
  },

  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authAPI.login({ email, password });
      const { accessToken, refreshToken, user } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('user', JSON.stringify(user));
      
      set({
        user,
        accessToken,
        refreshToken,
        isLoading: false,
      });
      
      return user;
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed';
      set({ isLoading: false, error: message });
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    set({ user: null, accessToken: null, refreshToken: null });
  },

  hydrate: () => {
    const user = localStorage.getItem('user');
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    
    if (user && accessToken) {
      set({
        user: JSON.parse(user),
        accessToken,
        refreshToken,
      });
    }
  },
}));
