export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
}

export type AuthContextType = {
  isAuthInitialized: boolean;
  isLoggedIn: boolean;
  role: string | null;
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
};
