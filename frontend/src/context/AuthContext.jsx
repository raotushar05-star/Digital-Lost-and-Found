import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import { authService } from "../services/authService";
import { userService } from "../services/userService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("lf_user");
    return stored ? JSON.parse(stored) : null;
  });
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadProfile = useCallback(async () => {
    if (!localStorage.getItem("lf_token")) {
      setLoading(false);
      return;
    }
    try {
      const data = await userService.getProfile();
      setProfile(data);
    } catch {
      // token likely expired; interceptor already handles redirect on 401
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = async (payload) => {
    const data = await authService.login(payload);
    localStorage.setItem("lf_token", data.accessToken);
    localStorage.setItem("lf_user", JSON.stringify(data.user));
    setUser(data.user);
    const fullProfile = await userService.getProfile();
    setProfile(fullProfile);
    return data.user;
  };

  const register = async (payload) => {
    return authService.register(payload);
  };

  const logout = () => {
    authService.logout().catch(() => {});
    localStorage.removeItem("lf_token");
    localStorage.removeItem("lf_user");
    setUser(null);
    setProfile(null);
    window.location.href = "/login";
  };

  const value = {
    user,
    profile,
    isAuthenticated: !!user,
    isPolice: user && (user.role === "POLICE_OFFICER" || user.role === "POLICE_ADMIN"),
    isAdmin: user && (user.role === "POLICE_ADMIN" || user.role === "SYSTEM_ADMIN"),
    role: user ? user.role : null,
    loading,
    login,
    register,
    logout,
    refreshProfile: loadProfile
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
