import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import authService from '../services/authService.js';
import { tokenUtils } from '../utils/token.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(() => Boolean(tokenUtils.getToken()));

  const refreshUser = useCallback(async () => {
    const token = tokenUtils.getToken();
    if (!token) {
      setUser(null);
      setIsLoading(false);
      return null;
    }

    try {
      const currentUser = await authService.getCurrentUser();
      setUser(currentUser);
      return currentUser;
    } catch {
      tokenUtils.removeToken();
      setUser(null);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  useEffect(() => {
    const handleUnauthorized = () => {
      tokenUtils.removeToken();
      setUser(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  const applyAuthSession = useCallback((payload) => {
    if (payload?.accessToken) {
      tokenUtils.setToken(payload.accessToken);
    }
    setUser(payload?.user || null);
    return payload?.user || null;
  }, []);

  const login = useCallback(
    async ({ identifier, password }) => {
      const payload = await authService.login({ identifier, password });
      return applyAuthSession(payload);
    },
    [applyAuthSession]
  );

  const verifyRegistrationOtp = useCallback(
    async ({ email, otp }) => {
      const payload = await authService.verifyRegistrationOtp({ email, otp });
      return applyAuthSession(payload);
    },
    [applyAuthSession]
  );

  const logout = useCallback(async () => {
    try {
      if (tokenUtils.getToken()) {
        await authService.logout();
      }
    } catch {
      // Always clear local session even if token is already expired or server unreachable
    } finally {
      tokenUtils.removeToken();
      setUser(null);
    }
  }, []);

  const updateProfile = useCallback(async (profilePayload) => {
    const updatedUser = await authService.updateProfile(profilePayload);
    setUser(updatedUser);
    return updatedUser;
  }, []);

  const value = {
    user,
    setUser,
    isAuthenticated: Boolean(user),
    isAdmin: user?.role === 'ADMIN',
    isUser: user?.role === 'USER',
    isLoading,
    login,
    verifyRegistrationOtp,
    logout,
    refreshUser,
    updateProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
