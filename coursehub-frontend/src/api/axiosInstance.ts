import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

// In-memory access token storage (not localStorage — more secure)
let accessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
  accessToken = token;
};

export const getAccessToken = () => accessToken;

// =============================================
// Axios Instance
// =============================================
const axiosInstance: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  withCredentials: true, // send HTTP-only refresh token cookie
  headers: {
    'Content-Type': 'application/json',
  },
});

// =============================================
// Request Interceptor — attach JWT
// =============================================
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (accessToken && config.headers) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// =============================================
// Response Interceptor — auto refresh on 401
// =============================================
let isRefreshing = false;
let pendingQueue: Array<{
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
}> = [];

let refreshPromise: Promise<string | null> | null = null;

const processQueue = (error: unknown, token: string | null = null) => {
  pendingQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token!);
  });
  pendingQueue = [];
};

export const refreshAccessTokenRequest = (): Promise<string | null> => {
  if (refreshPromise) {
    return refreshPromise;
  }

  isRefreshing = true;
  refreshPromise = axiosInstance.post('/auth/refresh-token')
    .then((response) => {
      const newAccessToken = response.data?.data?.accessToken;
      if (newAccessToken) {
        setAccessToken(newAccessToken);
        axiosInstance.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
        processQueue(null, newAccessToken);
        return newAccessToken;
      }
      throw new Error('No access token returned');
    })
    .catch((err) => {
      processQueue(err, null);
      setAccessToken(null);
      window.dispatchEvent(new Event('auth:logout'));
      throw err;
    })
    .finally(() => {
      refreshPromise = null;
      isRefreshing = false;
    });

  return refreshPromise;
};

axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error) => {
    const originalRequest = error.config;

    // If 401 and not already retried and not the refresh endpoint itself
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/auth/refresh-token') &&
      !originalRequest.url?.includes('/auth/login')
    ) {
      if (isRefreshing) {
        // Queue this request until refresh completes
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosInstance(originalRequest);
        });
      }

      originalRequest._retry = true;

      try {
        const newAccessToken = await refreshAccessTokenRequest();
        if (newAccessToken) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return axiosInstance(originalRequest);
        }
      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
