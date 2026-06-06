"use client";

import { AuthContextType } from "@/types";
import { createContext, useContext, useEffect, useState, ReactNode } from "react";

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthInitialized, setIsAuthInitialized] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [role, setRole] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);

  const resolveRole = (payload: { roles?: string[]; role?: string } | null): string | null => {
    if (!payload) return null;
    if (Array.isArray(payload.roles) && payload.roles.length > 0) {
      return payload.roles[0];
    }
    return payload.role ?? null;
  };

  const decodeToken = (jwt: string) => {
    try {
      const base64Url = jwt.split(".")[1];
      if (!base64Url) return null;
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
      const payload = JSON.parse(atob(padded));
      return payload;
    } catch (e) {
      console.error("Invalid JWT", e);
      return null;
    }
  };

  useEffect(() => {
    const storedToken = localStorage.getItem("accessToken");
    if (storedToken) {
      const payload = decodeToken(storedToken);
      if (payload) {
        setToken(storedToken);
        setRole(resolveRole(payload));
        setIsLoggedIn(true);
      }
    }
    setIsAuthInitialized(true);
  }, []);

  useEffect(() => {
    const handleForcedLogout = () => {
      setToken(null);
      setRole(null);
      setIsLoggedIn(false);
      setIsAuthInitialized(true);
    };

    window.addEventListener("auth:logout", handleForcedLogout);
    return () => window.removeEventListener("auth:logout", handleForcedLogout);
  }, []);

  const login = (newToken: string) => {
    localStorage.setItem("accessToken", newToken);
    const payload = decodeToken(newToken);
    setToken(newToken);
    setRole(resolveRole(payload));
    setIsLoggedIn(true);
    setIsAuthInitialized(true);
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    setToken(null);
    setRole(null);
    setIsLoggedIn(false);
    setIsAuthInitialized(true);
  };

  return (
    <AuthContext.Provider
      value={{ isAuthInitialized, isLoggedIn, role, token, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
