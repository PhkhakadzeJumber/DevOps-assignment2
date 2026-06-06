import axios, { AxiosError, AxiosRequestConfig } from "axios";

interface RefreshTokenResponse {
  accessToken: string;
}

interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  _retry?: boolean;
}

interface RefreshSubscriber {
  resolve: (token: string) => void;
  reject: (error: Error) => void;
}

let isRefreshing = false;
let refreshSubscribers: RefreshSubscriber[] = [];

function subscribeTokenRefresh(
  resolve: (token: string) => void,
  reject: (error: Error) => void
): void {
  refreshSubscribers.push({ resolve, reject });
}

function onRefreshed(token: string): void {
  refreshSubscribers.forEach(({ resolve }) => resolve(token));
  refreshSubscribers = [];
}

function onRefreshFailed(error: Error): void {
  refreshSubscribers.forEach(({ reject }) => reject(error));
  refreshSubscribers = [];
}

function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("accessToken");
}

function setAccessToken(token: string): void {
  if (typeof window !== "undefined") {
    localStorage.setItem("accessToken", token);
  }
}

function clearAccessToken(): void {
  if (typeof window !== "undefined") {
    localStorage.removeItem("accessToken");
  }
}

function notifyAuthLogout(): void {
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("auth:logout"));
  }
}

function redirectToLogin(): void {
  if (typeof window !== "undefined") {
    window.location.href = "/auth/login";
  }
}

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true,
  timeout: 10000,
});


apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as ExtendedAxiosRequestConfig;
    const requestUrl = originalRequest?.url ?? "";
    
    if (
      (error.response?.status === 401 || error.response?.status === 403) &&
      originalRequest && 
      !originalRequest._retry &&
      !requestUrl.includes("/auth/refresh-token")
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          subscribeTokenRefresh(
            (token: string) => {
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${token}`;
              }
              resolve(apiClient(originalRequest));
            },
            (refreshError: Error) => {
              reject(refreshError);
            }
          );
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshUrl = `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh-token`;
        
        const response = await axios.post<RefreshTokenResponse>(
          refreshUrl,
          {},
          { 
            withCredentials: true,
            timeout: 5000
          }
        );

        const newAccessToken = response.data.accessToken;
        
        if (!newAccessToken) {
          throw new Error("No access token received from refresh endpoint");
        }

        setAccessToken(newAccessToken);
        
        onRefreshed(newAccessToken);
        
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        
        return apiClient(originalRequest);
        
      } catch (refreshError) {
        console.warn("Token refresh failed:", refreshError);
        clearAccessToken();
        notifyAuthLogout();

        const normalizedError =
          refreshError instanceof Error
            ? refreshError
            : new Error("Token refresh failed");
        onRefreshFailed(normalizedError);

        redirectToLogin();
        return Promise.reject(normalizedError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
