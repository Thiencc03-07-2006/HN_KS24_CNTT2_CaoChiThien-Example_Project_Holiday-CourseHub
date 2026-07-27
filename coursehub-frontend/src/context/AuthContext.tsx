import React, { createContext, useContext, useState, useEffect } from 'react';
import axiosInstance, { setAccessToken, refreshAccessTokenRequest } from '../api/axiosInstance';
import type { User, LoginRequest, RegisterRequest, UserProfileResponse } from '../types/auth.types';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  verifyOtp: (email: string, otpCode: string) => Promise<void>;
  logout: () => Promise<void>;
  updateProfile: (fullName: string, phoneNumber?: string, bio?: string) => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const mapProfileToUser = (profile: UserProfileResponse): User => {
    return {
      id: profile.id,
      email: profile.email,
      fullName: profile.fullName,
      avatarUrl: profile.avatarUrl,
      roles: profile.roles,
    };
  };

  const refreshProfile = async () => {
    try {
      const res = await axiosInstance.get('/users/me');
      if (res.data?.success && res.data?.data) {
        setUser(mapProfileToUser(res.data.data));
      }
    } catch (err) {
      console.error('Failed to refresh user profile:', err);
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      try {
        const token = await refreshAccessTokenRequest();
        if (token) {
          const profileRes = await axiosInstance.get('/users/me');
          if (profileRes.data?.success && profileRes.data?.data) {
            setUser(mapProfileToUser(profileRes.data.data));
          }
        }
      } catch (err) {
        console.log('No active session found on startup');
      } finally {
        setLoading(false);
      }
    };

    initAuth();

    const handleLogoutEvent = () => {
      setUser(null);
      setAccessToken(null);
    };

    window.addEventListener('auth:logout', handleLogoutEvent);
    return () => {
      window.removeEventListener('auth:logout', handleLogoutEvent);
    };
  }, []);

  const login = async (credentials: LoginRequest) => {
    const res = await axiosInstance.post('/auth/login', credentials);
    if (res.data?.success && res.data?.data) {
      const token = res.data.data.accessToken;
      setAccessToken(token);
      setUser(res.data.data.user);
    }
  };

  const register = async (data: RegisterRequest) => {
    await axiosInstance.post('/auth/register', data);
  };

  const verifyOtp = async (email: string, otpCode: string) => {
    await axiosInstance.post('/auth/verify-otp', { email, otpCode });
  };

  const logout = async () => {
    try {
      await axiosInstance.post('/auth/logout');
    } catch (err) {
      console.warn('Logout request failed:', err);
    } finally {
      setUser(null);
      setAccessToken(null);
    }
  };

  const updateProfile = async (fullName: string, phoneNumber?: string, bio?: string) => {
    const res = await axiosInstance.put('/users/me', { fullName, phoneNumber, bio });
    if (res.data?.success && res.data?.data) {
      setUser(mapProfileToUser(res.data.data));
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        verifyOtp,
        logout,
        updateProfile,
        refreshProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
